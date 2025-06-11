package com.pokeskies.skiescrates.managers

import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.config.ConfigManager
import com.pokeskies.skiescrates.config.lang.Lang
import com.pokeskies.skiescrates.data.Crate
import com.pokeskies.skiescrates.data.CrateOpenData
import com.pokeskies.skiescrates.data.DimensionalBlockPos
import com.pokeskies.skiescrates.data.Key
import com.pokeskies.skiescrates.gui.CrateInventory
import com.pokeskies.skiescrates.gui.PreviewInventory
import com.pokeskies.skiescrates.utils.MinecraftDispatcher
import com.pokeskies.skiescrates.utils.TextUtils
import com.pokeskies.skiescrates.utils.Utils
import kotlinx.coroutines.withContext
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.phys.Vec3
import java.util.*
import java.util.concurrent.CompletableFuture

object CratesManager {
    const val CRATE_IDENTIFIER: String = "${SkiesCrates.MOD_ID}:crate"
    const val KEY_IDENTIFIER: String = "${SkiesCrates.MOD_ID}:key"

    val locations: MutableMap<DimensionalBlockPos, Crate> = mutableMapOf()
    val openingPlayers: MutableList<UUID> = mutableListOf()
    private val interactionLimiter = mutableMapOf<UUID, Long>()

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
        // TODO: interaction limiter cleanup
    }

    fun giveCrates(crate: Crate, player: ServerPlayer, amount: Int, silent: Boolean = false): Boolean {
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
                player.sendMessage(TextUtils.parseAll(player, crate.parsePlaceholders(it)))
            }
        }

        return true
    }

    fun giveKeys(key: Key, player: ServerPlayer, amount: Int, silent: Boolean = false): CompletableFuture<Boolean> {
        if (key.virtual) {
            val storage = SkiesCrates.INSTANCE.storage ?: run {
                Utils.printError("Storage was null while attempting give ${player.name.string} ${amount}x ${key.name} keys! Check elsewhere for errors.")
                Lang.ERROR_STORAGE.forEach {
                    player.sendMessage(TextUtils.toNative(it))
                }
                return CompletableFuture.completedFuture(false)
            }

            return storage.getUserAsync(player.uuid)
                .thenCompose { playerData ->
                    playerData.addKeys(key.id, amount)
                    storage.saveUserAsync(player.uuid, playerData)
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
                    Utils.printError("Storage was null while attempting save ${player.name.string}'s userdata while giving them keys! Check elsewhere for errors.")
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
            val storage = SkiesCrates.INSTANCE.storage ?: run {
                Utils.printError("Storage was null while attempting take ${amount}x ${key.name} keys from ${player.name.string}! Check elsewhere for errors.")
                Lang.ERROR_STORAGE.forEach {
                    player.sendMessage(TextUtils.toNative(it))
                }
                return CompletableFuture.completedFuture(false)
            }

            return storage.getUserAsync(player.uuid)
                .thenCompose { playerData ->
                    if (!playerData.removeKeys(key.id, amount)) {
                        CompletableFuture.completedFuture(false)
                    } else {
                        storage.saveUserAsync(player.uuid, playerData)
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
            val storage = SkiesCrates.INSTANCE.storage ?: run {
                Utils.printError("Storage was null while attempting set ${amount}x ${key.name} keys for ${player.name.string}! Check elsewhere for errors.")
                Lang.ERROR_STORAGE.forEach {
                    player.sendMessage(TextUtils.toNative(it))
                }
                return CompletableFuture.completedFuture(false)
            }

            return storage.getUserAsync(player.uuid)
                .thenCompose { playerData ->
                    playerData.setKeys(key.id, amount)
                    storage.saveUserAsync(player.uuid, playerData)
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

    // This method is massive, but it handles a lot of things!
    suspend fun openCrate(player: ServerPlayer, crate: Crate, openData: CrateOpenData, isForced: Boolean): Boolean {
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
                player.sendMessage(TextUtils.parseAll(player, crate.parsePlaceholders(
                    it
                )))
            }
            return false
        }

        // Permission check
        if (!isForced && crate.permission.isNotEmpty() && !Permissions.check(player, crate.permission)) {
            handleCrateFail(player, crate, openData)
            Lang.ERROR_NO_PERMISSION.forEach {
                player.sendMessage(TextUtils.parseAll(player, crate.parsePlaceholders(
                    it
                )))
            }
            return false
        }

        // Inventory space check
        if (!isForced && crate.inventorySpace > 0 && player.inventory.items.count { it.isEmpty } >= crate.inventorySpace) {
            handleCrateFail(player, crate, openData)
            Lang.ERROR_INVENTORY_SPACE.forEach {
                player.sendMessage(TextUtils.parseAll(player, crate.parsePlaceholders(
                    it
                )))
            }
            return false
        }

        // Balance check
        if (crate.cost != null && crate.cost.amount > 0) {
            val service = SkiesCrates.INSTANCE.getEconomyService(crate.cost.provider) ?: run {
                handleCrateFail(player, crate, openData)
                Utils.printError("Crate ${crate.id} has an invalid economy provider '${crate.cost.provider}'. Valid providers are: ${SkiesCrates.INSTANCE.getLoadedEconomyServices().keys.joinToString(", ")}")
                Lang.ERROR_ECONOMY_PROVIDER.forEach {
                    player.sendMessage(TextUtils.parseAll(player, crate.parsePlaceholders(
                        it
                    )))
                }
                return false
            }
            if (service.balance(player, crate.cost.currency) < crate.cost.amount) {
                handleCrateFail(player, crate, openData)
                Lang.ERROR_COST.forEach {
                    player.sendMessage(TextUtils.parseAll(player, crate.parsePlaceholders(
                        it
                    )))
                }
                return false
            }
        }

        val storage = SkiesCrates.INSTANCE.storage ?: run {
            handleCrateFail(player, crate, openData)
            Utils.printError("Storage was null while attempting to open a ${crate.name} crate for ${player.name.string}! Check elsewhere for errors.")
            Lang.ERROR_STORAGE.forEach {
                player.sendMessage(TextUtils.parseAll(player, crate.parsePlaceholders(
                    it
                )))
            }
            return false
        }

        val playerData = storage.getUser(player.uuid)

        // Check for a cooldown, if one is present
        if (crate.cooldown > 0) {
            val lastOpened = playerData.getCrateCooldown(crate.id)
            if (lastOpened != null) {
                val cooldownTime = lastOpened + (crate.cooldown * 1000)
                if (System.currentTimeMillis() < cooldownTime) {
                    withContext(MinecraftDispatcher(player.server)) {
                        handleCrateFail(player, crate, openData)
                        Lang.ERROR_COOLDOWN.forEach {
                            player.sendMessage(TextUtils.parseAll(player, crate.parsePlaceholders(
                                it.replace("%cooldown%", Utils.getFormattedTime((cooldownTime - System.currentTimeMillis()) / 1000))
                            )))
                        }
                    }
                    return false
                }
            }
        }

        // Ensure there are rewards to be given
        if (crate.rewards.isEmpty()) {
            withContext(MinecraftDispatcher(player.server)) {
                handleCrateFail(player, crate, openData)
                Lang.ERROR_NO_REWARDS.forEach {
                    player.sendMessage(TextUtils.parseAll(player, crate.parsePlaceholders(it)))
                }
            }
            return false
        }

        // Check for any keys needed
        if (!isForced && crate.keys.isNotEmpty()) {
            if(!withContext(MinecraftDispatcher(player.server)) {
                if (!crate.keys.all { (keyId, amount) ->
                    val key = ConfigManager.KEYS[keyId] ?: run {
                        Utils.printError("Key $keyId does not exist while opening crate ${crate.id} for ${player.name.string}!")
                        Lang.ERROR_KEY_NOT_FOUND.forEach {
                            player.sendMessage(TextUtils.parseAll(player, crate.parsePlaceholders(
                                it.replace("%key_id%", keyId)
                            )))
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
                    Lang.ERROR_MISSING_KEYS.forEach {
                        player.sendMessage(TextUtils.parseAll(player, crate.parsePlaceholders(
                            it
                        )))
                    }
                    return@withContext false
                }
                true
            }) return false
        }

        // Check for crate item
        if (!isForced && openData.itemStack != null) {
            var contains = true
            withContext(MinecraftDispatcher(player.server)) {
                if (!player.inventory.contains { getCrateOrNull(it)?.id == crate.id }) {
                    contains = false
                    handleCrateFail(player, crate, openData)
                    Lang.ERROR_NO_CRATE.forEach {
                        player.sendMessage(TextUtils.parseAll(player, crate.parsePlaceholders(
                            it
                        )))
                    }
                }
            }
            if (!contains) return false
        }

        // Take cost of opening the crate
        if (!isForced) {
            // Remove balance if needed
            if (crate.cost != null && crate.cost.amount > 0) {
                val service = SkiesCrates.INSTANCE.getEconomyService(crate.cost.provider) ?: run {
                    withContext(MinecraftDispatcher(player.server)) {
                        handleCrateFail(player, crate, openData)
                        Utils.printError("Crate ${crate.id} has an invalid economy provider '${crate.cost.provider}'. Valid providers are: ${SkiesCrates.INSTANCE.getLoadedEconomyServices().keys.joinToString(", ")}")
                        Lang.ERROR_ECONOMY_PROVIDER.forEach {
                            player.sendMessage(TextUtils.parseAll(player, crate.parsePlaceholders(
                                it
                            )))
                        }
                    }
                    return false
                }
                if (!service.withdraw(player, crate.cost.amount, crate.cost.currency)) {
                    withContext(MinecraftDispatcher(player.server)) {
                        handleCrateFail(player, crate, openData)
                        Lang.ERROR_BALANCE_CHANGED.forEach {
                            player.sendMessage(
                                TextUtils.parseAll(
                                    player, crate.parsePlaceholders(
                                        it
                                    )
                                )
                            )
                        }
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
                if (!withContext(MinecraftDispatcher(player.server)) {
                    for ((keyId, amount) in crate.keys) {
                        var removed = 0
                        val key = ConfigManager.KEYS[keyId] ?: run {
                            Lang.ERROR_NO_REWARDS.forEach {
                                player.sendMessage(TextUtils.parseAll(player, crate.parsePlaceholders(
                                    it
                                )))
                            }
                            return@withContext false
                        }

                        if (key.virtual) {
                            if (!playerData.removeKeys(keyId, amount)) {
                                Utils.printError("Failed to remove $amount keys from ${player.name.string} for crate ${crate.id}, but they were present in the check!")
                                Lang.ERROR_KEYS_CHANGED.forEach {
                                    player.sendMessage(TextUtils.parseAll(player, crate.parsePlaceholders(
                                        it.replace("%key_id%", keyId)
                                    )))
                                }
                                return@withContext false
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
                                player.sendMessage(TextUtils.parseAll(player, crate.parsePlaceholders(
                                    it.replace("%key_id%", keyId)
                                )))
                            }
                            return@withContext false
                        }
                    }
                    return@withContext true
                }) return false
            }

            // Take crate item, if it was an inventory open
            if (openData.itemStack != null) {
                withContext(MinecraftDispatcher(player.server)) {
                    openData.itemStack.count -= 1
                }
            }
        }

        playerData.addCrateUse(crate.id)
        storage.saveUserAsync(player.uuid, playerData)

        Lang.CRATE_OPENING.forEach {
            player.sendMessage(TextUtils.parseAll(player, crate.parsePlaceholders(
                it
            )))
        }

        return withContext(MinecraftDispatcher(player.server)) {
            if (crate.animation.isEmpty()) {
                // TODO: Update this probably
                val reward = crate.generateReward(player)
                reward.second.giveReward(player, crate)
                return@withContext true
            }

            val animation = ConfigManager.ANIMATIONS_INVENTORY[crate.animation] ?: run {
                handleCrateFail(player, crate, openData)
                Lang.ERROR_INVALID_ANIMATION.forEach {
                    player.sendMessage(TextUtils.parseAll(player, crate.parsePlaceholders(
                        it
                    )))
                }
                return@withContext false
            }

            openingPlayers.add(player.uuid)
            CrateInventory(player, crate, animation).open()
            return@withContext true
        }
    }

    fun previewCrate(player: ServerPlayer, crate: Crate) {
        interactionLimiter[player.uuid]?.let {
            if ((it + ConfigManager.CONFIG.interactionLimiter) > System.currentTimeMillis()) {
                return
            }
        }

        interactionLimiter[player.uuid] = System.currentTimeMillis()

        val preview = ConfigManager.PREVIEW[crate.preview] ?: run {
            Lang.ERROR_INVALID_PREVIEW.forEach {
                player.sendMessage(TextUtils.parseAll(player, crate.parsePlaceholders(
                    it
                )))
            }
            return
        }
        Utils.printInfo("DEBUGGING @ CratesManager#previewCrate - Preview found, opening")

        PreviewInventory(player, crate, preview).open()
    }

    private fun handleCrateFail(player: ServerPlayer, crate: Crate, openData: CrateOpenData) {
        crate.failure?.sound?.playSound(player)
        val force = crate.failure?.pushback ?: return
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

        // Migration from other crate mods
        return ConfigManager.CONFIG.migration.keys?.firstNotNullOfOrNull { instance ->
            val key = ConfigManager.KEYS[instance.key] ?: run {
                Utils.printError("Migration Key ${instance.key} did not exist while attempting to find valid keys from player!")
                return@firstNotNullOfOrNull null
            }

            if (tag.contains(instance.nbt.key)) {
                val value = tag.copyTag().getString(instance.nbt.key)
                if (value != null && value.equals(instance.nbt.value)) {
                    return key
                }
            }
            null
        }
    }

    fun getCrateBlock(pos: DimensionalBlockPos): Crate? {
        return locations[pos]
    }
}
