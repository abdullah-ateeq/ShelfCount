package com.example.shelfcount.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shelfcount.calculator.MeasurementFormatter
import com.example.shelfcount.model.ShelfCountResult
import com.example.shelfcount.model.ShelfFitStatus
import com.example.shelfcount.ui.theme.FitNoneRed
import com.example.shelfcount.ui.theme.FitPerfectGreen
import com.example.shelfcount.ui.theme.FitSpaceAmber

@Composable
fun PrimaryResultCard(
    result: ShelfCountResult,
    shelfLevels: Long,
    onCopyCapacity: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (result.fitStatus) {
        ShelfFitStatus.PERFECT_FIT -> FitPerfectGreen
        ShelfFitStatus.SPACE_REMAINING -> MaterialTheme.colorScheme.primary
        ShelfFitStatus.NO_ITEM_FIT -> FitNoneRed
    }

    val statusIcon = when (result.fitStatus) {
        ShelfFitStatus.PERFECT_FIT -> Icons.Default.CheckCircle
        ShelfFitStatus.SPACE_REMAINING -> Icons.Default.Info
        ShelfFitStatus.NO_ITEM_FIT -> Icons.Default.ErrorOutline
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = if (result.itemsPerShelf == 0L) {
                    "Total capacity 0 items. No item fit. ${result.errorMessage ?: "Item is wider than available shelf space."}"
                } else {
                    "Total capacity ${result.totalCapacity} items, ${result.itemsPerShelf} items per shelf, $shelfLevels shelf levels. Status ${result.fitStatus.label}."
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with status badge & copy capacity button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = result.fitStatus.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        letterSpacing = 0.5.sp
                    )
                }

                IconButton(
                    onClick = onCopyCapacity,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy capacity",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Visually Dominant Capacity Result
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TOTAL CAPACITY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (result.isCapacityOverflow) {
                    Text(
                        text = "Capacity is too large",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = FitNoneRed
                    )
                } else {
                    Text(
                        text = "${result.totalCapacity}",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 52.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = if (result.itemsPerShelf == 0L) FitNoneRed else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (result.totalCapacity == 1L) "ITEM" else "ITEMS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }

                if (result.itemsPerShelf == 0L) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.errorMessage ?: "Item is wider than the available shelf space.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FitNoneRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Secondary Metrics: Items / Shelf and Shelf Levels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${result.itemsPerShelf}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ITEMS / SHELF",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(width = 1.dp, height = 28.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$shelfLevels",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "SHELF LEVELS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
