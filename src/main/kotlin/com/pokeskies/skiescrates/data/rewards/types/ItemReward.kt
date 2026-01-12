package com.pokeskies.skiescrates.data.rewards.types

import com.pokeskies.skiescrates.config.item.GenericItem
import com.pokeskies.skiescrates.data.Crate
import com.pokeskies.skiescrates.data.rewards.Reward
import com.pokeskies.skiescrates.data.rewards.RewardLimits
import com.pokeskies.skiescrates.data.rewards.RewardType
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

class ItemReward(
    name: String = "",
    display: GenericItem? = null,
    weight: Int = 1,
    limits: RewardLimits? = null,
    broadcast: Boolean = false,
    private val item: GenericItem = GenericItem()
) : Reward(RewardType.ITEM, name, display, weight, limits, broadcast) {
    override fun giveReward(player: ServerPlayer, crate: Crate) {
        // Super to call the message
        super.giveReward(player, crate)

        player.inventory.placeItemBackInInventory(item.createItemStack(player))
    }

    override fun getGenericDisplay(): GenericItem {
        return display ?: item
    }

    override fun getDisplayItem(player: ServerPlayer, placeholders: Map<String, String>): ItemStack {
        return getGenericDisplay().createItemStack(player, placeholders)
    }

    override fun toString(): String {
        return "ItemReward(name='$name', display=$display, weight=$weight, limits=$limits, broadcast=$broadcast, item=$item)"
    }
}
