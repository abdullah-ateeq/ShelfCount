package com.example.shelfcount.model

import org.json.JSONObject
import java.math.BigDecimal
import java.util.UUID

data class SavedCalculation(
    val id: String = UUID.randomUUID().toString(),
    val itemName: String,
    val notes: String,
    val unit: MeasurementUnit,
    val shelfWidth: BigDecimal,
    val itemWidth: BigDecimal,
    val gap: BigDecimal,
    val shelfLevels: Long,
    val edgeMarginsEnabled: Boolean,
    val leftMargin: BigDecimal,
    val rightMargin: BigDecimal,
    val depthCheckEnabled: Boolean,
    val shelfDepth: BigDecimal,
    val itemDepth: BigDecimal,
    val depthMargin: BigDecimal = BigDecimal.ZERO,
    val itemsPerShelf: Long,
    val totalCapacity: Long,
    val usedWidth: BigDecimal,
    val remainingWidth: BigDecimal,
    val utilizationPercent: BigDecimal,
    val depthFits: Boolean?,
    val fitStatus: ShelfFitStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
) {
    fun toInput(): ShelfCountInput = ShelfCountInput(
        itemName = itemName,
        notes = notes,
        unit = unit,
        shelfWidth = shelfWidth,
        itemWidth = itemWidth,
        gap = gap,
        shelfLevels = shelfLevels,
        edgeMarginsEnabled = edgeMarginsEnabled,
        leftMargin = leftMargin,
        rightMargin = rightMargin,
        depthCheckEnabled = depthCheckEnabled,
        shelfDepth = shelfDepth,
        itemDepth = itemDepth,
        depthMargin = depthMargin
    )

    fun duplicate(): SavedCalculation = copy(
        id = UUID.randomUUID().toString(),
        itemName = "Copy of ${itemName.trim().ifEmpty { "Untitled Shelf" }}",
        timestamp = System.currentTimeMillis(),
        isFavorite = false
    )

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("itemName", itemName)
            put("notes", notes)
            put("unit", unit.name)
            put("shelfWidth", shelfWidth.toPlainString())
            put("itemWidth", itemWidth.toPlainString())
            put("gap", gap.toPlainString())
            put("shelfLevels", shelfLevels)
            put("edgeMarginsEnabled", edgeMarginsEnabled)
            put("leftMargin", leftMargin.toPlainString())
            put("rightMargin", rightMargin.toPlainString())
            put("depthCheckEnabled", depthCheckEnabled)
            put("shelfDepth", shelfDepth.toPlainString())
            put("itemDepth", itemDepth.toPlainString())
            put("depthMargin", depthMargin.toPlainString())
            put("itemsPerShelf", itemsPerShelf)
            put("totalCapacity", totalCapacity)
            put("usedWidth", usedWidth.toPlainString())
            put("remainingWidth", remainingWidth.toPlainString())
            put("utilizationPercent", utilizationPercent.toPlainString())
            if (depthFits != null) put("depthFits", depthFits)
            put("fitStatus", fitStatus.name)
            put("timestamp", timestamp)
            put("isFavorite", isFavorite)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): SavedCalculation {
            return SavedCalculation(
                id = json.optString("id", UUID.randomUUID().toString()),
                itemName = json.optString("itemName", ""),
                notes = json.optString("notes", ""),
                unit = try {
                    MeasurementUnit.valueOf(json.optString("unit", MeasurementUnit.CM.name))
                } catch (_: Exception) {
                    MeasurementUnit.CM
                },
                shelfWidth = BigDecimal(json.optString("shelfWidth", "120")),
                itemWidth = BigDecimal(json.optString("itemWidth", "30")),
                gap = BigDecimal(json.optString("gap", "0")),
                shelfLevels = json.optLong("shelfLevels", 4L),
                edgeMarginsEnabled = json.optBoolean("edgeMarginsEnabled", false),
                leftMargin = BigDecimal(json.optString("leftMargin", "0")),
                rightMargin = BigDecimal(json.optString("rightMargin", "0")),
                depthCheckEnabled = json.optBoolean("depthCheckEnabled", false),
                shelfDepth = BigDecimal(json.optString("shelfDepth", "30")),
                itemDepth = BigDecimal(json.optString("itemDepth", "20")),
                depthMargin = BigDecimal(json.optString("depthMargin", "0")),
                itemsPerShelf = json.optLong("itemsPerShelf", 0L),
                totalCapacity = json.optLong("totalCapacity", 0L),
                usedWidth = BigDecimal(json.optString("usedWidth", "0")),
                remainingWidth = BigDecimal(json.optString("remainingWidth", "0")),
                utilizationPercent = BigDecimal(json.optString("utilizationPercent", "0")),
                depthFits = if (json.has("depthFits")) json.optBoolean("depthFits") else null,
                fitStatus = try {
                    ShelfFitStatus.valueOf(json.optString("fitStatus", ShelfFitStatus.NO_ITEM_FIT.name))
                } catch (_: Exception) {
                    ShelfFitStatus.NO_ITEM_FIT
                },
                timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                isFavorite = json.optBoolean("isFavorite", false)
            )
        }
    }
}
