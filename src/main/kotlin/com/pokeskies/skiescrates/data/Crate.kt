package com.pokeskies.skiescrates.data

import com.google.gson.annotations.SerializedName
import com.pokeskies.skiescrates.config.ConfigManager
import com.pokeskies.skiescrates.config.CostOptions
import com.pokeskies.skiescrates.config.FailureOptions
import com.pokeskies.skiescrates.config.GenericGUIItem
import com.pokeskies.skiescrates.config.block.BlockOptions
import com.pokeskies.skiescrates.data.rewards.Reward
import com.pokeskies.skiescrates.utils.RandomCollection
import net.minecraft.server.level.ServerPlayer

class Crate(
    val enabled: Boolean = true,
    val name: String = "",
    val display: GenericGUIItem = GenericGUIItem(),
    val unique: Boolean = false,
    val preview: String = "",
    val animation: String = "",
    val permission: String = "",
    @SerializedName("inventory_space")
    val inventorySpace: Int = -1,
    val cost: CostOptions? = null,
    val cooldown: Long = -1,
    val failure: FailureOptions? = null,
    val keys: Map<String, Int> = emptyMap(),
    val block: BlockOptions = BlockOptions(),
    val rewards: MutableMap<String, Reward> = mutableMapOf(),
) {
    // Local variable that is filled in when creating the object
    lateinit var id: String

    private var randomBag: RandomCollection<Pair<String, Reward>>? = null

    fun generateReward(player: ServerPlayer): Pair<String, Reward> {
        if (randomBag == null) {
            randomBag = RandomCollection()
            rewards.forEach { (id, reward) ->
                randomBag!!.add(reward.weight.toDouble(), id to reward)
            }
        }
        return randomBag!!.next()
    }

    fun parsePlaceholders(string: String): String {
        return string.replace("%crate_name%", name)
            .replace("%crate_id%", id)
            .replace("%crate_keys%", keys.entries.joinToString(", ") { (keyId, amount) ->
                "${ConfigManager.KEYS[keyId]?.name ?: keyId} x$amount"
            })
            .replace("%crate_inventory_space%", inventorySpace.toString())
    }

    override fun toString(): String {
        return "Crate(id='$id', enabled=$enabled, name='$name', display=$display, unique=$unique, preview='$preview', " +
                "animation='$animation', permission='$permission', cost=$cost, cooldown=$cooldown, keys=$keys, " +
                "block=$block, rewards=$rewards)"
    }
}
