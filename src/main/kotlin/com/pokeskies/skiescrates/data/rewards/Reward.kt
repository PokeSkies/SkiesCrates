package com.pokeskies.skiescrates.data.rewards

import com.google.gson.*
import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.config.Lang
import com.pokeskies.skiescrates.config.item.GenericItem
import com.pokeskies.skiescrates.data.Crate
import com.pokeskies.skiescrates.data.userdata.CrateData
import com.pokeskies.skiescrates.data.userdata.UserData
import com.pokeskies.skiescrates.utils.TextUtils
import com.pokeskies.skiescrates.utils.Utils
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import java.lang.reflect.Type
import java.util.*

abstract class Reward(
    val type: RewardType = RewardType.COMMAND_PLAYER,
    val name: String = "",
    val display: GenericItem? = null,
    val weight: Int = 1,
    val limits: RewardLimits? = null,
    val broadcast: Boolean = false,
    val preview: GenericItem? = null
) {
    lateinit var id: String

    open fun giveReward(player: ServerPlayer, crate: Crate) {
        Utils.printDebug("Attempting to execute a ${type.identifier} reward: $this")

        Lang.CRATE_REWARD.forEach {
            player.sendMessage(TextUtils.parseAllNative(
                player,
                crate.parsePlaceholders(it)
                    .replace("%reward_name%", name)
            ))
        }

        if (broadcast) {
            Lang.CRATE_REWARD_BROADCAST.forEach {
                SkiesCrates.INSTANCE.adventure.all().sendMessage(TextUtils.parseAllNative(
                    player,
                    crate.parsePlaceholders(it)
                        .replace("%reward_name%", name)
                ))
            }
        }
    }

    abstract fun getGenericDisplay(): GenericItem
    abstract fun getDisplayItem(player: ServerPlayer, placeholders: Map<String, String> = emptyMap()): ItemStack

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

    fun getPlaceholders(userData: UserData, crate: Crate): Map<String, String> {
        return mapOf(
            "%reward_name%" to (preview?.name ?: name),
            "%reward_display_name%" to (getGenericDisplay().name ?: ""),
            "%reward_display_lore%" to (getGenericDisplay().lore?.joinToString("\n") ?: ""),
            "%reward_id%" to id,
            "%reward_weight%" to weight.toString(),
            "%reward_percent%" to String.format(Locale.US, "%.2f", calculatePercent(this, crate)),
            "%reward_limit_player%" to (limits?.player?.amount?.toString() ?: "0"),
            "%reward_limit_player_claimed%" to userData.getRewardLimits(crate, this).toString()
        )
    }

    private fun calculatePercent(reward: Reward, crate: Crate): Double {
        return (reward.weight.toDouble() / crate.rewards.values.sumOf { it.weight }) * 100
    }

    override fun toString(): String {
        return "Reward(type=$type, name='$name', display=$display, weight=$weight, limits=$limits, broadcast=$broadcast)"
    }

    internal class Adapter : JsonSerializer<Reward>, JsonDeserializer<Reward> {
        override fun serialize(src: Reward, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return context.serialize(src, src::class.java)
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Reward {
            val jsonObject: JsonObject = json.getAsJsonObject()
            val value = jsonObject.get("type").asString
            val type: RewardType? = RewardType.valueOfAnyCase(value)
            return try {
                context.deserialize(json, type!!.clazz)
            } catch (e: NullPointerException) {
                throw JsonParseException("Could not deserialize reward type: $value", e)
            }
        }
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
