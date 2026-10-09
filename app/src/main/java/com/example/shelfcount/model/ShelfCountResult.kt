package com.example.shelfcount.model

import java.math.BigDecimal

data class ShelfCountResult(
    val availableWidth: BigDecimal,
    val itemsPerShelf: Long,
    val totalCapacity: Long,
    val usedWidth: BigDecimal,
    val remainingWidth: BigDecimal,
    val utilizationPercent: BigDecimal,
    val unusedPercent: BigDecimal,
    val depthFits: Boolean? = null,
    val fitStatus: ShelfFitStatus,
    val isCapacityOverflow: Boolean = false,
    val formulaSteps: List<String> = emptyList(),
    val errorMessage: String? = null
)
