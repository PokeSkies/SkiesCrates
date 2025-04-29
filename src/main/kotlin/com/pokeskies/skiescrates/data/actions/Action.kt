package com.pokeskies.skiescrates.data.actions

import com.pokeskies.skiescrates.gui.PreviewInventory
import net.minecraft.server.level.ServerPlayer

abstract class Action(
    val type: ActionType
) {
    abstract fun executeAction(player: ServerPlayer, preview: PreviewInventory)

    override fun toString(): String {
        return "Action(type=$type)"
    }
}
