package com.pokeskies.skiescrates.config

import com.google.gson.annotations.SerializedName
import com.pokeskies.skiescrates.config.item.ActionMenuItem
import com.pokeskies.skiescrates.config.item.KeyMenuItem
import com.pokeskies.skiescrates.gui.InventoryType

class KeysMenu(
    val title: String = "Keys",
    @SerializedName("type", alternate = ["menu_type"])
    val type: InventoryType = InventoryType.GENERIC_9x3,
    val keys: MutableMap<String, KeyMenuItem> = mutableMapOf(),
    val items: MutableMap<String, ActionMenuItem> = mutableMapOf()
) {
    override fun toString(): String {
        return "KeysMenu(title='$title', type=$type, keys=$keys, items=$items)"
    }
}