package com.pokeskies.skiescrates.data.animations

import com.google.gson.annotations.SerializedName
import com.pokeskies.skiescrates.config.GenericGUIItem
import com.pokeskies.skiescrates.data.animations.items.SpinningItem
import com.pokeskies.skiescrates.data.animations.presets.AnimatedItem
import com.pokeskies.skiescrates.data.animations.presets.RewardItem
import com.pokeskies.skiescrates.gui.InventoryType

class InventoryAnimation(
    val settings: Settings,
    val items: Items,
    val presets: Presets
) {
    // This is the general settings for this inventory animation
    class Settings(
        val title: String,
        @SerializedName("menu_type")
        val menuType: InventoryType,
        @SerializedName("close_delay")
        val closeDelay: Int,
        @SerializedName("win_slots")
        val winSlots: List<Int>,
    )

    // These are the items that are used in the inventory animation
    class Items(
        // These are items that display the rewards
        val rewards: MutableMap<String, SpinningItem>,
        // These are items that update over time
        val animated: MutableMap<String, SpinningItem>,
        // These are items that remain the same throughout the GUI
        val static: MutableMap<String, GenericGUIItem>
    )

    class Presets(
        val rewards: MutableMap<String, RewardItem>,
        val animations: MutableMap<String, List<AnimatedItem>>,
    )
}
