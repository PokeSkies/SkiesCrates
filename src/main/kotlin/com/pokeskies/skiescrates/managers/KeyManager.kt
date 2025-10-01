package com.pokeskies.skiescrates.managers

import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.config.ConfigManager
import com.pokeskies.skiescrates.config.lang.Lang
import com.pokeskies.skiescrates.data.Key
import com.pokeskies.skiescrates.utils.TextUtils
import com.pokeskies.skiescrates.utils.Utils
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import java.util.concurrent.CompletableFuture
import kotlin.text.replace

object KeyManager {
    const val KEY_IDENTIFIER: String = "${SkiesCrates.MOD_ID}:key"

    fun giveKeys(key: Key, player: ServerPlayer, amount: Int, silent: Boolean = false): CompletableFuture<Boolean> {
        if (key.virtual) {
            val storage = SkiesCrates.INSTANCE.storage

            return storage.getUserAsync(player.uuid)
                .thenCompose { playerData ->
                    playerData.addKeys(key.id, amount)
                    storage.saveUserAsync(playerData)
                }
                .thenApplyAsync { result ->
                    if (result && !silent) {
                        player.server.execute {
                            Lang.KEY_GIVE.forEach {
                                player.sendMessage(TextUtils.parseAll(
                                    player,
                                    it.replace("%key_name%", key.name)
                                        .replace("%amount%", amount.toString())
                                ))
                            }
                        }
                    }
                    result
                }.exceptionally { e ->
                    Utils.printError("Storage was null while attempting save ${player.name.string}'s userdata while giving them keys! Check elsewhere for errors. Local Error: ${e.message}")
                    Lang.ERROR_STORAGE.forEach {
                        player.sendMessage(TextUtils.toNative(it))
                    }
                    false
                }
        }

        // For non-virtual keys, process synchronously since no database access is needed
        val item = key.display.createItemStack(player)
        item.count = amount

        // Apply custom data to identify as a crate
        val tag = CompoundTag()
        tag.putString(KEY_IDENTIFIER, key.id)
        item.set(DataComponents.CUSTOM_DATA, CustomData.of(tag))

        // Add to player's inventory
        player.inventory.placeItemBackInInventory(item)

        if (!silent) {
            Lang.KEY_GIVE.forEach {
                player.sendMessage(TextUtils.parseAll(
                    player,
                    it.replace("%key_name%", key.name)
                        .replace("%amount%", amount.toString())
                ))
            }
        }

        return CompletableFuture.completedFuture(true)
    }

    // TODO: Allow taking non virtual keys, merge into one function with crate removals
    fun takeKeys(key: Key, player: ServerPlayer, amount: Int, silent: Boolean = false): CompletableFuture<Boolean> {
        if (key.virtual) {
            val storage = SkiesCrates.INSTANCE.storage

            return storage.getUserAsync(player.uuid)
                .thenCompose { playerData ->
                    if (!playerData.removeKeys(key.id, amount)) {
                        CompletableFuture.completedFuture(false)
                    } else {
                        storage.saveUserAsync(playerData)
                    }
                }
                .thenApplyAsync { result ->
                    if (result && !silent) {
                        player.server.execute {
                            Lang.KEY_TAKE.forEach {
                                player.sendMessage(
                                    TextUtils.parseAll(
                                        player,
                                        it.replace("%key_name%", key.name)
                                            .replace("%amount%", amount.toString())
                                    )
                                )
                            }
                        }
                    }
                    result
                }.exceptionally { e ->
                    Utils.printError("Storage was null while attempting save ${player.name.string}'s userdata while taking keys from them! Check elsewhere for errors.")
                    Lang.ERROR_STORAGE.forEach {
                        player.sendMessage(TextUtils.toNative(it))
                    }
                    false
                }
        }

        return CompletableFuture.completedFuture(false)
    }

    // TODO: Allow taking non virtual keys, merge into one function with crate removals
    fun setKeys(key: Key, player: ServerPlayer, amount: Int, silent: Boolean = false): CompletableFuture<Boolean> {
        if (key.virtual) {
            val storage = SkiesCrates.INSTANCE.storage

            return storage.getUserAsync(player.uuid)
                .thenCompose { playerData ->
                    playerData.setKeys(key.id, amount)
                    storage.saveUserAsync(playerData)
                }
                .thenApplyAsync { result ->
                    if (result && !silent) {
                        player.server.execute {
                            Lang.KEY_SET.forEach {
                                player.sendMessage(TextUtils.parseAll(
                                    player,
                                    it.replace("%key_name%", key.name)
                                        .replace("%amount%", amount.toString())
                                ))
                            }
                        }
                    }
                    result
                }.exceptionally { e ->
                    Utils.printError("Storage was null while attempting save ${player.name.string}'s userdata while setting their keys! Check elsewhere for errors.")
                    Lang.ERROR_STORAGE.forEach {
                        player.sendMessage(TextUtils.toNative(it))
                    }
                    false
                }
        }

        return CompletableFuture.completedFuture(false)
    }

    fun getKeyOrNull(itemStack: ItemStack): Key? {
        val tag = itemStack.get(DataComponents.CUSTOM_DATA) ?: return null

        if (tag.contains(KEY_IDENTIFIER)) {
            return ConfigManager.KEYS[tag.copyTag().getString(KEY_IDENTIFIER)]
        }

        // Migration from other crate mods
        return ConfigManager.CONFIG.migration.keys?.firstNotNullOfOrNull { instance ->
            val key = ConfigManager.KEYS[instance.key] ?: run {
                Utils.printError("Migration Key ${instance.key} did not exist while attempting to find valid keys from player!")
                return@firstNotNullOfOrNull null
            }

            if (tag.contains(instance.nbt.key)) {
                val value = tag.copyTag().getString(instance.nbt.key)
                if (value != null && value == instance.nbt.value) {
                    return key
                }
            }
            null
        }
    }
}