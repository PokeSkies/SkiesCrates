package com.pokeskies.skiescrates.data.rewards

import com.pokeskies.skiescrates.config.GenericGUIItem
import com.pokeskies.skiescrates.config.lang.Lang
import com.pokeskies.skiescrates.data.Crate
import com.pokeskies.skiescrates.utils.TextUtils
import com.pokeskies.skiescrates.utils.Utils
import net.minecraft.server.level.ServerPlayer

abstract class Reward(
    val type: RewardType,
    val name: String,
    val display: GenericGUIItem,
    val weight: Int,
    val broadcast: Boolean
) {
    open fun giveReward(player: ServerPlayer, crate: Crate) {
        Utils.printDebug("Attempting to execute a ${type.identifier} reward: $this")

        Lang.CRATE_REWARD.forEach {
            player.sendMessage(
                TextUtils.toComponent(it
                    .replace("%player", player.name.string)
                    .replace("%crate_name%", crate.name)
                    .replace("%reward_name%", name)
                ))
        }

        if (broadcast) {
            Lang.CRATE_REWARD_BROADCAST.forEach {
                player.sendMessage(
                    TextUtils.toComponent(it
                        .replace("%player", player.name.string)
                        .replace("%crate_name%", crate.name)
                        .replace("%reward_name%", name)
                    ))
            }
        }
    }

    override fun toString(): String {
        return "Reward(type=$type, display=$display, weight=$weight, broadcast=$broadcast)"
    }
}
