package com.pokeskies.skiescrates.data.rewards.types

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.config.GenericGUIItem
import com.pokeskies.skiescrates.data.Crate
import com.pokeskies.skiescrates.data.rewards.Reward
import com.pokeskies.skiescrates.data.rewards.RewardType
import com.pokeskies.skiescrates.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiescrates.utils.Utils
import net.minecraft.server.level.ServerPlayer

class CommandConsole(
    type: RewardType = RewardType.COMMAND_CONSOLE,
    name: String = "null",
    display: GenericGUIItem = GenericGUIItem(),
    weight: Int = 1,
    broadcast: Boolean = false,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    private val commands: List<String> = emptyList()
) : Reward(type, name, display, weight, broadcast) {
    override fun giveReward(player: ServerPlayer, crate: Crate) {
        // Super to call the message
        super.giveReward(player, crate)

        if (SkiesCrates.INSTANCE.server.commands == null) {
            Utils.printError("There was an error while giving a reward for player ${player.name}: Server was somehow null on command execution?")
            return
        }

        for (command in commands) {
            SkiesCrates.INSTANCE.server.commands.performPrefixedCommand(
                SkiesCrates.INSTANCE.server.createCommandSourceStack(),
                command.replace("%player%", player.name.string)
            )
        }
    }

    override fun toString(): String {
        return "CommandConsole(type=$type, commands=$commands)"
    }
}
