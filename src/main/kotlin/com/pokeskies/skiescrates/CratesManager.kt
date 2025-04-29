package com.pokeskies.skiescrates

import com.pokeskies.skiescrates.config.ConfigManager
import com.pokeskies.skiescrates.config.lang.Lang
import com.pokeskies.skiescrates.data.DimensionalBlockPos
import com.pokeskies.skiescrates.data.Crate
import com.pokeskies.skiescrates.data.CrateOpenData
import com.pokeskies.skiescrates.data.Key
import com.pokeskies.skiescrates.gui.CrateInventory
import com.pokeskies.skiescrates.gui.PreviewInventory
import com.pokeskies.skiescrates.utils.TextUtils
import com.pokeskies.skiescrates.utils.Utils
import me.lucko.fabric.api.permissions.v0.Permissions
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.phys.Vec3
import java.util.*

object CratesManager {
    const val CRATE_IDENTIFIER: String = "${SkiesCrates.MOD_ID}:crate"
    const val KEY_IDENTIFIER: String = "${SkiesCrates.MOD_ID}:crate"

    private val locations: MutableMap<DimensionalBlockPos, Crate> = mutableMapOf()
    private val interactionLimiter = mutableMapOf<UUID, Long>()
    val openingPlayers: MutableList<UUID> = mutableListOf()

    fun init() {
        // load the crate locations
        locations.clear()
        ConfigManager.CRATES.forEach { (id, crate) ->
            if (!crate.enabled) return@forEach
            for (location in crate.block.locations) {
                val level = Utils.getLevel(location.dimension)
                if (level == null) {
                    Utils.printError("Crate ${crate.name} has an invalid dimension location: $location")
                    return@forEach
                }
                if (locations.containsKey(location)) {
                    Utils.printError("Crate ${crate.name} has a duplicate location: $location")
                    return@forEach
                }
                locations[location] = crate

                if (crate.block.hologram != null) {
                    // TODO: Hologram implementation
                }

                if (crate.block.particles != null) {
                    // TODO: Particle implementation
                }
            }
        }
    }

    fun tick() {
        // TODO: Particle ticking
        // TODO: spamLimiter cleanup every X often
    }

    fun giveCrate(crate: Crate, player: ServerPlayer, amount: Int, silent: Boolean = false): Boolean {
        val item = crate.display.createItemStack(player)

        // TODO: Update amount to checking unique
        item.count = amount

        // Apply custom data to identify as a crate
        val tag = CompoundTag()
        tag.putString(CRATE_IDENTIFIER, crate.id)
        item.applyComponents(
            DataComponentPatch.builder()
            .set(DataComponents.CUSTOM_DATA, CustomData.of(tag))
            .build())

        // Add to player's inventory
        player.inventory.placeItemBackInInventory(item)

        if (!silent) {
            Lang.CRATE_GIVE.forEach {
                player.sendMessage(TextUtils.toComponent(it))
            }
        }

        return true
    }

    fun giveKey(key: Key, player: ServerPlayer, amount: Int, silent: Boolean = false): Boolean {
        if (key.virtual) {
            val playerData = SkiesCrates.INSTANCE.storage?.getUser(player.uuid) ?: run {
                player.sendMessage(Component.text("There was an error with the storage system! Please contact an admin.").color(NamedTextColor.RED))
                return false
            }

            playerData.addKey(key.id, amount)

            val result = SkiesCrates.INSTANCE.storage?.saveUser(player.uuid, playerData) ?: false

            if (result && !silent) {
                Lang.KEY_GIVE.forEach {
                    player.sendMessage(TextUtils.toComponent(it))
                }
            }

            return result
        }

        val item = key.display.createItemStack(player)

        // TODO: Update amount to do unique checking
        item.count = amount

        // Apply custom data to identify as a crate
        val tag = CompoundTag()
        tag.putString(KEY_IDENTIFIER, key.id)
        item.set(DataComponents.CUSTOM_DATA, CustomData.of(tag))

        // Add to player's inventory
        player.inventory.placeItemBackInInventory(item)

        if (!silent) {
            Lang.CRATE_GIVE.forEach {
                player.sendMessage(TextUtils.toComponent(it))
            }
        }

        return true
    }

    // This method is massive, but it handles a lot of things!
    fun openCrate(player: ServerPlayer, crate: Crate, openData: CrateOpenData, isForced: Boolean): Boolean {
        // TODO: Check crate validity
        interactionLimiter[player.uuid]?.let {
            if ((it + ConfigManager.CONFIG.interactionLimiter) > System.currentTimeMillis())
                return false
        }

        interactionLimiter[player.uuid] = System.currentTimeMillis()

        // Check if the player is already opening a crate
        if (openingPlayers.contains(player.uuid)) {
            handleCrateFail(player, crate, openData)
            Lang.ERROR_ALREADY_OPENING.forEach {
                player.sendMessage(TextUtils.toComponent(it))
            }
            return false
        }

        // Permission check
        if (!isForced && crate.permission.isNotEmpty() && !Permissions.check(player, crate.permission)) {
            handleCrateFail(player, crate, openData)
            Lang.ERROR_NO_PERMISSION.forEach {
                player.sendMessage(TextUtils.toComponent(it))
            }
            return false
        }

        // Inventory space check
        if (!isForced && crate.inventorySpace > 0 && player.inventory.items.count { it.isEmpty } >= crate.inventorySpace) {
            handleCrateFail(player, crate, openData)
            Lang.ERROR_INVENTORY_SPACE.forEach {
                player.sendMessage(TextUtils.toComponent(it.replace("%crate_inventory_space%", crate.inventorySpace.toString())))
            }
            return false
        }

        // Balance check
        if (crate.cost != null && crate.cost.amount > 0) {
            val service = SkiesCrates.INSTANCE.getEconomyService(crate.cost.provider) ?: run {
                handleCrateFail(player, crate, openData)
                Utils.printError("Crate ${crate.id} has an invalid economy provider '${crate.cost.provider}'. Valid providers are: ${SkiesCrates.INSTANCE.getLoadedEconomyServices().keys.joinToString(", ")}")
                Lang.ERROR_ECONOMY_PROVIDER.forEach {
                    player.sendMessage(TextUtils.toComponent(it))
                }
                return false
            }
            if (service.balance(player, crate.cost.currency) < crate.cost.amount) {
                handleCrateFail(player, crate, openData)
                Lang.ERROR_COST.forEach {
                    player.sendMessage(TextUtils.toComponent(it))
                }
                return false
            }
        }

        val playerData = SkiesCrates.INSTANCE.storage?.getUser(player.uuid) ?: run {
            handleCrateFail(player, crate, openData)
            Lang.ERROR_STORAGE.forEach {
                player.sendMessage(TextUtils.toComponent(it))
            }
            return false
        }

        // Check for a cooldown, if one is present
        if (crate.cooldown > 0) {
            val lastOpened = playerData.getCrateCooldown(crate.id)
            if (lastOpened != null) {
                val cooldownTime = lastOpened + (crate.cooldown * 1000)
                if (System.currentTimeMillis() < cooldownTime) {
                    handleCrateFail(player, crate, openData)
                    Lang.ERROR_COOLDOWN.forEach {
                        player.sendMessage(TextUtils.toComponent(it.replace("%cooldown%", Utils.getFormattedTime((cooldownTime - System.currentTimeMillis()) / 1000))))
                    }
                    return false
                }
            }
        }

        // Ensure there are rewards to be given
        if (crate.rewards.isEmpty()) {
            handleCrateFail(player, crate, openData)
            Lang.ERROR_NO_REWARDS.forEach {
                player.sendMessage(TextUtils.toComponent(it))
            }
            return false
        }

        // Check for any keys needed
        if (!isForced && crate.keys.isNotEmpty()) {
            if (!crate.keys.all { (keyId, amount) ->
                val key = ConfigManager.KEYS[keyId] ?: run {
                    Utils.printError("Key $keyId does not exist while opening crate ${crate.id} for ${player.name.string}!")
                    Lang.ERROR_KEY_NOT_FOUND.forEach {
                        player.sendMessage(TextUtils.toComponent(it
                            .replace("%crate_name%", crate.name)
                            .replace("%key_id%", keyId)
                        ))
                    }
                    return@all false
                }
                if (key.virtual) {
                    playerData.keys[keyId]?.let {
                        it >= amount
                    } ?: false
                } else {
                    player.inventory.contains { getKeyOrNull(it)?.id == keyId && it.count >= amount }
                }
            }) {
                handleCrateFail(player, crate, openData)
                Lang.ERROR_MISSING_KEYS.forEach { it ->
                    player.sendMessage(TextUtils.toComponent(it
                        .replace("%crate_name%", crate.name)
                        .replace("%crate_keys%", crate.keys.entries.joinToString(", ") { (keyId, amount) ->
                            "${ConfigManager.KEYS[keyId]?.name ?: keyId} x$amount"
                        })
                    ))
                }
                return false
            }
        }

        // Check for crate item
        if (!isForced && openData.itemStack != null) {
            if (!player.inventory.contains { getCrateOrNull(it)?.id == crate.id }) {
                handleCrateFail(player, crate, openData)
                Lang.ERROR_NO_CRATE.forEach {
                    player.sendMessage(TextUtils.toComponent(it))
                }
                return false
            }
        }

        // Take cost of opening the crate
        if (!isForced) {
            // Remove balance if needed
            if (crate.cost != null && crate.cost.amount > 0) {
                val service = SkiesCrates.INSTANCE.getEconomyService(crate.cost.provider) ?: run {
                    handleCrateFail(player, crate, openData)
                    Utils.printError("Crate ${crate.id} has an invalid economy provider '${crate.cost.provider}'. Valid providers are: ${SkiesCrates.INSTANCE.getLoadedEconomyServices().keys.joinToString(", ")}")
                    Lang.ERROR_ECONOMY_PROVIDER.forEach {
                        player.sendMessage(TextUtils.toComponent(it))
                    }
                    return false
                }
                if (!service.withdraw(player, crate.cost.amount, crate.cost.currency)) {
                    handleCrateFail(player, crate, openData)
                    Lang.ERROR_BALANCE_CHANGED.forEach {
                        player.sendMessage(TextUtils.toComponent(it))
                    }
                    return false
                }
            }

            // Apply a cooldown
            if (crate.cooldown > 0) {
                playerData.addCrateCooldown(crate.id, System.currentTimeMillis())
            }

            // Take keys if needed
            if (crate.keys.isNotEmpty()) {
                // TODO: I dont like this code very much, but need to figure out a better way
                for ((keyId, amount) in crate.keys) {
                    var removed = 0
                    val key = ConfigManager.KEYS[keyId] ?: run {
                        Lang.ERROR_NO_REWARDS.forEach {
                            player.sendMessage(TextUtils.toComponent(it))
                        }
                        return false
                    }

                    if (key.virtual) {
                        if (!playerData.removeKeys(keyId, amount)) {
                            Utils.printError("Failed to remove $amount keys from ${player.name.string} for crate ${crate.id}, but they were present in the check!")
                            Lang.ERROR_KEYS_CHANGED.forEach {
                                player.sendMessage(TextUtils.toComponent(it
                                    .replace("%crate_name%", crate.name)
                                    .replace("%key_id%", keyId)
                                    .replace("%crate_keys%", crate.keys.entries.joinToString(", ") { (keyId, amount) ->
                                        "${ConfigManager.KEYS[keyId]?.name ?: keyId} x$amount"
                                    })
                                ))
                            }
                            return false
                        }
                        removed += amount
                    } else {
                        for ((i, stack) in player.inventory.items.withIndex()) {
                            if (!stack.isEmpty) {
                                if (getKeyOrNull(stack)?.id == keyId) {
                                    val stackSize = stack.count
                                    if (removed + stackSize >= amount) {
                                        player.inventory.items[i].shrink(amount - removed)
                                        removed += (amount - removed)
                                        break
                                    } else {
                                        player.inventory.items[i].shrink(stackSize)
                                        removed += stackSize
                                    }
                                }
                            }
                        }
                    }

                    if (removed != amount) {
                        // This should never happen, but just in case
                        Utils.printError("Somehow the ${player.name.string} had $amount keys on check, but we removed $removed instead!")
                        Lang.ERROR_KEYS_CHANGED.forEach {
                            player.sendMessage(TextUtils.toComponent(it
                                .replace("%crate_name%", crate.name)
                                .replace("%key_id%", keyId)
                                .replace("%crate_keys%", crate.keys.entries.joinToString(", ") { (keyId, amount) ->
                                    "${ConfigManager.KEYS[keyId]?.name ?: keyId} x$amount"
                                })
                            ))
                        }
                        return false
                    }
                }
            }

            // Take crate item, if it was an inventory open
            if (openData.itemStack != null) {
                openData.itemStack.count -= 1
            }
        }

        Lang.CRATE_OPENING.forEach {
            player.sendMessage(TextUtils.toComponent(it
                .replace("%crate_name%", crate.name)
            ))
        }

        playerData.addCrateUse(crate.id)
        SkiesCrates.INSTANCE.storage?.saveUser(player.uuid, playerData)

        if (crate.animation.isEmpty()) {
            // TODO: Update this probably
            val reward = crate.generateReward(player)
            reward.second.giveReward(player, crate)
            return true
        }

        val animation = ConfigManager.ANIMATIONS_INVENTORY[crate.animation] ?: run {
            handleCrateFail(player, crate, openData)
            Lang.ERROR_INVALID_ANIMATION.forEach {
                player.sendMessage(TextUtils.toComponent(it
                    .replace("%crate_name%", crate.name)
                ))
            }
            return false
        }

        openingPlayers.add(player.uuid)
        CrateInventory(player, crate, animation).open()

        return true
    }

    fun previewCrate(player: ServerPlayer, crate: Crate) {
        interactionLimiter[player.uuid]?.let {
            if ((it + ConfigManager.CONFIG.interactionLimiter) > System.currentTimeMillis())
                return
        }

        interactionLimiter[player.uuid] = System.currentTimeMillis()

        val preview = ConfigManager.PREVIEW[crate.preview] ?: run {
            Lang.ERROR_INVALID_PREVIEW.forEach {
                player.sendMessage(TextUtils.toComponent(it
                    .replace("%crate_name%", crate.name)
                ))
            }
            return
        }

        PreviewInventory(player, crate, preview).open()
    }

    private fun handleCrateFail(player: ServerPlayer, crate: Crate, openData: CrateOpenData) {
        crate.failure?.sound?.playSound(player)
        val force = crate.failure?.force ?: return
        if (openData.location != null) {
            val blockPos = openData.location.getBlockPos()
            val sourcePos = blockPos.center
            val playerPos = player.position()

            val direction = Vec3(
                playerPos.x - sourcePos.x,
                playerPos.y - sourcePos.y,
                playerPos.z - sourcePos.z
            ).normalize()

            val velocity = direction.scale(force)
            player.addDeltaMovement(velocity)
            player.hurtMarked = true
        }
    }

    fun getCrateOrNull(itemStack: ItemStack): Crate? {
        val tag = itemStack.get(DataComponents.CUSTOM_DATA) ?: return null
        if (tag.contains(CRATE_IDENTIFIER)) {
            return ConfigManager.CRATES[tag.copyTag().getString(CRATE_IDENTIFIER)]
        }
        return null
    }

    fun getKeyOrNull(itemStack: ItemStack): Key? {
        val tag = itemStack.get(DataComponents.CUSTOM_DATA) ?: return null
        if (tag.contains(KEY_IDENTIFIER)) {
            return ConfigManager.KEYS[tag.copyTag().getString(KEY_IDENTIFIER)]
        }
        return null
    }

    fun getCrateBlock(pos: DimensionalBlockPos): Crate? {
        return locations[pos]
    }
}
