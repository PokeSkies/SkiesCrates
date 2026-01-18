package com.pokeskies.skiescrates.data.opening.inventory

import com.google.gson.annotations.SerializedName
import com.pokeskies.skiescrates.config.item.MenuItem
import com.pokeskies.skiescrates.data.opening.OpeningAnimation
import com.pokeskies.skiescrates.data.opening.inventory.items.SpinningItem
import com.pokeskies.skiescrates.data.opening.inventory.presets.AnimatedItem
import com.pokeskies.skiescrates.data.opening.inventory.presets.RewardItem
import com.pokeskies.skiescrates.gui.InventoryType

class InventoryOpeningAnimation(
    val title: String,
    @SerializedName("type", alternate = ["menu_type"])
    val type: InventoryType,
    @SerializedName("close_delay")
    val closeDelay: Int,
    @SerializedName("win_slots")
    val winSlots: List<Int>,
    val skippable: Boolean = false,
    val items: Items,
    val presets: Presets
): OpeningAnimation {
    // These are the items that are used in the inventory animation
    class Items(
        // These are items that display the rewards
        val rewards: MutableMap<String, SpinningItem>,
        // These are items that update over time
        val animated: MutableMap<String, SpinningItem>,
        // These are items that remain the same throughout the GUI
        val static: MutableMap<String, MenuItem>
    )

    class Presets(
        val rewards: MutableMap<String, RewardItem>,
        val animations: MutableMap<String, List<AnimatedItem>>,
    )
}