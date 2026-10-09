package com.example.shelfcount.viewmodel

import com.example.shelfcount.calculator.MeasurementFormatter
import com.example.shelfcount.calculator.ShelfCountCalculator
import com.example.shelfcount.model.MeasurementUnit
import com.example.shelfcount.model.SavedCalculation
import com.example.shelfcount.model.ShelfCountInput
import com.example.shelfcount.model.ShelfCountResult
import com.example.shelfcount.model.ShelfFitStatus
import java.math.BigDecimal

data class CalculatorUiState(
    val itemName: String = "",
    val notes: String = "",
    val unit: MeasurementUnit = MeasurementUnit.CM,
    val shelfWidthText: String = "120",
    val itemWidthText: String = "30",
    val gapText: String = "0",
    val shelfLevelsText: String = "4",
    val edgeMarginsEnabled: Boolean = false,
    val leftMarginText: String = "0",
    val rightMarginText: String = "0",
    val depthCheckEnabled: Boolean = false,
    val shelfDepthText: String = "30",
    val itemDepthText: String = "20",
    val depthMarginText: String = "0",
    val shelfWidthError: String? = null,
    val itemWidthError: String? = null,
    val gapError: String? = null,
    val levelsError: String? = null,
    val marginsError: String? = null,
    val depthError: String? = null,
    val result: ShelfCountResult = ShelfCountResult(
        availableWidth = BigDecimal("120"),
        itemsPerShelf = 4L,
        totalCapacity = 16L,
        usedWidth = BigDecimal("120"),
        remainingWidth = BigDecimal.ZERO,
        utilizationPercent = BigDecimal("100"),
        unusedPercent = BigDecimal.ZERO,
        depthFits = null,
        fitStatus = ShelfFitStatus.PERFECT_FIT
    )
) {
    fun toInput(): ShelfCountInput {
        val sWidth = shelfWidthText.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val iWidth = itemWidthText.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val g = gapText.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val levels = shelfLevelsText.toLongOrNull() ?: 1L
        val lMargin = leftMarginText.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val rMargin = rightMarginText.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val sDepth = shelfDepthText.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val iDepth = itemDepthText.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val dMargin = depthMarginText.toBigDecimalOrNull() ?: BigDecimal.ZERO

        return ShelfCountInput(
            itemName = itemName,
            notes = notes,
            unit = unit,
            shelfWidth = sWidth,
            itemWidth = iWidth,
            gap = g,
            shelfLevels = levels,
            edgeMarginsEnabled = edgeMarginsEnabled,
            leftMargin = lMargin,
            rightMargin = rMargin,
            depthCheckEnabled = depthCheckEnabled,
            shelfDepth = sDepth,
            itemDepth = iDepth,
            depthMargin = dMargin
        )
    }

    fun toSavedCalculation(isFavorite: Boolean = false): SavedCalculation {
        val input = toInput()
        return SavedCalculation(
            itemName = input.resolvedItemName(),
            notes = input.notes,
            unit = input.unit,
            shelfWidth = input.shelfWidth,
            itemWidth = input.itemWidth,
            gap = input.gap,
            shelfLevels = input.shelfLevels,
            edgeMarginsEnabled = input.edgeMarginsEnabled,
            leftMargin = input.leftMargin,
            rightMargin = input.rightMargin,
            depthCheckEnabled = input.depthCheckEnabled,
            shelfDepth = input.shelfDepth,
            itemDepth = input.itemDepth,
            depthMargin = input.depthMargin,
            itemsPerShelf = result.itemsPerShelf,
            totalCapacity = result.totalCapacity,
            usedWidth = result.usedWidth,
            remainingWidth = result.remainingWidth,
            utilizationPercent = result.utilizationPercent,
            depthFits = result.depthFits,
            fitStatus = result.fitStatus,
            timestamp = System.currentTimeMillis(),
            isFavorite = isFavorite
        )
    }

    fun formatShareText(): String {
        val name = itemName.trim().ifEmpty { "Untitled Shelf" }
        val unitSym = unit.symbol
        val sb = StringBuilder()
        sb.appendLine("ShelfCount")
        sb.appendLine("Calculation: $name")
        sb.appendLine("Shelf Width: ${shelfWidthText.trim()} $unitSym")
        sb.appendLine("Item Width: ${itemWidthText.trim()} $unitSym")
        if (gapText.trim() != "0" && gapText.trim() != "0.0") {
            sb.appendLine("Gap: ${gapText.trim()} $unitSym")
        }
        sb.appendLine("Shelf Levels: ${shelfLevelsText.trim()}")

        if (edgeMarginsEnabled) {
            sb.appendLine("Margins: Left ${leftMarginText.trim()} $unitSym, Right ${rightMarginText.trim()} $unitSym")
        }

        if (depthCheckEnabled) {
            val dFitStr = if (result.depthFits == true) "YES (Fit)" else "NO (Does not fit)"
            sb.appendLine("Depth Fit: $dFitStr (Shelf: ${shelfDepthText.trim()} $unitSym, Item: ${itemDepthText.trim()} $unitSym)")
        }

        sb.appendLine()
        sb.appendLine("Items / Shelf: ${result.itemsPerShelf}")
        sb.appendLine("Total Capacity: ${result.totalCapacity}")
        sb.appendLine("Used Width: ${MeasurementFormatter.formatMeasurement(result.usedWidth)} $unitSym")
        sb.appendLine("Remaining: ${MeasurementFormatter.formatMeasurement(result.remainingWidth)} $unitSym")
        sb.appendLine("Utilization: ${MeasurementFormatter.formatPercent(result.utilizationPercent)}")

        if (notes.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("Notes: ${notes.trim()}")
        }
        return sb.toString()
    }
}
