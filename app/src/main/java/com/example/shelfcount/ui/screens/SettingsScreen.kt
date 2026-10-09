package com.example.shelfcount.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shelfcount.calculator.MeasurementFormatter
import com.example.shelfcount.model.HistorySortOption
import com.example.shelfcount.model.MeasurementUnit
import com.example.shelfcount.model.ThemeMode
import com.example.shelfcount.viewmodel.ShelfCountViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ShelfCountViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val scrollState = rememberScrollState()

    var showResetSettingsDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Column {
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Configure app defaults and calculation preferences.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Section: Appearance
        SettingsSectionCard(title = "APPEARANCE") {
            // Theme Mode
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { viewModel.updateSettings(settings.copy(themeMode = mode)) },
                            label = { Text(mode.displayName, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Dynamic Color Toggle
            SettingsToggleRow(
                title = "Dynamic Color",
                subtitle = "Apply wallpaper colors (Android 12+)",
                checked = settings.dynamicColor,
                onCheckedChange = { viewModel.updateSettings(settings.copy(dynamicColor = it)) }
            )
        }

        // Section: Defaults
        SettingsSectionCard(title = "MEASUREMENT DEFAULTS") {
            // Default Unit
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Default Unit",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MeasurementUnit.entries.forEach { unit ->
                        FilterChip(
                            selected = settings.defaultUnit == unit,
                            onClick = { viewModel.updateSettings(settings.copy(defaultUnit = unit)) },
                            label = { Text(unit.symbol, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }

            // Default Shelf Width & Item Width
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = MeasurementFormatter.formatMeasurement(settings.defaultShelfWidth),
                    onValueChange = {
                        val v = it.toBigDecimalOrNull()
                        if (v != null && v > BigDecimal.ZERO) {
                            viewModel.updateSettings(settings.copy(defaultShelfWidth = v))
                        }
                    },
                    label = { Text("Default Shelf Width") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = MeasurementFormatter.formatMeasurement(settings.defaultItemWidth),
                    onValueChange = {
                        val v = it.toBigDecimalOrNull()
                        if (v != null && v > BigDecimal.ZERO) {
                            viewModel.updateSettings(settings.copy(defaultItemWidth = v))
                        }
                    },
                    label = { Text("Default Item Width") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Default Shelf Levels & Default Gap
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = settings.defaultShelfLevels.toString(),
                    onValueChange = {
                        val v = it.toLongOrNull()
                        if (v != null && v in 1..1000) {
                            viewModel.updateSettings(settings.copy(defaultShelfLevels = v))
                        }
                    },
                    label = { Text("Default Shelf Levels") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = MeasurementFormatter.formatMeasurement(settings.defaultGap),
                    onValueChange = {
                        val v = it.toBigDecimalOrNull()
                        if (v != null && v >= BigDecimal.ZERO) {
                            viewModel.updateSettings(settings.copy(defaultGap = v))
                        }
                    },
                    label = { Text("Default Gap") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Section: Display Options
        SettingsSectionCard(title = "DISPLAY PREFERENCES") {
            SettingsToggleRow(
                title = "Show Utilization",
                subtitle = "Display shelf utilization percentage",
                checked = settings.showUtilization,
                onCheckedChange = { viewModel.updateSettings(settings.copy(showUtilization = it)) }
            )

            SettingsToggleRow(
                title = "Show Remaining Space",
                subtitle = "Display unused width after item fit",
                checked = settings.showRemainingSpace,
                onCheckedChange = { viewModel.updateSettings(settings.copy(showRemainingSpace = it)) }
            )

            SettingsToggleRow(
                title = "Show Used Width",
                subtitle = "Display occupied shelf space",
                checked = settings.showUsedWidth,
                onCheckedChange = { viewModel.updateSettings(settings.copy(showUsedWidth = it)) }
            )

            SettingsToggleRow(
                title = "Show Formula Explanation",
                subtitle = "Collapsible mathematical breakdown",
                checked = settings.showFormula,
                onCheckedChange = { viewModel.updateSettings(settings.copy(showFormula = it)) }
            )

            SettingsToggleRow(
                title = "Show Shelf Visualization",
                subtitle = "Simplified visual shelf levels & item blocks",
                checked = settings.showShelfVisualization,
                onCheckedChange = { viewModel.updateSettings(settings.copy(showShelfVisualization = it)) }
            )

            SettingsToggleRow(
                title = "Show Item Name Field",
                subtitle = "Optional label for shelf calculation",
                checked = settings.showItemName,
                onCheckedChange = { viewModel.updateSettings(settings.copy(showItemName = it)) }
            )

            SettingsToggleRow(
                title = "Show Notes Field",
                subtitle = "Optional description & placement notes",
                checked = settings.showNotes,
                onCheckedChange = { viewModel.updateSettings(settings.copy(showNotes = it)) }
            )
        }

        // Section: Interaction & History
        SettingsSectionCard(title = "INTERACTION & HISTORY") {
            SettingsToggleRow(
                title = "Haptic Feedback",
                subtitle = "Vibrate lightly on save, copy, and favorite",
                checked = settings.hapticFeedback,
                onCheckedChange = { viewModel.updateSettings(settings.copy(hapticFeedback = it)) }
            )

            SettingsToggleRow(
                title = "Confirm Deletions",
                subtitle = "Prompt before deleting saved calculation",
                checked = settings.confirmDelete,
                onCheckedChange = { viewModel.updateSettings(settings.copy(confirmDelete = it)) }
            )

            // History Limit
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "History Limit",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = settings.historyLimit == 50,
                        onClick = { viewModel.updateSettings(settings.copy(historyLimit = 50)) },
                        label = { Text("50 Calculations") }
                    )
                    FilterChip(
                        selected = settings.historyLimit == 100,
                        onClick = { viewModel.updateSettings(settings.copy(historyLimit = 100)) },
                        label = { Text("100 Calculations") }
                    )
                }
            }

            // Default History Sort
            var sortMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = sortMenuExpanded,
                onExpandedChange = { sortMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = settings.defaultHistorySort.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Default History Sort") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {
                    HistorySortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.displayName) },
                            onClick = {
                                viewModel.updateSettings(settings.copy(defaultHistorySort = option))
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Actions: Reset Settings, Clear History
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { showResetSettingsDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Reset Settings to Default")
            }

            OutlinedButton(
                onClick = { showClearHistoryDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Clear History")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Reset Confirmation
    if (showResetSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showResetSettingsDialog = false },
            title = { Text("Reset Settings") },
            text = { Text("Reset all settings to default values?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetSettings()
                        showResetSettingsDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear History Confirmation
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear All History") },
            text = { Text("Delete all saved calculations? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
