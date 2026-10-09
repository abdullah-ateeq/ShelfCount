package com.example.shelfcount.model

import java.math.BigDecimal

data class ShelfCountInput(
    val itemName: String = "",
    val notes: String = "",
    val unit: MeasurementUnit = MeasurementUnit.CM,
    val shelfWidth: BigDecimal = BigDecimal("120"),
    val itemWidth: BigDecimal = BigDecimal("30"),
    val gap: BigDecimal = BigDecimal.ZERO,
    val shelfLevels: Long = 4L,
    val edgeMarginsEnabled: Boolean = false,
    val leftMargin: BigDecimal = BigDecimal.ZERO,
    val rightMargin: BigDecimal = BigDecimal.ZERO,
    val depthCheckEnabled: Boolean = false,
    val shelfDepth: BigDecimal = BigDecimal("30"),
    val itemDepth: BigDecimal = BigDecimal("20"),
    val depthMargin: BigDecimal = BigDecimal.ZERO
) {
    fun resolvedItemName(): String {
        return itemName.trim().ifEmpty { "Untitled Shelf" }
    }
}
