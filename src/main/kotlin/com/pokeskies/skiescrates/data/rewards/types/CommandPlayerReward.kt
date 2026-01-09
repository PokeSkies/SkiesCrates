package com.pokeskies.skiescrates.data.rewards.types

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.config.GenericGUIItem
import com.pokeskies.skiescrates.data.Crate
import com.pokeskies.skiescrates.data.rewards.Reward
import com.pokeskies.skiescrates.data.rewards.RewardLimits
import com.pokeskies.skiescrates.data.rewards.RewardType
import com.pokeskies.skiescrates.placeholders.PlaceholderManager
import com.pokeskies.skiescrates.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiescrates.utils.Utils
import net.minecraft.server.level.ServerPlayer

class CommandPlayerReward(
    name: String = "",
    display: GenericGUIItem = GenericGUIItem(),
    weight: Int = 1,
    limits: RewardLimits? = null,
    broadcast: Boolean = false,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    private val commands: List<String> = emptyList()
) : Reward(RewardType.COMMAND_PLAYER, name, display, weight, limits, broadcast) {
    override fun giveReward(player: ServerPlayer, crate: Crate) {
        // Super to call the message
        super.giveReward(player, crate)

        if (SkiesCrates.INSTANCE.server.commands == null) {
            Utils.printError("There was an error while giving a reward for player ${player.name}: Server was somehow null on command execution?")
            return
        }

        for (command in commands) {
            SkiesCrates.INSTANCE.server.commands?.performPrefixedCommand(
                player.createCommandSourceStack(),
                PlaceholderManager.parse(player, command)
            )
        }
    }

    override fun toString(): String {
        return "CommandPlayer(type=$type, name='$name', display=$display, weight=$weight, limits=$limits, broadcast=$broadcast, commands=$commands)"
    }
}
