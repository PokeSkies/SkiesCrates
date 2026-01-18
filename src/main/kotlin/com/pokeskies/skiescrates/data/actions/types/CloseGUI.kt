package com.pokeskies.skiescrates.data.actions.types

import com.pokeskies.skiescrates.data.actions.Action
import com.pokeskies.skiescrates.data.actions.ActionType
import com.pokeskies.skiescrates.utils.Utils
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.server.level.ServerPlayer

class CloseGUI(
) : Action(ActionType.CLOSE_GUI) {
    override fun executeAction(player: ServerPlayer, gui: SimpleGui) {
        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}): $this")
        gui.close()
    }

    override fun toString(): String {
        return "CloseGUI()"
    }
}
