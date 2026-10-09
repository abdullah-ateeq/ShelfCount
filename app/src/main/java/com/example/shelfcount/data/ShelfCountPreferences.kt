package com.example.shelfcount.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.shelfcount.model.AppSettings
import com.example.shelfcount.model.HistorySortOption
import com.example.shelfcount.model.MeasurementUnit
import com.example.shelfcount.model.SavedCalculation
import com.example.shelfcount.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import java.io.IOException
import java.math.BigDecimal

val Context.shelfCountDataStore: DataStore<Preferences> by preferencesDataStore(name = "shelf_count_preferences")

class ShelfCountPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        // AppSettings keys
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val KEY_DEFAULT_UNIT = stringPreferencesKey("default_unit")
        private val KEY_DEFAULT_SHELF_WIDTH = stringPreferencesKey("default_shelf_width")
        private val KEY_DEFAULT_ITEM_WIDTH = stringPreferencesKey("default_item_width")
        private val KEY_DEFAULT_SHELF_LEVELS = longPreferencesKey("default_shelf_levels")
        private val KEY_DEFAULT_GAP = stringPreferencesKey("default_gap")
        private val KEY_SHOW_UTILIZATION = booleanPreferencesKey("show_utilization")
        private val KEY_SHOW_REMAINING_SPACE = booleanPreferencesKey("show_remaining_space")
        private val KEY_SHOW_USED_WIDTH = booleanPreferencesKey("show_used_width")
        private val KEY_SHOW_FORMULA = booleanPreferencesKey("show_formula")
        private val KEY_SHOW_SHELF_VISUALIZATION = booleanPreferencesKey("show_shelf_visualization")
        private val KEY_SHOW_DEPTH_CHECK = booleanPreferencesKey("show_depth_check")
        private val KEY_SHOW_ITEM_NAME = booleanPreferencesKey("show_item_name")
        private val KEY_SHOW_NOTES = booleanPreferencesKey("show_notes")
        private val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        private val KEY_CONFIRM_DELETE = booleanPreferencesKey("confirm_delete")
        private val KEY_HISTORY_LIMIT = intPreferencesKey("history_limit")
        private val KEY_DEFAULT_HISTORY_SORT = stringPreferencesKey("default_history_sort")

        // History key
        private val KEY_HISTORY_JSON = stringPreferencesKey("history_json")
    }

    val settingsFlow: Flow<AppSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            AppSettings(
                themeMode = try {
                    ThemeMode.valueOf(prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name)
                } catch (_: Exception) {
                    ThemeMode.SYSTEM
                },
                dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: true,
                defaultUnit = try {
                    MeasurementUnit.valueOf(prefs[KEY_DEFAULT_UNIT] ?: MeasurementUnit.CM.name)
                } catch (_: Exception) {
                    MeasurementUnit.CM
                },
                defaultShelfWidth = BigDecimal(prefs[KEY_DEFAULT_SHELF_WIDTH] ?: "120"),
                defaultItemWidth = BigDecimal(prefs[KEY_DEFAULT_ITEM_WIDTH] ?: "30"),
                defaultShelfLevels = prefs[KEY_DEFAULT_SHELF_LEVELS] ?: 4L,
                defaultGap = BigDecimal(prefs[KEY_DEFAULT_GAP] ?: "0"),
                showUtilization = prefs[KEY_SHOW_UTILIZATION] ?: true,
                showRemainingSpace = prefs[KEY_SHOW_REMAINING_SPACE] ?: true,
                showUsedWidth = prefs[KEY_SHOW_USED_WIDTH] ?: true,
                showFormula = prefs[KEY_SHOW_FORMULA] ?: true,
                showShelfVisualization = prefs[KEY_SHOW_SHELF_VISUALIZATION] ?: true,
                showDepthCheck = prefs[KEY_SHOW_DEPTH_CHECK] ?: false,
                showItemName = prefs[KEY_SHOW_ITEM_NAME] ?: true,
                showNotes = prefs[KEY_SHOW_NOTES] ?: true,
                hapticFeedback = prefs[KEY_HAPTIC_FEEDBACK] ?: true,
                confirmDelete = prefs[KEY_CONFIRM_DELETE] ?: true,
                historyLimit = prefs[KEY_HISTORY_LIMIT] ?: 100,
                defaultHistorySort = try {
                    HistorySortOption.valueOf(prefs[KEY_DEFAULT_HISTORY_SORT] ?: HistorySortOption.RECENTLY_UPDATED.name)
                } catch (_: Exception) {
                    HistorySortOption.RECENTLY_UPDATED
                }
            )
        }

    val historyFlow: Flow<List<SavedCalculation>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val jsonString = prefs[KEY_HISTORY_JSON] ?: "[]"
            parseHistoryJson(jsonString)
        }

    suspend fun updateSettings(settings: AppSettings) {
        dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = settings.themeMode.name
            prefs[KEY_DYNAMIC_COLOR] = settings.dynamicColor
            prefs[KEY_DEFAULT_UNIT] = settings.defaultUnit.name
            prefs[KEY_DEFAULT_SHELF_WIDTH] = settings.defaultShelfWidth.toPlainString()
            prefs[KEY_DEFAULT_ITEM_WIDTH] = settings.defaultItemWidth.toPlainString()
            prefs[KEY_DEFAULT_SHELF_LEVELS] = settings.defaultShelfLevels
            prefs[KEY_DEFAULT_GAP] = settings.defaultGap.toPlainString()
            prefs[KEY_SHOW_UTILIZATION] = settings.showUtilization
            prefs[KEY_SHOW_REMAINING_SPACE] = settings.showRemainingSpace
            prefs[KEY_SHOW_USED_WIDTH] = settings.showUsedWidth
            prefs[KEY_SHOW_FORMULA] = settings.showFormula
            prefs[KEY_SHOW_SHELF_VISUALIZATION] = settings.showShelfVisualization
            prefs[KEY_SHOW_DEPTH_CHECK] = settings.showDepthCheck
            prefs[KEY_SHOW_ITEM_NAME] = settings.showItemName
            prefs[KEY_SHOW_NOTES] = settings.showNotes
            prefs[KEY_HAPTIC_FEEDBACK] = settings.hapticFeedback
            prefs[KEY_CONFIRM_DELETE] = settings.confirmDelete
            prefs[KEY_HISTORY_LIMIT] = settings.historyLimit
            prefs[KEY_DEFAULT_HISTORY_SORT] = settings.defaultHistorySort.name
        }
    }

    suspend fun resetSettings() {
        val defaultSettings = AppSettings()
        updateSettings(defaultSettings)
    }

    /**
     * Saves a calculation.
     * Enforces the maximum history limit:
     * - If count reaches the limit (e.g. 100 or 50):
     *   Find the oldest non-favorite item and remove it.
     *   If all items are favorites, do not remove any and return Result.failure with message.
     */
    suspend fun saveCalculation(item: SavedCalculation, historyLimit: Int = 100): Result<Unit> {
        var failureMessage: String? = null

        dataStore.edit { prefs ->
            val jsonString = prefs[KEY_HISTORY_JSON] ?: "[]"
            val currentList = parseHistoryJson(jsonString).toMutableList()

            // If an item with same ID exists, update it
            val existingIndex = currentList.indexOfFirst { it.id == item.id }
            if (existingIndex >= 0) {
                currentList[existingIndex] = item
            } else {
                if (currentList.size >= historyLimit) {
                    // Try to find the oldest non-favorite item
                    val oldestNonFavIndex = currentList
                        .mapIndexed { index, calc -> index to calc }
                        .filter { !it.second.isFavorite }
                        .minByOrNull { it.second.timestamp }
                        ?.first

                    if (oldestNonFavIndex != null) {
                        currentList.removeAt(oldestNonFavIndex)
                        currentList.add(0, item)
                    } else {
                        // All records are favorites
                        failureMessage = "History limit reached. Remove an item before saving."
                        return@edit
                    }
                } else {
                    currentList.add(0, item)
                }
            }

            prefs[KEY_HISTORY_JSON] = serializeHistory(currentList)
        }

        return if (failureMessage != null) {
            Result.failure(IllegalStateException(failureMessage))
        } else {
            Result.success(Unit)
        }
    }

    suspend fun toggleFavorite(id: String) {
        dataStore.edit { prefs ->
            val jsonString = prefs[KEY_HISTORY_JSON] ?: "[]"
            val currentList = parseHistoryJson(jsonString).map { calc ->
                if (calc.id == id) calc.copy(isFavorite = !calc.isFavorite) else calc
            }
            prefs[KEY_HISTORY_JSON] = serializeHistory(currentList)
        }
    }

    suspend fun duplicateCalculation(id: String, historyLimit: Int = 100): Result<Unit> {
        var failureMessage: String? = null
        dataStore.edit { prefs ->
            val jsonString = prefs[KEY_HISTORY_JSON] ?: "[]"
            val currentList = parseHistoryJson(jsonString).toMutableList()
            val original = currentList.find { it.id == id } ?: return@edit
            val duplicated = original.duplicate()

            if (currentList.size >= historyLimit) {
                val oldestNonFavIndex = currentList
                    .mapIndexed { index, calc -> index to calc }
                    .filter { !it.second.isFavorite }
                    .minByOrNull { it.second.timestamp }
                    ?.first

                if (oldestNonFavIndex != null) {
                    currentList.removeAt(oldestNonFavIndex)
                    currentList.add(0, duplicated)
                } else {
                    failureMessage = "History limit reached. Remove an item before saving."
                    return@edit
                }
            } else {
                currentList.add(0, duplicated)
            }

            prefs[KEY_HISTORY_JSON] = serializeHistory(currentList)
        }

        return if (failureMessage != null) {
            Result.failure(IllegalStateException(failureMessage))
        } else {
            Result.success(Unit)
        }
    }

    suspend fun deleteCalculation(id: String) {
        dataStore.edit { prefs ->
            val jsonString = prefs[KEY_HISTORY_JSON] ?: "[]"
            val currentList = parseHistoryJson(jsonString).filterNot { it.id == id }
            prefs[KEY_HISTORY_JSON] = serializeHistory(currentList)
        }
    }

    suspend fun clearHistory() {
        dataStore.edit { prefs ->
            prefs[KEY_HISTORY_JSON] = "[]"
        }
    }

    private fun parseHistoryJson(jsonString: String): List<SavedCalculation> {
        val list = mutableListOf<SavedCalculation>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(SavedCalculation.fromJson(obj))
            }
        } catch (_: Exception) {
            // Safe fallback on corrupted JSON
        }
        return list
    }

    private fun serializeHistory(list: List<SavedCalculation>): String {
        val array = JSONArray()
        for (item in list) {
            array.put(item.toJson())
        }
        return array.toString()
    }
}
