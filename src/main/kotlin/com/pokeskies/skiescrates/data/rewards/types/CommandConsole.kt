package com.pokeskies.skiescrates.data.rewards.types

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.config.GenericGUIItem
import com.pokeskies.skiescrates.config.CrateConfig
import com.pokeskies.skiescrates.data.rewards.Reward
import com.pokeskies.skiescrates.data.rewards.RewardLimits
import com.pokeskies.skiescrates.data.rewards.RewardType
import com.pokeskies.skiescrates.placeholders.PlaceholderManager
import com.pokeskies.skiescrates.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiescrates.utils.Utils
import net.minecraft.server.level.ServerPlayer

class CommandConsole(
    type: RewardType = RewardType.COMMAND_CONSOLE,
    name: String = "null",
    display: GenericGUIItem = GenericGUIItem(),
    weight: Int = 1,
    limits: RewardLimits? = null,
    broadcast: Boolean = false,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    private val commands: List<String> = emptyList()
) : Reward(type, name, display, weight, limits, broadcast) {
    override fun giveReward(player: ServerPlayer, crateConfig: CrateConfig) {
        // Super to call the message
        super.giveReward(player, crateConfig)

        if (SkiesCrates.INSTANCE.server.commands == null) {
            Utils.printError("There was an error while giving a reward for player ${player.name}: Server was somehow null on command execution?")
            return
        }

        for (command in commands) {
            SkiesCrates.INSTANCE.server.commands.performPrefixedCommand(
                SkiesCrates.INSTANCE.server.createCommandSourceStack(),
                PlaceholderManager.parse(player, command)
            )
        }
    }

    override fun toString(): String {
        return "CommandConsole(type=$type, name='$name', display=$display, weight=$weight, limits=$limits, broadcast=$broadcast, commands=$commands)"
    }
}
