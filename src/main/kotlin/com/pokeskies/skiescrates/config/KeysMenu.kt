package com.pokeskies.skiescrates.config

import com.google.gson.annotations.SerializedName
import com.pokeskies.skiescrates.config.item.KeyMenuItem
import com.pokeskies.skiescrates.config.item.MenuItem
import com.pokeskies.skiescrates.gui.InventoryType

class KeysMenu(
    val title: String = "Keys",
    @SerializedName("menu_type", alternate = ["menuType"])
    val menuType: InventoryType = InventoryType.GENERIC_9x3,
    val keys: MutableMap<String, KeyMenuItem> = mutableMapOf(),
    val items: MutableMap<String, MenuItem> = mutableMapOf()
) {
    override fun toString(): String {
        return "KeysMenu(title='$title', menuType=$menuType, keys=$keys, items=$items)"
    }
}