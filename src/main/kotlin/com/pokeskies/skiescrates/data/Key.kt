package com.pokeskies.skiescrates.data

import com.pokeskies.skiescrates.CratesManager
import com.pokeskies.skiescrates.config.GenericGUIItem
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.component.CustomData

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
