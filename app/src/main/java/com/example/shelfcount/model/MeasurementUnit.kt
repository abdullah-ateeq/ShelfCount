package com.example.shelfcount.model

import java.math.BigDecimal
import java.math.RoundingMode

enum class MeasurementUnit(
    val symbol: String,
    val displayName: String,
    val toMmFactor: BigDecimal
) {
    MM("mm", "Millimeters (mm)", BigDecimal("1")),
    CM("cm", "Centimeters (cm)", BigDecimal("10")),
    M("m", "Meters (m)", BigDecimal("1000")),
    IN("in", "Inches (in)", BigDecimal("25.4")),
    FT("ft", "Feet (ft)", BigDecimal("304.8"));

    /**
     * Converts a value in this unit to base unit (millimeters).
     */
    fun toMm(value: BigDecimal): BigDecimal {
        return value.multiply(toMmFactor)
    }

    /**
     * Converts a value in millimeters to this unit.
     */
    fun fromMm(valueInMm: BigDecimal): BigDecimal {
        return valueInMm.divide(toMmFactor, 10, RoundingMode.HALF_UP)
    }

    companion object {
        fun fromSymbol(symbol: String): MeasurementUnit {
            return entries.firstOrNull { it.symbol.equals(symbol, ignoreCase = true) } ?: CM
        }
    }
}
