package com.example.shelfcount.calculator

import com.example.shelfcount.model.MeasurementUnit
import com.example.shelfcount.model.ShelfCountInput
import com.example.shelfcount.model.ShelfCountResult
import com.example.shelfcount.model.ShelfFitStatus
import java.math.BigDecimal
import java.math.RoundingMode

object ShelfCountCalculator {

    /**
     * Converts a value in the given unit to base unit (millimeters).
     */
    fun convertToBaseUnit(value: BigDecimal, unit: MeasurementUnit): BigDecimal {
        return unit.toMm(value)
    }

    /**
     * Converts a value in base unit (millimeters) to the specified unit.
     */
    fun convertFromBaseUnit(valueInMm: BigDecimal, targetUnit: MeasurementUnit): BigDecimal {
        return targetUnit.fromMm(valueInMm)
    }

    /**
     * Calculates the available shelf width after subtracting left and right margins.
     * Formula: shelfWidth − leftMargin − rightMargin.
     */
    fun calculateAvailableWidth(
        shelfWidth: BigDecimal,
        leftMargin: BigDecimal = BigDecimal.ZERO,
        rightMargin: BigDecimal = BigDecimal.ZERO
    ): BigDecimal {
        val available = shelfWidth.subtract(leftMargin).subtract(rightMargin)
        return if (available.compareTo(BigDecimal.ZERO) < 0) BigDecimal.ZERO else available
    }

    /**
     * Calculates the maximum integer N items that fit across the available width:
     * Condition: N × itemWidth + (N − 1) × gap ≤ availableWidth.
     *
     * For gap == 0: floor(availableWidth ÷ itemWidth)
     * For gap > 0: floor((availableWidth + gap) ÷ (itemWidth + gap))
     *
     * Always verifies N and N + 1.
     */
    fun calculateItemsPerShelf(
        availableWidth: BigDecimal,
        itemWidth: BigDecimal,
        gap: BigDecimal = BigDecimal.ZERO
    ): Long {
        if (availableWidth.compareTo(BigDecimal.ZERO) <= 0 || itemWidth.compareTo(BigDecimal.ZERO) <= 0) {
            return 0L
        }
        if (gap.compareTo(BigDecimal.ZERO) < 0) {
            return 0L
        }
        if (itemWidth.compareTo(availableWidth) > 0) {
            return 0L
        }

        var n: Long = if (gap.compareTo(BigDecimal.ZERO) == 0) {
            availableWidth.divideToIntegralValue(itemWidth).toLong()
        } else {
            val numerator = availableWidth.add(gap)
            val denominator = itemWidth.add(gap)
            numerator.divideToIntegralValue(denominator).toLong()
        }

        if (n < 0L) n = 0L

        // Verification step: ensure requiredWidth(n) <= availableWidth
        while (n > 0L) {
            val req = calculateRequiredWidth(n, itemWidth, gap)
            if (req.compareTo(availableWidth) <= 0) {
                break
            }
            n--
        }

        // Test N + 1: ensure next item does NOT fit (or increment if it does due to edge cases)
        while (true) {
            val nextN = n + 1L
            val reqNext = calculateRequiredWidth(nextN, itemWidth, gap)
            if (reqNext.compareTo(availableWidth) <= 0) {
                n = nextN
            } else {
                break
            }
        }

        return n
    }

    /**
     * Internal helper to calculate width required for n items:
     * n × itemWidth + (n − 1) × gap (if n > 0).
     */
    private fun calculateRequiredWidth(
        n: Long,
        itemWidth: BigDecimal,
        gap: BigDecimal
    ): BigDecimal {
        if (n <= 0L) return BigDecimal.ZERO
        val itemsWidth = itemWidth.multiply(BigDecimal.valueOf(n))
        val gapsWidth = if (n > 1L && gap.compareTo(BigDecimal.ZERO) > 0) {
            gap.multiply(BigDecimal.valueOf(n - 1L))
        } else {
            BigDecimal.ZERO
        }
        return itemsWidth.add(gapsWidth)
    }

    /**
     * Calculates actual width occupied by the items and gaps.
     * Formula:
     * If items == 0: 0
     * If items > 0: items × itemWidth + (items − 1) × gap
     */
    fun calculateUsedWidth(
        itemsPerShelf: Long,
        itemWidth: BigDecimal,
        gap: BigDecimal
    ): BigDecimal {
        return calculateRequiredWidth(itemsPerShelf, itemWidth, gap)
    }

    /**
     * Calculates remaining shelf width: availableShelfWidth − usedWidth.
     * The result is never negative.
     */
    fun calculateRemainingWidth(
        availableWidth: BigDecimal,
        usedWidth: BigDecimal
    ): BigDecimal {
        val remaining = availableWidth.subtract(usedWidth)
        return if (remaining.compareTo(BigDecimal.ZERO) < 0) BigDecimal.ZERO else remaining
    }

    /**
     * Calculates shelf utilization percentage: (usedWidth ÷ availableShelfWidth) × 100.
     */
    fun calculateUtilization(
        usedWidth: BigDecimal,
        availableWidth: BigDecimal
    ): BigDecimal {
        if (availableWidth.compareTo(BigDecimal.ZERO) <= 0 || usedWidth.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO
        }
        if (usedWidth.compareTo(availableWidth) >= 0) {
            return BigDecimal("100")
        }
        return usedWidth
            .multiply(BigDecimal("100"))
            .divide(availableWidth, 4, RoundingMode.HALF_UP)
    }

    /**
     * Calculates total capacity: itemsPerShelf × shelfLevels.
     * Uses overflow-safe multiplication.
     * Throws ArithmeticException on overflow.
     */
    fun calculateTotalCapacity(
        itemsPerShelf: Long,
        shelfLevels: Long
    ): Long {
        if (itemsPerShelf <= 0L || shelfLevels <= 0L) return 0L
        return Math.multiplyExact(itemsPerShelf, shelfLevels)
    }

    /**
     * Determines whether the item fits front-to-back:
     * Formula: itemDepth + depthMargin ≤ shelfDepth.
     */
    fun calculateDepthFit(
        shelfDepth: BigDecimal,
        itemDepth: BigDecimal,
        depthMargin: BigDecimal = BigDecimal.ZERO
    ): Boolean {
        if (shelfDepth.compareTo(BigDecimal.ZERO) <= 0 || itemDepth.compareTo(BigDecimal.ZERO) <= 0) {
            return false
        }
        val totalDepth = itemDepth.add(depthMargin)
        return totalDepth.compareTo(shelfDepth) <= 0
    }

    /**
     * Determines fit status: PERFECT_FIT, SPACE_REMAINING, or NO_ITEM_FIT.
     */
    fun determineFitStatus(
        itemsPerShelf: Long,
        remainingWidth: BigDecimal
    ): ShelfFitStatus {
        return when {
            itemsPerShelf == 0L -> ShelfFitStatus.NO_ITEM_FIT
            remainingWidth.compareTo(BigDecimal.ZERO) == 0 -> ShelfFitStatus.PERFECT_FIT
            else -> ShelfFitStatus.SPACE_REMAINING
        }
    }

    /**
     * Runs full calculation on given ShelfCountInput.
     * Calculations are performed in the base unit (millimeters), then results are mapped
     * to the user's selected unit.
     */
    fun calculate(input: ShelfCountInput): ShelfCountResult {
        val unit = input.unit

        // Validate basic physical inputs
        if (input.shelfWidth.compareTo(BigDecimal.ZERO) <= 0) {
            return ShelfCountResult(
                availableWidth = BigDecimal.ZERO,
                itemsPerShelf = 0L,
                totalCapacity = 0L,
                usedWidth = BigDecimal.ZERO,
                remainingWidth = BigDecimal.ZERO,
                utilizationPercent = BigDecimal.ZERO,
                unusedPercent = BigDecimal("100"),
                depthFits = if (input.depthCheckEnabled) false else null,
                fitStatus = ShelfFitStatus.NO_ITEM_FIT,
                errorMessage = "Shelf width must be greater than zero."
            )
        }

        if (input.itemWidth.compareTo(BigDecimal.ZERO) <= 0) {
            return ShelfCountResult(
                availableWidth = BigDecimal.ZERO,
                itemsPerShelf = 0L,
                totalCapacity = 0L,
                usedWidth = BigDecimal.ZERO,
                remainingWidth = BigDecimal.ZERO,
                utilizationPercent = BigDecimal.ZERO,
                unusedPercent = BigDecimal("100"),
                depthFits = if (input.depthCheckEnabled) false else null,
                fitStatus = ShelfFitStatus.NO_ITEM_FIT,
                errorMessage = "Item width must be greater than zero."
            )
        }

        if (input.gap.compareTo(BigDecimal.ZERO) < 0) {
            return ShelfCountResult(
                availableWidth = BigDecimal.ZERO,
                itemsPerShelf = 0L,
                totalCapacity = 0L,
                usedWidth = BigDecimal.ZERO,
                remainingWidth = BigDecimal.ZERO,
                utilizationPercent = BigDecimal.ZERO,
                unusedPercent = BigDecimal("100"),
                depthFits = if (input.depthCheckEnabled) false else null,
                fitStatus = ShelfFitStatus.NO_ITEM_FIT,
                errorMessage = "Gap cannot be negative."
            )
        }

        val shelfWidthMm = convertToBaseUnit(input.shelfWidth, unit)
        val itemWidthMm = convertToBaseUnit(input.itemWidth, unit)
        val gapMm = convertToBaseUnit(input.gap, unit)

        val leftMarginMm = if (input.edgeMarginsEnabled) convertToBaseUnit(input.leftMargin, unit) else BigDecimal.ZERO
        val rightMarginMm = if (input.edgeMarginsEnabled) convertToBaseUnit(input.rightMargin, unit) else BigDecimal.ZERO

        val availableWidthMm = calculateAvailableWidth(shelfWidthMm, leftMarginMm, rightMarginMm)

        if (availableWidthMm.compareTo(BigDecimal.ZERO) <= 0) {
            return ShelfCountResult(
                availableWidth = BigDecimal.ZERO,
                itemsPerShelf = 0L,
                totalCapacity = 0L,
                usedWidth = BigDecimal.ZERO,
                remainingWidth = BigDecimal.ZERO,
                utilizationPercent = BigDecimal.ZERO,
                unusedPercent = BigDecimal("100"),
                depthFits = if (input.depthCheckEnabled) false else null,
                fitStatus = ShelfFitStatus.NO_ITEM_FIT,
                errorMessage = "Available shelf width must be greater than zero after margins."
            )
        }

        val itemsPerShelf = calculateItemsPerShelf(availableWidthMm, itemWidthMm, gapMm)
        val usedWidthMm = calculateUsedWidth(itemsPerShelf, itemWidthMm, gapMm)
        val remainingWidthMm = calculateRemainingWidth(availableWidthMm, usedWidthMm)
        val utilization = calculateUtilization(usedWidthMm, availableWidthMm)
        val unused = BigDecimal("100").subtract(utilization)

        var totalCapacity = 0L
        var isOverflow = false
        try {
            totalCapacity = calculateTotalCapacity(itemsPerShelf, input.shelfLevels)
        } catch (_: ArithmeticException) {
            isOverflow = true
            totalCapacity = Long.MAX_VALUE
        }

        val depthFits = if (input.depthCheckEnabled) {
            val shelfDepthMm = convertToBaseUnit(input.shelfDepth, unit)
            val itemDepthMm = convertToBaseUnit(input.itemDepth, unit)
            val depthMarginMm = convertToBaseUnit(input.depthMargin, unit)
            calculateDepthFit(shelfDepthMm, itemDepthMm, depthMarginMm)
        } else {
            null
        }

        val fitStatus = determineFitStatus(itemsPerShelf, remainingWidthMm)

        // Convert dimensions back to user's unit for display
        val availableWidthDisplay = convertFromBaseUnit(availableWidthMm, unit)
        val usedWidthDisplay = convertFromBaseUnit(usedWidthMm, unit)
        val remainingWidthDisplay = convertFromBaseUnit(remainingWidthMm, unit)

        val formulaSteps = generateFormulaSteps(
            input = input,
            availableWidth = availableWidthDisplay,
            itemsPerShelf = itemsPerShelf,
            usedWidth = usedWidthDisplay,
            remainingWidth = remainingWidthDisplay,
            utilization = utilization,
            totalCapacity = totalCapacity,
            isOverflow = isOverflow,
            depthFits = depthFits
        )

        return ShelfCountResult(
            availableWidth = availableWidthDisplay,
            itemsPerShelf = itemsPerShelf,
            totalCapacity = totalCapacity,
            usedWidth = usedWidthDisplay,
            remainingWidth = remainingWidthDisplay,
            utilizationPercent = utilization,
            unusedPercent = unused,
            depthFits = depthFits,
            fitStatus = fitStatus,
            isCapacityOverflow = isOverflow,
            formulaSteps = formulaSteps
        )
    }

    /**
     * Generates human-readable, mathematically precise steps for the formula display.
     */
    private fun generateFormulaSteps(
        input: ShelfCountInput,
        availableWidth: BigDecimal,
        itemsPerShelf: Long,
        usedWidth: BigDecimal,
        remainingWidth: BigDecimal,
        utilization: BigDecimal,
        totalCapacity: Long,
        isOverflow: Boolean,
        depthFits: Boolean?
    ): List<String> {
        val steps = mutableListOf<String>()
        val unit = input.unit.symbol

        // 1. Available width step
        if (input.edgeMarginsEnabled && (input.leftMargin.compareTo(BigDecimal.ZERO) > 0 || input.rightMargin.compareTo(BigDecimal.ZERO) > 0)) {
            val sWidth = MeasurementFormatter.formatMeasurement(input.shelfWidth)
            val lMargin = MeasurementFormatter.formatMeasurement(input.leftMargin)
            val rMargin = MeasurementFormatter.formatMeasurement(input.rightMargin)
            val aWidth = MeasurementFormatter.formatMeasurement(availableWidth)
            steps.add("Available width = Shelf width − Left margin − Right margin")
            steps.add("$sWidth $unit − $lMargin $unit − $rMargin $unit = $aWidth $unit")
        } else {
            steps.add("Available width: ${MeasurementFormatter.formatMeasurement(availableWidth)} $unit")
        }

        // 2. Items per shelf calculation step
        val itemW = MeasurementFormatter.formatMeasurement(input.itemWidth)
        val gapW = MeasurementFormatter.formatMeasurement(input.gap)
        val hasGap = input.gap.compareTo(BigDecimal.ZERO) > 0

        if (itemsPerShelf == 0L) {
            steps.add("Item width ($itemW $unit) > Available width (${MeasurementFormatter.formatMeasurement(availableWidth)} $unit)")
            steps.add("Therefore: 0 items fit per shelf.")
        } else {
            // Show required width for N items
            val n = itemsPerShelf
            val nCalcStr = if (hasGap && n > 1L) {
                "$n × $itemW + ${n - 1} × $gapW = ${MeasurementFormatter.formatMeasurement(usedWidth)} $unit"
            } else {
                "$n × $itemW = ${MeasurementFormatter.formatMeasurement(usedWidth)} $unit"
            }
            steps.add("For $n items:")
            steps.add(nCalcStr)

            // Test N + 1
            val nextN = n + 1L
            val nextUsed = calculateUsedWidth(nextN, input.itemWidth, input.gap)
            val nextCalcStr = if (hasGap && nextN > 1L) {
                "$nextN × $itemW + $n × $gapW = ${MeasurementFormatter.formatMeasurement(nextUsed)} $unit"
            } else {
                "$nextN × $itemW = ${MeasurementFormatter.formatMeasurement(nextUsed)} $unit"
            }
            steps.add("For $nextN items:")
            steps.add(nextCalcStr)
            steps.add("${MeasurementFormatter.formatMeasurement(nextUsed)} $unit > ${MeasurementFormatter.formatMeasurement(availableWidth)} $unit")
            steps.add("Therefore: $nextN items do NOT fit. $n items fit per shelf.")
        }

        // 3. Total capacity calculation
        if (isOverflow) {
            steps.add("Total capacity: Exceeds maximum calculable limit.")
        } else {
            steps.add("Total capacity = Items / shelf × Shelf levels")
            steps.add("$itemsPerShelf × ${input.shelfLevels} = $totalCapacity items")
        }

        // 4. Space and Utilization
        if (itemsPerShelf > 0L) {
            steps.add("Used width: ${MeasurementFormatter.formatMeasurement(usedWidth)} $unit")
            steps.add("Remaining width = Available width − Used width")
            steps.add("${MeasurementFormatter.formatMeasurement(availableWidth)} $unit − ${MeasurementFormatter.formatMeasurement(usedWidth)} $unit = ${MeasurementFormatter.formatMeasurement(remainingWidth)} $unit")

            steps.add("Shelf utilization = Used width ÷ Available width × 100")
            steps.add("${MeasurementFormatter.formatMeasurement(usedWidth)} ÷ ${MeasurementFormatter.formatMeasurement(availableWidth)} × 100 = ${MeasurementFormatter.formatPercent(utilization)}")
        }

        // 5. Depth check
        if (depthFits != null) {
            val sDepth = MeasurementFormatter.formatMeasurement(input.shelfDepth)
            val iDepth = MeasurementFormatter.formatMeasurement(input.itemDepth)
            steps.add("Depth fit check: Item depth ≤ Shelf depth")
            val op = if (depthFits) "≤" else ">"
            val res = if (depthFits) "FIT (YES)" else "DOES NOT FIT (NO)"
            steps.add("$iDepth $unit $op $sDepth $unit = $res")
        }

        return steps
    }
}
