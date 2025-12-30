package com.pokeskies.skiescrates.data.animations.inventory.items

enum class SpinMode {
    INDEPENDENT, // Each slot randomly changes independently of each other
    SEQUENTIAL, // Each slot changes in the order list, the next being the last one
    SYNCED, // Each slot is changed to the same item
    RANDOM // Randomly changes only **one** of the slots
}
