package com.pokeskies.skiescrates.data.key

import com.pokeskies.skiescrates.config.GenericGUIItem

class Key(
    val enabled: Boolean = true,
    val name: String = "",
    val display: GenericGUIItem = GenericGUIItem(),
    val virtual: Boolean = false,
    val unique: Boolean = false,
) {
    // Local variable that is filled in when creating the object
    lateinit var id: String

    override fun toString(): String {
        return "Key(id='$id', enabled=$enabled, name='$name', display=$display, virtual=$virtual, unique=$unique)"
    }
}
