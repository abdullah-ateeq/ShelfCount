package com.example.shelfcount

import com.example.shelfcount.calculator.MeasurementFormatter
import com.example.shelfcount.calculator.ShelfCountCalculator
import com.example.shelfcount.model.MeasurementUnit
import com.example.shelfcount.model.ShelfCountInput
import com.example.shelfcount.model.ShelfFitStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class ShelfCountCalculatorTest {

    // 1. Basic fit
    @Test
    fun testBasicFit() {
        val input = ShelfCountInput(
            shelfWidth = BigDecimal("120"),
            itemWidth = BigDecimal("30"),
            gap = BigDecimal.ZERO,
            shelfLevels = 4L,
            unit = MeasurementUnit.CM
        )
        val result = ShelfCountCalculator.calculate(input)
        assertEquals(4L, result.itemsPerShelf)
        assertEquals(16L, result.totalCapacity)
        assertEquals(0, result.usedWidth.compareTo(BigDecimal("120")))
        assertEquals(0, result.remainingWidth.compareTo(BigDecimal.ZERO))
        assertEquals(0, result.utilizationPercent.compareTo(BigDecimal("100")))
        assertEquals(ShelfFitStatus.PERFECT_FIT, result.fitStatus)
    }

    // 2. Multiple shelf levels
    @Test
    fun testMultipleShelfLevels() {
        val input = ShelfCountInput(
            shelfWidth = BigDecimal("100"),
            itemWidth = BigDecimal("20"),
            gap = BigDecimal.ZERO,
            shelfLevels = 8L,
            unit = MeasurementUnit.CM
        )
        val result = ShelfCountCalculator.calculate(input)
        assertEquals(5L, result.itemsPerShelf)
        assertEquals(40L, result.totalCapacity)
    }

    // 3. Zero gap
    @Test
    fun testZeroGap() {
        val input = ShelfCountInput(
            shelfWidth = BigDecimal("100"),
            itemWidth = BigDecimal("20"),
            gap = BigDecimal.ZERO,
            shelfLevels = 5L,
            unit = MeasurementUnit.CM
        )
        val result = ShelfCountCalculator.calculate(input)
        assertEquals(5L, result.itemsPerShelf)
        assertEquals(0, result.usedWidth.compareTo(BigDecimal("100")))
    }

    // 4. Non-zero gap
    @Test
    fun testNonZeroGap() {
        // Shelf 100 cm, Item 30 cm, Gap 5 cm, Levels 1
        // 3 items = 30 + 5 + 30 + 5 + 30 = 100 cm
        val input = ShelfCountInput(
            shelfWidth = BigDecimal("100"),
            itemWidth = BigDecimal("30"),
            gap = BigDecimal("5"),
            shelfLevels = 1L,
            unit = MeasurementUnit.CM
        )
        val result = ShelfCountCalculator.calculate(input)
        assertEquals(3L, result.itemsPerShelf)
        assertEquals(0, result.usedWidth.compareTo(BigDecimal("100")))
        assertEquals(0, result.remainingWidth.compareTo(BigDecimal.ZERO))
        assertEquals(0, result.utilizationPercent.compareTo(BigDecimal("100")))
        assertEquals(ShelfFitStatus.PERFECT_FIT, result.fitStatus)
    }

    // 5. Exact fit
    @Test
    fun testExactFit() {
        val input = ShelfCountInput(
            shelfWidth = BigDecimal("90"),
            itemWidth = BigDecimal("30"),
            gap = BigDecimal.ZERO,
            shelfLevels = 2L,
            unit = MeasurementUnit.CM
        )
        val result = ShelfCountCalculator.calculate(input)
        assertEquals(ShelfFitStatus.PERFECT_FIT, result.fitStatus)
        assertEquals(0, result.remainingWidth.compareTo(BigDecimal.ZERO))
    }

    // 6. Remaining space
    @Test
    fun testRemainingSpace() {
        // Shelf 120 cm, Item 30 cm, Gap 2 cm, Levels 4
        // 3 items require 3 × 30 + 2 × 2 = 94 cm
        // 4 items require 4 × 30 + 3 × 2 = 126 cm > 120 cm
        // Result: 3 items / shelf, 12 total, used 94, remaining 26, utilization 78.33%
        val input = ShelfCountInput(
            shelfWidth = BigDecimal("120"),
            itemWidth = BigDecimal("30"),
            gap = BigDecimal("2"),
            shelfLevels = 4L,
            unit = MeasurementUnit.CM
        )
        val result = ShelfCountCalculator.calculate(input)
        assertEquals(3L, result.itemsPerShelf)
        assertEquals(12L, result.totalCapacity)
        assertEquals(0, result.usedWidth.compareTo(BigDecimal("94")))
        assertEquals(0, result.remainingWidth.compareTo(BigDecimal("26")))
        assertEquals("78.33%", MeasurementFormatter.formatPercent(result.utilizationPercent))
        assertEquals(ShelfFitStatus.SPACE_REMAINING, result.fitStatus)
    }

    // 7. Item too wide
    @Test
    fun testItemTooWide() {
        val input = ShelfCountInput(
            shelfWidth = BigDecimal("20"),
            itemWidth = BigDecimal("25"),
            gap = BigDecimal.ZERO,
            shelfLevels = 3L,
            unit = MeasurementUnit.CM
        )
        val result = ShelfCountCalculator.calculate(input)
        assertEquals(0L, result.itemsPerShelf)
        assertEquals(0L, result.totalCapacity)
        assertEquals(ShelfFitStatus.NO_ITEM_FIT, result.fitStatus)
    }

    // 8. Zero capacity
    @Test
    fun testZeroCapacity() {
        val input = ShelfCountInput(
            shelfWidth = BigDecimal("10"),
            itemWidth = BigDecimal("50"),
            gap = BigDecimal.ZERO,
            shelfLevels = 4L,
            unit = MeasurementUnit.CM
        )
        val result = ShelfCountCalculator.calculate(input)
        assertEquals(0L, result.totalCapacity)
    }

    // 9. Edge margins
    @Test
    fun testEdgeMargins() {
        val input = ShelfCountInput(
            shelfWidth = BigDecimal("120"),
            itemWidth = BigDecimal("25"),
            gap = BigDecimal.ZERO,
            shelfLevels = 2L,
            edgeMarginsEnabled = true,
            leftMargin = BigDecimal("10"),
            rightMargin = BigDecimal("10"),
            unit = MeasurementUnit.CM
        )
        val result = ShelfCountCalculator.calculate(input)
        assertEquals(0, result.availableWidth.compareTo(BigDecimal("100")))
        assertEquals(4L, result.itemsPerShelf)
        assertEquals(8L, result.totalCapacity)
    }

    // 10. Left/right margins (asymmetrical)
    @Test
    fun testAsymmetricMargins() {
        val input = ShelfCountInput(
            shelfWidth = BigDecimal("100"),
            itemWidth = BigDecimal("20"),
            gap = BigDecimal.ZERO,
            shelfLevels = 1L,
            edgeMarginsEnabled = true,
            leftMargin = BigDecimal("5"),
            rightMargin = BigDecimal("15"),
            unit = MeasurementUnit.CM
        )
        val result = ShelfCountCalculator.calculate(input)
        assertEquals(0, result.availableWidth.compareTo(BigDecimal("80")))
        assertEquals(4L, result.itemsPerShelf)
    }

    // 11. Utilization calculation
    @Test
    fun testUtilizationCalculation() {
        val used = BigDecimal("94")
        val available = BigDecimal("120")
        val util = ShelfCountCalculator.calculateUtilization(used, available)
        // 94 / 120 * 100 = 78.3333... -> 78.33%
        assertEquals(BigDecimal("78.3333"), util.setScale(4, RoundingMode.HALF_UP))
        assertEquals("78.33%", MeasurementFormatter.formatPercent(util))
    }

    // 12. Total capacity multiplication
    @Test
    fun testTotalCapacityMultiplication() {
        assertEquals(24L, ShelfCountCalculator.calculateTotalCapacity(6L, 4L))
        assertEquals(0L, ShelfCountCalculator.calculateTotalCapacity(0L, 4L))
    }

    // 13. Large shelf levels
    @Test
    fun testLargeShelfLevels() {
        val input = ShelfCountInput(
            shelfWidth = BigDecimal("120"),
            itemWidth = BigDecimal("30"),
            gap = BigDecimal.ZERO,
            shelfLevels = 1000L,
            unit = MeasurementUnit.CM
        )
        val result = ShelfCountCalculator.calculate(input)
        assertEquals(4L, result.itemsPerShelf)
        assertEquals(4000L, result.totalCapacity)
        assertFalse(result.isCapacityOverflow)
    }

    // 14. Decimal measurements
    @Test
    fun testDecimalMeasurements() {
        val input = ShelfCountInput(
            shelfWidth = BigDecimal("120.5"),
            itemWidth = BigDecimal("30.1"),
            gap = BigDecimal("0.5"),
            shelfLevels = 2L,
            unit = MeasurementUnit.CM
        )
        val result = ShelfCountCalculator.calculate(input)
        // 3 items = 3 * 30.1 + 2 * 0.5 = 90.3 + 1.0 = 91.3 <= 120.5
        // 4 items = 4 * 30.1 + 3 * 0.5 = 120.4 + 1.5 = 121.9 > 120.5
        // So exactly 3 items fit
        assertEquals(3L, result.itemsPerShelf)
        assertEquals(6L, result.totalCapacity)
    }

    // 15. Metric conversion (mm, cm, m)
    @Test
    fun testMetricConversion() {
        // 1.2 m shelf = 120 cm = 1200 mm
        // 0.3 m item = 30 cm = 300 mm
        val inputM = ShelfCountInput(
            shelfWidth = BigDecimal("1.2"),
            itemWidth = BigDecimal("0.3"),
            gap = BigDecimal.ZERO,
            shelfLevels = 1L,
            unit = MeasurementUnit.M
        )
        val resultM = ShelfCountCalculator.calculate(inputM)
        assertEquals(4L, resultM.itemsPerShelf)

        val inputMm = ShelfCountInput(
            shelfWidth = BigDecimal("1200"),
            itemWidth = BigDecimal("300"),
            gap = BigDecimal.ZERO,
            shelfLevels = 1L,
            unit = MeasurementUnit.MM
        )
        val resultMm = ShelfCountCalculator.calculate(inputMm)
        assertEquals(4L, resultMm.itemsPerShelf)
    }

    // 16. Imperial conversion (in, ft)
    @Test
    fun testImperialConversion() {
        // 4 ft shelf = 48 inches
        // 1 ft item = 12 inches
        val inputFt = ShelfCountInput(
            shelfWidth = BigDecimal("4"),
            itemWidth = BigDecimal("1"),
            gap = BigDecimal.ZERO,
            shelfLevels = 3L,
            unit = MeasurementUnit.FT
        )
        val resultFt = ShelfCountCalculator.calculate(inputFt)
        assertEquals(4L, resultFt.itemsPerShelf)
        assertEquals(12L, resultFt.totalCapacity)

        val inputIn = ShelfCountInput(
            shelfWidth = BigDecimal("48"),
            itemWidth = BigDecimal("12"),
            gap = BigDecimal.ZERO,
            shelfLevels = 3L,
            unit = MeasurementUnit.IN
        )
        val resultIn = ShelfCountCalculator.calculate(inputIn)
        assertEquals(4L, resultIn.itemsPerShelf)
    }

    // 17. Mixed measurement values through conversion
    @Test
    fun testUnitConversionFactors() {
        val cmValue = BigDecimal("10")
        val mmFromCm = MeasurementUnit.CM.toMm(cmValue)
        assertEquals(0, mmFromCm.compareTo(BigDecimal("100")))

        val inValue = BigDecimal("2")
        val mmFromIn = MeasurementUnit.IN.toMm(inValue)
        assertEquals(0, mmFromIn.compareTo(BigDecimal("50.8")))

        val ftValue = BigDecimal("1")
        val mmFromFt = MeasurementUnit.FT.toMm(ftValue)
        assertEquals(0, mmFromFt.compareTo(BigDecimal("304.8")))
    }

    // 18. Depth fit
    @Test
    fun testDepthFit() {
        val fits = ShelfCountCalculator.calculateDepthFit(
            shelfDepth = BigDecimal("30"),
            itemDepth = BigDecimal("20")
        )
        assertTrue(fits)
    }

    // 19. Depth failure
    @Test
    fun testDepthFailure() {
        val fits = ShelfCountCalculator.calculateDepthFit(
            shelfDepth = BigDecimal("20"),
            itemDepth = BigDecimal("25")
        )
        assertFalse(fits)
    }

    // 20. Zero gap formula
    @Test
    fun testZeroGapFormula() {
        val used = ShelfCountCalculator.calculateUsedWidth(4L, BigDecimal("30"), BigDecimal.ZERO)
        assertEquals(0, used.compareTo(BigDecimal("120")))
    }

    // 21. Gap formula
    @Test
    fun testGapFormula() {
        val used = ShelfCountCalculator.calculateUsedWidth(3L, BigDecimal("30"), BigDecimal("2"))
        // 3 * 30 + 2 * 2 = 94
        assertEquals(0, used.compareTo(BigDecimal("94")))
    }

    // 22. Maximum item calculation
    @Test
    fun testMaximumItemCalculation() {
        val n = ShelfCountCalculator.calculateItemsPerShelf(
            availableWidth = BigDecimal("100"),
            itemWidth = BigDecimal("30"),
            gap = BigDecimal("5")
        )
        assertEquals(3L, n)
    }

    // 23. N + 1 verification
    @Test
    fun testNPlusOneVerification() {
        // Shelf 120, Item 30, Gap 2
        val n = ShelfCountCalculator.calculateItemsPerShelf(
            availableWidth = BigDecimal("120"),
            itemWidth = BigDecimal("30"),
            gap = BigDecimal("2")
        )
        assertEquals(3L, n)

        val usedForN = ShelfCountCalculator.calculateUsedWidth(n, BigDecimal("30"), BigDecimal("2"))
        assertTrue(usedForN <= BigDecimal("120"))

        val usedForNPlus1 = ShelfCountCalculator.calculateUsedWidth(n + 1, BigDecimal("30"), BigDecimal("2"))
        assertTrue(usedForNPlus1 > BigDecimal("120"))
    }

    // 24. BigDecimal precision
    @Test
    fun testBigDecimalPrecision() {
        val input = ShelfCountInput(
            shelfWidth = BigDecimal("100.0001"),
            itemWidth = BigDecimal("33.3333"),
            gap = BigDecimal("0.0001"),
            shelfLevels = 1L,
            unit = MeasurementUnit.CM
        )
        val result = ShelfCountCalculator.calculate(input)
        assertEquals(3L, result.itemsPerShelf)
    }

    // 25. Overflow protection
    @Test
    fun testOverflowProtection() {
        var overflowCaught = false
        try {
            ShelfCountCalculator.calculateTotalCapacity(Long.MAX_VALUE / 2 + 1, 3L)
        } catch (_: ArithmeticException) {
            overflowCaught = true
        }
        assertTrue(overflowCaught)
    }
}
