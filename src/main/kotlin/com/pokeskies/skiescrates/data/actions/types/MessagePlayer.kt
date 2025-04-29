package com.pokeskies.skiescrates.data.actions.types

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiescrates.data.actions.Action
import com.pokeskies.skiescrates.gui.PreviewInventory
import com.pokeskies.skiescrates.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiescrates.utils.TextUtils
import com.pokeskies.skiescrates.utils.Utils
import com.pokeskies.skiescrates.data.actions.ActionType
import net.minecraft.server.level.ServerPlayer

class MessagePlayer(
    type: ActionType = ActionType.MESSAGE,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    private val message: List<String> = emptyList()
) : Action(type) {
    override fun executeAction(player: ServerPlayer, preview: PreviewInventory) {
        val parsedMessages = message.map { it /* TODO: do parsing */ }
        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}), Parsed Messages($parsedMessages): $this")
        for (line in parsedMessages) {
            player.sendMessage(TextUtils.toNative(line))
        }
    }

    override fun toString(): String {
        return "MessagePlayer(message=$message)"
    }
}
