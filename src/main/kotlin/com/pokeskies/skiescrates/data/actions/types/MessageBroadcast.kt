package com.pokeskies.skiescrates.data.actions.types

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.data.actions.Action
import com.pokeskies.skiescrates.data.actions.ActionType
import com.pokeskies.skiescrates.placeholders.PlaceholderManager
import com.pokeskies.skiescrates.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiescrates.utils.Utils
import com.pokeskies.skiescrates.utils.asNative
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.server.level.ServerPlayer

class MessageBroadcast(
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    private val message: List<String> = emptyList()
) : Action(ActionType.BROADCAST) {
    override fun executeAction(player: ServerPlayer, gui: SimpleGui) {
        val parsedMessages = message.map { PlaceholderManager.parse(player, it) }

        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}), Parsed Messages($parsedMessages): $this")

        for (line in parsedMessages) {
            SkiesCrates.INSTANCE.adventure.all().sendMessage(line.asNative())
        }
    }

    override fun toString(): String {
        return "MessageBroadcast(message=$message)"
    }
}
