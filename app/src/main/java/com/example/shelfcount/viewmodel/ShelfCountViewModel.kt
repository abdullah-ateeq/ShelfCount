package com.example.shelfcount.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shelfcount.calculator.MeasurementFormatter
import com.example.shelfcount.calculator.ShelfCountCalculator
import com.example.shelfcount.data.ShelfCountPreferences
import com.example.shelfcount.model.AppSettings
import com.example.shelfcount.model.HistoryFilterOption
import com.example.shelfcount.model.HistorySortOption
import com.example.shelfcount.model.MeasurementUnit
import com.example.shelfcount.model.SavedCalculation
import com.example.shelfcount.model.ShelfCountInput
import com.example.shelfcount.model.ShelfCountResult
import com.example.shelfcount.model.ShelfFitStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

class ShelfCountViewModel(
    private val preferences: ShelfCountPreferences
) : ViewModel() {

    private val _calculatorState = MutableStateFlow(CalculatorUiState())
    val calculatorState: StateFlow<CalculatorUiState> = _calculatorState.asStateFlow()

    val settings: StateFlow<AppSettings> = preferences.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private val _historyFilter = MutableStateFlow(HistoryFilterOption.ALL)
    val historyFilter: StateFlow<HistoryFilterOption> = _historyFilter.asStateFlow()

    private val _historySort = MutableStateFlow(HistorySortOption.RECENTLY_UPDATED)
    val historySort: StateFlow<HistorySortOption> = _historySort.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    private val _navigationRequest = MutableSharedFlow<String>()
    val navigationRequest: SharedFlow<String> = _navigationRequest.asSharedFlow()

    val historyList: StateFlow<List<SavedCalculation>> = combine(
        preferences.historyFlow,
        _historySearchQuery,
        _historyFilter,
        _historySort
    ) { allHistory, query, filter, sort ->
        var list = allHistory

        // 1. Search filtering (case-insensitive by itemName and notes)
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.itemName.lowercase().contains(q) || it.notes.lowercase().contains(q)
            }
        }

        // 2. Filter chips
        list = when (filter) {
            HistoryFilterOption.ALL -> list
            HistoryFilterOption.PERFECT_FIT -> list.filter { it.fitStatus == ShelfFitStatus.PERFECT_FIT }
            HistoryFilterOption.SPACE_REMAINING -> list.filter { it.fitStatus == ShelfFitStatus.SPACE_REMAINING }
            HistoryFilterOption.NO_ITEM_FIT -> list.filter { it.fitStatus == ShelfFitStatus.NO_ITEM_FIT }
            HistoryFilterOption.DEPTH_FIT -> list.filter { it.depthFits == true }
            HistoryFilterOption.DEPTH_NOT_FIT -> list.filter { it.depthFits == false }
        }

        // 3. Sorting: Favorites ALWAYS appear first, then by selected sort option
        list.sortedWith(
            compareByDescending<SavedCalculation> { it.isFavorite }
                .thenComparing { a, b ->
                    when (sort) {
                        HistorySortOption.RECENTLY_UPDATED -> b.timestamp.compareTo(a.timestamp)
                        HistorySortOption.HIGHEST_CAPACITY -> b.totalCapacity.compareTo(a.totalCapacity)
                        HistorySortOption.HIGHEST_UTILIZATION -> b.utilizationPercent.compareTo(a.utilizationPercent)
                        HistorySortOption.ITEM_NAME_AZ -> a.itemName.lowercase().compareTo(b.itemName.lowercase())
                    }
                }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Initialize settings default values into calculator if needed
        viewModelScope.launch {
            settings.collect { appSettings ->
                _historySort.value = appSettings.defaultHistorySort
            }
        }
        recalculate()
    }

    // Input updaters
    fun setItemName(name: String) {
        if (name.length <= 60) {
            _calculatorState.value = _calculatorState.value.copy(itemName = name)
        }
    }

    fun setNotes(notes: String) {
        if (notes.length <= 300) {
            _calculatorState.value = _calculatorState.value.copy(notes = notes)
        }
    }

    fun setUnit(unit: MeasurementUnit) {
        _calculatorState.value = _calculatorState.value.copy(unit = unit)
        recalculate()
    }

    fun setShelfWidth(text: String) {
        _calculatorState.value = _calculatorState.value.copy(shelfWidthText = text)
        recalculate()
    }

    fun setItemWidth(text: String) {
        _calculatorState.value = _calculatorState.value.copy(itemWidthText = text)
        recalculate()
    }

    fun setGap(text: String) {
        _calculatorState.value = _calculatorState.value.copy(gapText = text)
        recalculate()
    }

    fun setShelfLevels(text: String) {
        _calculatorState.value = _calculatorState.value.copy(shelfLevelsText = text)
        recalculate()
    }

    fun setEdgeMarginsEnabled(enabled: Boolean) {
        _calculatorState.value = _calculatorState.value.copy(
            edgeMarginsEnabled = enabled,
            leftMarginText = if (enabled && _calculatorState.value.leftMarginText.isBlank()) "0" else _calculatorState.value.leftMarginText,
            rightMarginText = if (enabled && _calculatorState.value.rightMarginText.isBlank()) "0" else _calculatorState.value.rightMarginText
        )
        recalculate()
    }

    fun setLeftMargin(text: String) {
        _calculatorState.value = _calculatorState.value.copy(leftMarginText = text)
        recalculate()
    }

    fun setRightMargin(text: String) {
        _calculatorState.value = _calculatorState.value.copy(rightMarginText = text)
        recalculate()
    }

    fun setDepthCheckEnabled(enabled: Boolean) {
        _calculatorState.value = _calculatorState.value.copy(depthCheckEnabled = enabled)
        recalculate()
    }

    fun setShelfDepth(text: String) {
        _calculatorState.value = _calculatorState.value.copy(shelfDepthText = text)
        recalculate()
    }

    fun setItemDepth(text: String) {
        _calculatorState.value = _calculatorState.value.copy(itemDepthText = text)
        recalculate()
    }

    fun setDepthMargin(text: String) {
        _calculatorState.value = _calculatorState.value.copy(depthMarginText = text)
        recalculate()
    }

    fun applyPreset(presetType: String) {
        when (presetType) {
            "BOOKSHELF" -> {
                _calculatorState.value = _calculatorState.value.copy(
                    itemName = "Bookshelf",
                    shelfWidthText = "100",
                    itemWidthText = "20",
                    shelfLevelsText = "5",
                    gapText = "0",
                    edgeMarginsEnabled = false,
                    depthCheckEnabled = false
                )
            }
            "STORAGE_BOXES" -> {
                _calculatorState.value = _calculatorState.value.copy(
                    itemName = "Storage Boxes",
                    shelfWidthText = "120",
                    itemWidthText = "30",
                    shelfLevelsText = "4",
                    gapText = "2",
                    edgeMarginsEnabled = false,
                    depthCheckEnabled = false
                )
            }
            "DISPLAY_PRODUCTS" -> {
                _calculatorState.value = _calculatorState.value.copy(
                    itemName = "Display Products",
                    shelfWidthText = "150",
                    itemWidthText = "25",
                    shelfLevelsText = "6",
                    gapText = "5",
                    edgeMarginsEnabled = false,
                    depthCheckEnabled = false
                )
            }
        }
        recalculate()
    }

    fun resetCalculator() {
        val appSettings = settings.value
        _calculatorState.value = CalculatorUiState(
            itemName = "",
            notes = "",
            unit = appSettings.defaultUnit,
            shelfWidthText = MeasurementFormatter.formatMeasurement(appSettings.defaultShelfWidth),
            itemWidthText = MeasurementFormatter.formatMeasurement(appSettings.defaultItemWidth),
            shelfLevelsText = appSettings.defaultShelfLevels.toString(),
            gapText = MeasurementFormatter.formatMeasurement(appSettings.defaultGap),
            edgeMarginsEnabled = false,
            leftMarginText = "0",
            rightMarginText = "0",
            depthCheckEnabled = false,
            shelfDepthText = "30",
            itemDepthText = "20",
            depthMarginText = "0"
        )
        recalculate()
    }

    private fun recalculate() {
        val state = _calculatorState.value

        // Validate individual inputs
        var sWidthError: String? = null
        var iWidthError: String? = null
        var gapError: String? = null
        var levelsError: String? = null
        var marginsError: String? = null
        var depthError: String? = null

        val sWidth = state.shelfWidthText.toBigDecimalOrNull()
        if (sWidth == null || sWidth <= BigDecimal.ZERO) {
            sWidthError = "Shelf width must be greater than zero."
        } else if (sWidth > BigDecimal("999999.99")) {
            sWidthError = "Shelf width exceeds maximum limit (999,999.99)."
        }

        val iWidth = state.itemWidthText.toBigDecimalOrNull()
        if (iWidth == null || iWidth <= BigDecimal.ZERO) {
            iWidthError = "Item width must be greater than zero."
        } else if (iWidth > BigDecimal("999999.99")) {
            iWidthError = "Item width exceeds maximum limit (999,999.99)."
        }

        val gap = state.gapText.toBigDecimalOrNull()
        if (gap == null || gap < BigDecimal.ZERO) {
            gapError = "Gap cannot be negative."
        } else if (gap > BigDecimal("999999.99")) {
            gapError = "Gap exceeds maximum limit (999,999.99)."
        }

        val levels = state.shelfLevelsText.toLongOrNull()
        if (levels == null || levels < 1L || levels > 1000L) {
            levelsError = "Levels must be a whole number between 1 and 1,000."
        }

        val lMargin = state.leftMarginText.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val rMargin = state.rightMarginText.toBigDecimalOrNull() ?: BigDecimal.ZERO
        if (state.edgeMarginsEnabled) {
            if (lMargin < BigDecimal.ZERO || rMargin < BigDecimal.ZERO) {
                marginsError = "Margins cannot be negative."
            } else if (sWidth != null && sWidth > BigDecimal.ZERO) {
                val halfWidth = sWidth.multiply(BigDecimal("0.5"))
                if (lMargin > halfWidth || rMargin > halfWidth) {
                    marginsError = "Each margin can be at most 50% of shelf width."
                } else if (sWidth.subtract(lMargin).subtract(rMargin) <= BigDecimal.ZERO) {
                    marginsError = "Available width must be greater than zero."
                }
            }
        }

        val sDepth = state.shelfDepthText.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val iDepth = state.itemDepthText.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val dMargin = state.depthMarginText.toBigDecimalOrNull() ?: BigDecimal.ZERO
        if (state.depthCheckEnabled) {
            if (sDepth <= BigDecimal.ZERO || iDepth <= BigDecimal.ZERO) {
                depthError = "Depths must be greater than zero."
            } else if (dMargin < BigDecimal.ZERO) {
                depthError = "Depth margin cannot be negative."
            }
        }

        val input = ShelfCountInput(
            itemName = state.itemName,
            notes = state.notes,
            unit = state.unit,
            shelfWidth = sWidth ?: BigDecimal.ZERO,
            itemWidth = iWidth ?: BigDecimal.ZERO,
            gap = gap ?: BigDecimal.ZERO,
            shelfLevels = levels ?: 1L,
            edgeMarginsEnabled = state.edgeMarginsEnabled,
            leftMargin = lMargin,
            rightMargin = rMargin,
            depthCheckEnabled = state.depthCheckEnabled,
            shelfDepth = sDepth,
            itemDepth = iDepth,
            depthMargin = dMargin
        )

        val result = if (sWidthError != null || iWidthError != null || gapError != null || levelsError != null || marginsError != null) {
            ShelfCountResult(
                availableWidth = BigDecimal.ZERO,
                itemsPerShelf = 0L,
                totalCapacity = 0L,
                usedWidth = BigDecimal.ZERO,
                remainingWidth = BigDecimal.ZERO,
                utilizationPercent = BigDecimal.ZERO,
                unusedPercent = BigDecimal("100"),
                depthFits = if (state.depthCheckEnabled) false else null,
                fitStatus = ShelfFitStatus.NO_ITEM_FIT,
                errorMessage = sWidthError ?: iWidthError ?: gapError ?: levelsError ?: marginsError
            )
        } else {
            ShelfCountCalculator.calculate(input)
        }

        _calculatorState.value = state.copy(
            shelfWidthError = sWidthError,
            itemWidthError = iWidthError,
            gapError = gapError,
            levelsError = levelsError,
            marginsError = marginsError,
            depthError = depthError,
            result = result
        )
    }

    // Save calculation
    fun saveCurrentCalculation() {
        viewModelScope.launch {
            val calc = _calculatorState.value.toSavedCalculation()
            val limit = settings.value.historyLimit
            val result = preferences.saveCalculation(calc, limit)
            result.onSuccess {
                _snackbarMessage.emit("Calculation saved")
            }.onFailure { e ->
                _snackbarMessage.emit(e.message ?: "Failed to save calculation")
            }
        }
    }

    // History interactions
    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    fun setHistoryFilter(filter: HistoryFilterOption) {
        _historyFilter.value = filter
    }

    fun setHistorySort(sort: HistorySortOption) {
        _historySort.value = sort
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            preferences.toggleFavorite(id)
        }
    }

    fun duplicateCalculation(id: String) {
        viewModelScope.launch {
            val limit = settings.value.historyLimit
            val result = preferences.duplicateCalculation(id, limit)
            result.onSuccess {
                _snackbarMessage.emit("Duplicated calculation")
            }.onFailure { e ->
                _snackbarMessage.emit(e.message ?: "History limit reached. Remove an item before saving.")
            }
        }
    }

    fun deleteCalculation(id: String) {
        viewModelScope.launch {
            preferences.deleteCalculation(id)
            _snackbarMessage.emit("Calculation deleted")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            preferences.clearHistory()
            _snackbarMessage.emit("History cleared")
        }
    }

    fun openCalculation(calc: SavedCalculation) {
        _calculatorState.value = CalculatorUiState(
            itemName = calc.itemName,
            notes = calc.notes,
            unit = calc.unit,
            shelfWidthText = MeasurementFormatter.formatMeasurement(calc.shelfWidth),
            itemWidthText = MeasurementFormatter.formatMeasurement(calc.itemWidth),
            gapText = MeasurementFormatter.formatMeasurement(calc.gap),
            shelfLevelsText = calc.shelfLevels.toString(),
            edgeMarginsEnabled = calc.edgeMarginsEnabled,
            leftMarginText = MeasurementFormatter.formatMeasurement(calc.leftMargin),
            rightMarginText = MeasurementFormatter.formatMeasurement(calc.rightMargin),
            depthCheckEnabled = calc.depthCheckEnabled,
            shelfDepthText = MeasurementFormatter.formatMeasurement(calc.shelfDepth),
            itemDepthText = MeasurementFormatter.formatMeasurement(calc.itemDepth),
            depthMarginText = MeasurementFormatter.formatMeasurement(calc.depthMargin)
        )
        recalculate()
        viewModelScope.launch {
            _navigationRequest.emit("calculator")
        }
    }

    // Settings
    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            preferences.updateSettings(newSettings)
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            preferences.resetSettings()
            _snackbarMessage.emit("Settings reset to default")
        }
    }

    fun notifyMessage(message: String) {
        viewModelScope.launch {
            _snackbarMessage.emit(message)
        }
    }
}
