package com.pokeskies.skiescrates.data

import com.pokeskies.skiescrates.config.GenericGUIItem

class Key(
    val enabled: Boolean,
    val name: String,
    val display: GenericGUIItem,
    val virtual: Boolean
) {
    // Local variable that is filled in when creating the object
    lateinit var id: String

    override fun toString(): String {
        return "Key(enabled=$enabled, name='$name', display=$display, virtual=$virtual)"
    }
}
