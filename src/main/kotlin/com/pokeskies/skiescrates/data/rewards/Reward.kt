package com.pokeskies.skiescrates.data.rewards

import com.google.gson.*
import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.config.GenericGUIItem
import com.pokeskies.skiescrates.config.lang.Lang
import com.pokeskies.skiescrates.data.Crate
import com.pokeskies.skiescrates.data.userdata.CrateData
import com.pokeskies.skiescrates.data.userdata.UserData
import com.pokeskies.skiescrates.utils.TextUtils
import com.pokeskies.skiescrates.utils.Utils
import net.minecraft.server.level.ServerPlayer
import java.lang.reflect.Type

abstract class Reward(
    val type: RewardType = RewardType.COMMAND_PLAYER,
    val name: String = "null",
    val display: GenericGUIItem = GenericGUIItem(),
    val weight: Int = 1,
    val limits: RewardLimits? = null,
    val broadcast: Boolean = false,
    val preview: GenericGUIItem? = null
) {
    lateinit var id: String

    open fun giveReward(player: ServerPlayer, crate: Crate) {
        Utils.printDebug("Attempting to execute a ${type.identifier} reward: $this")

        Lang.CRATE_REWARD.forEach {
            player.sendMessage(TextUtils.parseAll(
                player,
                crate.parsePlaceholders(it)
                    .replace("%reward_name%", name)
            ))
        }

        if (broadcast) {
            Lang.CRATE_REWARD_BROADCAST.forEach {
                SkiesCrates.INSTANCE.adventure.all().sendMessage(TextUtils.parseAll(
                    player,
                    crate.parsePlaceholders(it)
                        .replace("%reward_name%", name)
                ))
            }
        }
    }

    // TODO: Add global limit checking, need to provide function a way to access that data
    fun canReceive(userData: UserData, crate: Crate): Boolean {
        if (limits == null) return true

        // Check player limits
        val playerLimits = limits.player
        if (playerLimits != null) {
            if (playerLimits.amount >= 0) {
                val limitData = userData.crates.getOrDefault(crate.id, CrateData()).rewards?.get(id)
                if (limitData != null && limitData.claimed >= playerLimits.amount) { // If the player has limit data, check if their amount has surpassed the limit
                    if (playerLimits.cooldown <= 0 || (limitData.time + (playerLimits.cooldown * 1000)) > System.currentTimeMillis()) { // If it has surpassed, check if the cooldown has not expired
                        return false
                    }
                }
            }
        }

        // TODO: Add global limit checking here!

        return true
    }

    fun getPlayerLimit(): Int {
        return limits?.player?.amount ?: 0
    }

    override fun toString(): String {
        return "Reward(type=$type, name='$name', display=$display, weight=$weight, limits=$limits, broadcast=$broadcast)"
    }

    class RewardMapAdapter: JsonSerializer<MutableMap<String, Reward>>, JsonDeserializer<MutableMap<String, Reward>> {
        override fun serialize(
            src: MutableMap<String, Reward>,
            typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement {
            val obj = JsonObject()
            for ((key, reward) in src) {
                obj.add(key, context.serialize(reward))
            }
            return obj
        }

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): MutableMap<String, Reward> {
            val map = mutableMapOf<String, Reward>()
            val obj = json.asJsonObject
            for ((key, value) in obj.entrySet()) {
                val reward = context.deserialize<Reward>(value, Reward::class.java)
                reward.id = key
                map[key] = reward
            }
            return map
        }
    }
}
