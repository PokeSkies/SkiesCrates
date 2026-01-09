package com.pokeskies.skiescrates.data.rewards.types

import com.pokeskies.skiescrates.config.GenericGUIItem
import com.pokeskies.skiescrates.data.Crate
import com.pokeskies.skiescrates.data.rewards.Reward
import com.pokeskies.skiescrates.data.rewards.RewardLimits
import com.pokeskies.skiescrates.data.rewards.RewardType
import net.minecraft.server.level.ServerPlayer

class ItemReward(
    name: String = "null",
    display: GenericGUIItem = GenericGUIItem(),
    weight: Int = 1,
    limits: RewardLimits? = null,
    broadcast: Boolean = false,
    private val item: GenericGUIItem = GenericGUIItem()
) : Reward(RewardType.ITEM, name, display, weight, limits, broadcast) {
    override fun giveReward(player: ServerPlayer, crate: Crate) {
        // Super to call the message
        super.giveReward(player, crate)

        player.inventory.placeItemBackInInventory(item.createItemStack(player))
    }

    override fun toString(): String {
        return "ItemReward(name='$name', display=$display, weight=$weight, limits=$limits, broadcast=$broadcast, item=$item)"
    }
}
