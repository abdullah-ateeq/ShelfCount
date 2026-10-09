package com.example.shelfcount.calculator

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object MeasurementFormatter {

    /**
     * Formats a BigDecimal measurement cleanly:
     * - Strips unnecessary trailing zeroes: "120.00" -> "120", "120.50" -> "120.5"
     * - Uses up to maxDecimals decimal places
     * - Never uses scientific notation (e.g. 1E+2)
     */
    fun formatMeasurement(value: BigDecimal, maxDecimals: Int = 2): String {
        val rounded = value.setScale(maxDecimals, RoundingMode.HALF_UP).stripTrailingZeros()
        // If value was effectively 0, stripTrailingZeros() might have scale < 0 or plain "0"
        return if (rounded.compareTo(BigDecimal.ZERO) == 0) {
            "0"
        } else {
            val symbols = DecimalFormatSymbols(Locale.US)
            val pattern = if (maxDecimals > 0) "#,##0." + "#".repeat(maxDecimals) else "#,##0"
            val df = DecimalFormat(pattern, symbols)
            df.format(rounded)
        }
    }

    /**
     * Formats a measurement with its unit symbol: e.g. "120 cm"
     */
    fun formatWithUnit(value: BigDecimal, unitSymbol: String, maxDecimals: Int = 2): String {
        return "${formatMeasurement(value, maxDecimals)} $unitSymbol"
    }

    /**
     * Formats a percentage: e.g. "78.33%" or "100%"
     */
    fun formatPercent(value: BigDecimal): String {
        val rounded = value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros()
        return if (rounded.compareTo(BigDecimal.ZERO) == 0) {
            "0%"
        } else {
            val symbols = DecimalFormatSymbols(Locale.US)
            val df = DecimalFormat("#,##0.##", symbols)
            "${df.format(rounded)}%"
        }
    }

    /**
     * Formats count with label: e.g. "12 items" or "1 item"
     */
    fun formatItemCount(count: Long): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#,###", symbols)
        val formatted = df.format(count)
        return if (count == 1L) "$formatted item" else "$formatted items"
    }
}
