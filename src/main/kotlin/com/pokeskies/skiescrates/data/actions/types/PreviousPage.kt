package com.pokeskies.skiescrates.data.actions.types

import com.pokeskies.skiescrates.data.actions.Action
import com.pokeskies.skiescrates.data.actions.ActionType
import com.pokeskies.skiescrates.gui.PreviewInventory
import com.pokeskies.skiescrates.utils.Utils
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.server.level.ServerPlayer

class PreviousPage(
) : Action(ActionType.PREVIOUS_PAGE) {
    override fun executeAction(player: ServerPlayer, gui: SimpleGui) {
        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}) $this")

        if (gui !is PreviewInventory) {
            Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}) tried to execute a PreviousPage action not in paginated.")
            return
        }

        gui.previousPage()
    }

    override fun toString(): String {
        return "PreviousPage()"
    }
}
