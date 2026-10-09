package com.example.shelfcount.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shelfcount.calculator.MeasurementFormatter
import com.example.shelfcount.model.AppSettings
import com.example.shelfcount.model.MeasurementUnit
import com.example.shelfcount.model.ShelfCountResult
import com.example.shelfcount.ui.theme.FitNoneRed
import com.example.shelfcount.ui.theme.FitPerfectGreen

@Composable
fun SecondaryResultCard(
    result: ShelfCountResult,
    unit: MeasurementUnit,
    settings: AppSettings,
    depthCheckEnabled: Boolean,
    shelfDepthText: String,
    itemDepthText: String,
    modifier: Modifier = Modifier
) {
    val unitSym = unit.symbol

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Measurement results: Used width ${MeasurementFormatter.formatMeasurement(result.usedWidth)} $unitSym, Remaining width ${MeasurementFormatter.formatMeasurement(result.remainingWidth)} $unitSym, Utilization ${MeasurementFormatter.formatPercent(result.utilizationPercent)}"
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Metrics Row 1: Used Width & Remaining Width
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (settings.showUsedWidth) {
                    MetricBox(
                        title = "USED WIDTH",
                        value = "${MeasurementFormatter.formatMeasurement(result.usedWidth)} $unitSym",
                        modifier = Modifier.weight(1f)
                    )
                }

                if (settings.showRemainingSpace) {
                    MetricBox(
                        title = "REMAINING",
                        value = "${MeasurementFormatter.formatMeasurement(result.remainingWidth)} $unitSym",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Metrics Row 2: Utilization & Unused Space
            if (settings.showUtilization) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricBox(
                        title = "UTILIZATION",
                        value = MeasurementFormatter.formatPercent(result.utilizationPercent),
                        modifier = Modifier.weight(1f)
                    )

                    MetricBox(
                        title = "UNUSED SPACE",
                        value = MeasurementFormatter.formatPercent(result.unusedPercent),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Optional Depth Check Result
            if (depthCheckEnabled && result.depthFits != null) {
                val fits = result.depthFits
                val dColor = if (fits) FitPerfectGreen else FitNoneRed
                val dIcon = if (fits) Icons.Default.Check else Icons.Default.Close
                val dText = if (fits) "YES (FITS)" else "NO (DOES NOT FIT)"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(dColor.copy(alpha = 0.1f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "DEPTH FIT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = dColor
                        )
                        Text(
                            text = "Item: $itemDepthText $unitSym  ·  Shelf: $shelfDepthText $unitSym",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = dIcon,
                            contentDescription = null,
                            tint = dColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = dText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = dColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
