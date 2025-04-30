package com.pokeskies.skiescrates.config.menu

import com.pokeskies.skiescrates.config.GenericGUIItem
import com.pokeskies.skiescrates.gui.InventoryType

class KeysMenu(
    val title: String = "Keys",
    val menuType: InventoryType = InventoryType.GENERIC_9x3,
    val keys: MutableMap<String, KeyMenuItem> = mutableMapOf(),
    val items: MutableMap<String, GenericGUIItem> = mutableMapOf()
) {
    override fun toString(): String {
        return "KeysMenu(title='$title', menuType=$menuType, keys=$keys, items=$items)"
    }
}
