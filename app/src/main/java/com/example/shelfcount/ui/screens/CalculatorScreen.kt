package com.example.shelfcount.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shelfcount.model.MeasurementUnit
import com.example.shelfcount.ui.components.FormulaSection
import com.example.shelfcount.ui.components.PrimaryResultCard
import com.example.shelfcount.ui.components.SecondaryResultCard
import com.example.shelfcount.ui.components.ShelfVisualization
import com.example.shelfcount.viewmodel.ShelfCountViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: ShelfCountViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.calculatorState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 600.dp
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column {
                Text(
                    text = "SHELFCOUNT",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Calculate how many items fit across shelf levels.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Preset Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Presets:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                FilterChip(
                    selected = false,
                    onClick = {
                        if (settings.hapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.applyPreset("BOOKSHELF")
                    },
                    label = { Text("Bookshelf", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = false,
                    onClick = {
                        if (settings.hapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.applyPreset("STORAGE_BOXES")
                    },
                    label = { Text("Storage", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = false,
                    onClick = {
                        if (settings.hapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.applyPreset("DISPLAY_PRODUCTS")
                    },
                    label = { Text("Display", fontSize = 12.sp) }
                )
            }

            // Unit Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "UNIT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MeasurementUnit.entries.forEach { unit ->
                            FilterChip(
                                selected = state.unit == unit,
                                onClick = {
                                    if (settings.hapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setUnit(unit)
                                },
                                label = { Text(unit.symbol, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }

            if (isWide) {
                // Two-column layout for tablets / landscape
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column: Inputs
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InputSections(
                            state = state,
                            settings = settings,
                            viewModel = viewModel
                        )
                    }

                    // Right Column: Results & Visualization
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ResultSections(
                            state = state,
                            settings = settings,
                            viewModel = viewModel,
                            context = context,
                            haptic = haptic
                        )
                    }
                }
            } else {
                // Single column layout for phone portrait
                InputSections(
                    state = state,
                    settings = settings,
                    viewModel = viewModel
                )

                ResultSections(
                    state = state,
                    settings = settings,
                    viewModel = viewModel,
                    context = context,
                    haptic = haptic
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InputSections(
    state: com.example.shelfcount.viewmodel.CalculatorUiState,
    settings: com.example.shelfcount.model.AppSettings,
    viewModel: ShelfCountViewModel
) {
    val unitSym = state.unit.symbol

    // Optional Item Name
    if (settings.showItemName) {
        OutlinedTextField(
            value = state.itemName,
            onValueChange = { viewModel.setItemName(it) },
            label = { Text("Item Name (Optional)") },
            placeholder = { Text("e.g. Books, Storage Boxes, Shoes") },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Item name" },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Shelf Card
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "SHELF DIMENSIONS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.shelfWidthText,
                    onValueChange = { viewModel.setShelfWidth(it) },
                    label = { Text("Shelf Width") },
                    suffix = { Text(unitSym) },
                    isError = state.shelfWidthError != null,
                    supportingText = state.shelfWidthError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1.2f)
                        .semantics { contentDescription = "Shelf width in $unitSym" },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = state.shelfLevelsText,
                    onValueChange = { viewModel.setShelfLevels(it) },
                    label = { Text("Levels") },
                    isError = state.levelsError != null,
                    supportingText = state.levelsError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(0.8f)
                        .semantics { contentDescription = "Shelf levels" },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }

    // Item Dimensions Card
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "ITEM DIMENSIONS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            OutlinedTextField(
                value = state.itemWidthText,
                onValueChange = { viewModel.setItemWidth(it) },
                label = { Text("Item Width") },
                suffix = { Text(unitSym) },
                isError = state.itemWidthError != null,
                supportingText = state.itemWidthError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Item width in $unitSym" },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }

    // Spacing Card (Gap & Optional Edge Margins)
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "SPACING & MARGINS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            OutlinedTextField(
                value = state.gapText,
                onValueChange = { viewModel.setGap(it) },
                label = { Text("Gap Between Items") },
                suffix = { Text(unitSym) },
                isError = state.gapError != null,
                supportingText = state.gapError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Gap in $unitSym" },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Edge Margins",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Reserve space on left and right edges",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.edgeMarginsEnabled,
                    onCheckedChange = { viewModel.setEdgeMarginsEnabled(it) }
                )
            }

            if (state.edgeMarginsEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.leftMarginText,
                        onValueChange = { viewModel.setLeftMargin(it) },
                        label = { Text("Left Margin") },
                        suffix = { Text(unitSym) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = "Left margin in $unitSym" },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = state.rightMarginText,
                        onValueChange = { viewModel.setRightMargin(it) },
                        label = { Text("Right Margin") },
                        suffix = { Text(unitSym) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = "Right margin in $unitSym" },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                state.marginsError?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Optional Depth Check Card
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "CHECK DEPTH",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Verify item fits front-to-back",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.depthCheckEnabled,
                    onCheckedChange = { viewModel.setDepthCheckEnabled(it) }
                )
            }

            if (state.depthCheckEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.shelfDepthText,
                        onValueChange = { viewModel.setShelfDepth(it) },
                        label = { Text("Shelf Depth") },
                        suffix = { Text(unitSym) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = "Shelf depth in $unitSym" },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = state.itemDepthText,
                        onValueChange = { viewModel.setItemDepth(it) },
                        label = { Text("Item Depth") },
                        suffix = { Text(unitSym) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = "Item depth in $unitSym" },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = state.depthMarginText,
                    onValueChange = { viewModel.setDepthMargin(it) },
                    label = { Text("Depth Margin (Optional)") },
                    suffix = { Text(unitSym) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Depth margin in $unitSym" },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                state.depthError?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Optional Notes
    if (settings.showNotes) {
        OutlinedTextField(
            value = state.notes,
            onValueChange = { viewModel.setNotes(it) },
            label = { Text("Notes (Optional)") },
            placeholder = { Text("e.g. Warehouse rack, Pantry, Kitchen cabinet") },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Calculation notes" },
            minLines = 2,
            maxLines = 4,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun ResultSections(
    state: com.example.shelfcount.viewmodel.CalculatorUiState,
    settings: com.example.shelfcount.model.AppSettings,
    viewModel: ShelfCountViewModel,
    context: Context,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    // Primary Dominant Result Card
    PrimaryResultCard(
        result = state.result,
        shelfLevels = state.shelfLevelsText.toLongOrNull() ?: 1L,
        onCopyCapacity = {
            if (settings.hapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("ShelfCount Capacity", "${state.result.totalCapacity} items")
            clipboard.setPrimaryClip(clip)
            viewModel.notifyMessage("Capacity copied")
        }
    )

    // Shelf Visualization
    if (settings.showShelfVisualization) {
        ShelfVisualization(
            itemsPerShelf = state.result.itemsPerShelf,
            shelfLevels = state.shelfLevelsText.toLongOrNull() ?: 1L
        )
    }

    // Secondary Metrics Card
    SecondaryResultCard(
        result = state.result,
        unit = state.unit,
        settings = settings,
        depthCheckEnabled = state.depthCheckEnabled,
        shelfDepthText = state.shelfDepthText,
        itemDepthText = state.itemDepthText
    )

    // Formula Section
    if (settings.showFormula && state.result.formulaSteps.isNotEmpty()) {
        FormulaSection(formulaSteps = state.result.formulaSteps)
    }

    // Actions Row / Buttons
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    if (settings.hapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.saveCurrentCalculation()
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Save calculation" },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("SAVE")
            }

            FilledTonalButton(
                onClick = {
                    if (settings.hapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("ShelfCount Result", state.formatShareText())
                    clipboard.setPrimaryClip(clip)
                    viewModel.notifyMessage("Result copied")
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Copy result" },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("COPY")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (settings.hapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "ShelfCount Calculation")
                        putExtra(Intent.EXTRA_TEXT, state.formatShareText())
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share ShelfCount Calculation"))
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Share result" },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("SHARE")
            }

            OutlinedButton(
                onClick = {
                    if (settings.hapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.resetCalculator()
                    viewModel.notifyMessage("Calculator reset")
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Reset calculator" },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("RESET")
            }
        }
    }
}
