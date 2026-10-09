package com.example.shelfcount

import com.example.shelfcount.model.HistoryFilterOption
import com.example.shelfcount.model.HistorySortOption
import com.example.shelfcount.model.MeasurementUnit
import com.example.shelfcount.model.SavedCalculation
import com.example.shelfcount.model.ShelfFitStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ShelfCountHistoryLogicTest {

    private fun createSampleCalculation(
        id: String = "id-1",
        name: String = "Books",
        capacity: Long = 16L,
        utilization: BigDecimal = BigDecimal("100"),
        fitStatus: ShelfFitStatus = ShelfFitStatus.PERFECT_FIT,
        depthFits: Boolean? = true,
        timestamp: Long = 1000L,
        isFavorite: Boolean = false
    ): SavedCalculation {
        return SavedCalculation(
            id = id,
            itemName = name,
            notes = "Standard shelf",
            unit = MeasurementUnit.CM,
            shelfWidth = BigDecimal("120"),
            itemWidth = BigDecimal("30"),
            gap = BigDecimal.ZERO,
            shelfLevels = 4L,
            edgeMarginsEnabled = false,
            leftMargin = BigDecimal.ZERO,
            rightMargin = BigDecimal.ZERO,
            depthCheckEnabled = true,
            shelfDepth = BigDecimal("30"),
            itemDepth = BigDecimal("20"),
            depthMargin = BigDecimal.ZERO,
            itemsPerShelf = 4L,
            totalCapacity = capacity,
            usedWidth = BigDecimal("120"),
            remainingWidth = BigDecimal.ZERO,
            utilizationPercent = utilization,
            depthFits = depthFits,
            fitStatus = fitStatus,
            timestamp = timestamp,
            isFavorite = isFavorite
        )
    }

    // 26. History persistence (Serialization / Deserialization)
    @Test
    fun testSerializationAndDeserialization() {
        val original = createSampleCalculation()
        val json = original.toJson()
        val restored = SavedCalculation.fromJson(json)

        assertEquals(original.id, restored.id)
        assertEquals(original.itemName, restored.itemName)
        assertEquals(original.notes, restored.notes)
        assertEquals(original.unit, restored.unit)
        assertEquals(0, original.shelfWidth.compareTo(restored.shelfWidth))
        assertEquals(0, original.itemWidth.compareTo(restored.itemWidth))
        assertEquals(0, original.gap.compareTo(restored.gap))
        assertEquals(original.shelfLevels, restored.shelfLevels)
        assertEquals(original.itemsPerShelf, restored.itemsPerShelf)
        assertEquals(original.totalCapacity, restored.totalCapacity)
        assertEquals(original.depthFits, restored.depthFits)
        assertEquals(original.fitStatus, restored.fitStatus)
        assertEquals(original.isFavorite, restored.isFavorite)
    }

    // 27. Settings persistence & Duplication
    @Test
    fun testDuplicate() {
        val original = createSampleCalculation(id = "orig-123", name = "Pantry", isFavorite = true)
        val dup = original.duplicate()

        assertNotEquals(original.id, dup.id)
        assertEquals("Copy of Pantry", dup.itemName)
        assertFalse(dup.isFavorite) // Duplicate should reset favorite to false
        assertEquals(original.totalCapacity, dup.totalCapacity)
        assertEquals(original.shelfWidth, dup.shelfWidth)
    }

    // 28. Favorites appearance first in sorting
    @Test
    fun testFavoritesAppearFirst() {
        val item1 = createSampleCalculation(id = "1", name = "Alpha", timestamp = 5000L, isFavorite = false)
        val item2 = createSampleCalculation(id = "2", name = "Beta", timestamp = 1000L, isFavorite = true)
        val item3 = createSampleCalculation(id = "3", name = "Gamma", timestamp = 3000L, isFavorite = false)

        val list = listOf(item1, item2, item3)

        // Comparator with favorites first, then Recently Updated (descending timestamp)
        val sorted = list.sortedWith(
            compareByDescending<SavedCalculation> { it.isFavorite }
                .thenByDescending { it.timestamp }
        )

        assertEquals("Beta", sorted[0].itemName) // Favorite first
        assertEquals("Alpha", sorted[1].itemName) // Higher timestamp
        assertEquals("Gamma", sorted[2].itemName)
    }

    // 29. Sorting options
    @Test
    fun testHistorySortingOptions() {
        val lowCap = createSampleCalculation(id = "1", name = "C", capacity = 5L, utilization = BigDecimal("50"), timestamp = 10L)
        val highCap = createSampleCalculation(id = "2", name = "A", capacity = 50L, utilization = BigDecimal("95"), timestamp = 20L)
        val midCap = createSampleCalculation(id = "3", name = "B", capacity = 20L, utilization = BigDecimal("80"), timestamp = 30L)

        val list = listOf(lowCap, highCap, midCap)

        // Highest Capacity
        val capSorted = list.sortedWith(
            compareByDescending<SavedCalculation> { it.isFavorite }
                .thenByDescending { it.totalCapacity }
        )
        assertEquals(50L, capSorted[0].totalCapacity)
        assertEquals(20L, capSorted[1].totalCapacity)
        assertEquals(5L, capSorted[2].totalCapacity)

        // Highest Utilization
        val utilSorted = list.sortedWith(
            compareByDescending<SavedCalculation> { it.isFavorite }
                .thenByDescending { it.utilizationPercent }
        )
        assertEquals(0, utilSorted[0].utilizationPercent.compareTo(BigDecimal("95")))

        // Name A-Z
        val nameSorted = list.sortedWith(
            compareByDescending<SavedCalculation> { it.isFavorite }
                .thenBy { it.itemName.lowercase() }
        )
        assertEquals("A", nameSorted[0].itemName)
        assertEquals("B", nameSorted[1].itemName)
        assertEquals("C", nameSorted[2].itemName)
    }

    // 30. Filtering and search
    @Test
    fun testHistoryFilteringAndSearch() {
        val perfect = createSampleCalculation(id = "1", name = "Perfect Box", fitStatus = ShelfFitStatus.PERFECT_FIT, depthFits = true)
        val space = createSampleCalculation(id = "2", name = "Spaced Jars", fitStatus = ShelfFitStatus.SPACE_REMAINING, depthFits = false)
        val noFit = createSampleCalculation(id = "3", name = "Huge Crate", fitStatus = ShelfFitStatus.NO_ITEM_FIT, depthFits = null)

        val all = listOf(perfect, space, noFit)

        // Filter: PERFECT_FIT
        val filteredPerfect = all.filter { it.fitStatus == ShelfFitStatus.PERFECT_FIT }
        assertEquals(1, filteredPerfect.size)
        assertEquals("Perfect Box", filteredPerfect[0].itemName)

        // Filter: DEPTH_FIT
        val filteredDepthFit = all.filter { it.depthFits == true }
        assertEquals(1, filteredDepthFit.size)
        assertEquals("Perfect Box", filteredDepthFit[0].itemName)

        // Search case-insensitive
        val searchBox = all.filter { it.itemName.contains("box", ignoreCase = true) }
        assertEquals(1, searchBox.size)
        assertEquals("Perfect Box", searchBox[0].itemName)
    }
}
