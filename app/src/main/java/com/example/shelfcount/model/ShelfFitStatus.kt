package com.example.shelfcount.model

enum class ShelfFitStatus(
    val label: String,
    val description: String
) {
    PERFECT_FIT(
        label = "PERFECT FIT",
        description = "Shelf is completely filled with zero leftover space."
    ),
    SPACE_REMAINING(
        label = "SPACE REMAINING",
        description = "Items fit comfortably with remaining shelf space."
    ),
    NO_ITEM_FIT(
        label = "NO ITEM FIT",
        description = "Item is wider than the available shelf space."
    )
}
