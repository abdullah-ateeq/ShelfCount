package com.example.shelfcount.model

import java.math.BigDecimal

enum class ThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class HistorySortOption(val displayName: String) {
    RECENTLY_UPDATED("Recently Updated"),
    HIGHEST_CAPACITY("Highest Capacity"),
    HIGHEST_UTILIZATION("Highest Utilization"),
    ITEM_NAME_AZ("Item Name A–Z")
}

enum class HistoryFilterOption(val displayName: String) {
    ALL("All"),
    PERFECT_FIT("Perfect Fit"),
    SPACE_REMAINING("Space Remaining"),
    NO_ITEM_FIT("No Item Fit"),
    DEPTH_FIT("Depth Fit"),
    DEPTH_NOT_FIT("Depth Not Fit")
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultUnit: MeasurementUnit = MeasurementUnit.CM,
    val defaultShelfWidth: BigDecimal = BigDecimal("120"),
    val defaultItemWidth: BigDecimal = BigDecimal("30"),
    val defaultShelfLevels: Long = 4L,
    val defaultGap: BigDecimal = BigDecimal.ZERO,
    val showUtilization: Boolean = true,
    val showRemainingSpace: Boolean = true,
    val showUsedWidth: Boolean = true,
    val showFormula: Boolean = true,
    val showShelfVisualization: Boolean = true,
    val showDepthCheck: Boolean = false,
    val showItemName: Boolean = true,
    val showNotes: Boolean = true,
    val hapticFeedback: Boolean = true,
    val confirmDelete: Boolean = true,
    val historyLimit: Int = 100,
    val defaultHistorySort: HistorySortOption = HistorySortOption.RECENTLY_UPDATED
)
