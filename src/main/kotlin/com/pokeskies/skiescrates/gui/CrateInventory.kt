package com.pokeskies.skiescrates.gui

import com.pokeskies.skiescrates.data.Crate
import com.pokeskies.skiescrates.data.animations.InventoryAnimation
import com.pokeskies.skiescrates.data.animations.spinners.AnimatedSpinnerInstance
import com.pokeskies.skiescrates.data.animations.spinners.RewardSpinnerInstance
import com.pokeskies.skiescrates.data.rewards.Reward
import com.pokeskies.skiescrates.managers.CratesManager.openingPlayers
import com.pokeskies.skiescrates.utils.RandomCollection
import com.pokeskies.skiescrates.utils.TextUtils
import com.pokeskies.skiescrates.utils.Utils
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

class CrateInventory(player: ServerPlayer, val crate: Crate, val animation: InventoryAnimation): SimpleGui(
    animation.settings.menuType.type, player, false
) {
    private var isFinished = false
    private var closeTicks = 0

    // This is a map of "Animated Preset ID" to a RandomCollection of ItemStacks for that preset
    private val cachedAnimatedPresets: MutableMap<String, RandomCollection<ItemStack>> = mutableMapOf()
    private var animatedSpinners: MutableMap<String, AnimatedSpinnerInstance> = mutableMapOf()

    private var cachedRewardStacks: MutableMap<String, ItemStack> = mutableMapOf()
    private var rewardSpinners: MutableMap<String, RewardSpinnerInstance> = mutableMapOf()

    init {
        this.title = TextUtils.parseAll(player, crate.parsePlaceholders(animation.settings.title))

        animation.items.static.forEach { (id, item) ->
            item.slots.forEach { slot ->
                this.setSlot(slot, item.createItemStack(player))
            }
        }

        // Setup animated spinners
        animation.presets.animations.forEach { (id, presetItems) ->
            val collection = RandomCollection<ItemStack>()
            presetItems.forEach { animatedItem ->
                val itemStack = animatedItem.createItemStack(player)
                collection.add(animatedItem.weight.toDouble(), itemStack)
            }
            cachedAnimatedPresets[id] = collection
        }
        animation.items.animated.forEach { (id, spinningItem) ->
            if (spinningItem.preset.isEmpty()) return@forEach
            val bag = cachedAnimatedPresets[spinningItem.preset] ?: run {
                Utils.printError("Animated preset ${spinningItem.preset} not found for spinner $id")
                return@forEach
            }

            animatedSpinners[id] = AnimatedSpinnerInstance(spinningItem, bag)
        }

        // Setup rewards spinners
        crate.rewards.forEach { (id, reward) ->
            cachedRewardStacks[id] = reward.display.createItemStack(player)
        }
        animation.items.rewards.forEach { (id, item) ->
            rewardSpinners[id] = RewardSpinnerInstance(item)
        }
    }

    override fun onTick() {
        if (isFinished) {
            if (closeTicks++ >= animation.settings.closeDelay) {
                this.close()
                return
            }
        } else {
            var allCompleted = true
            rewardSpinners.forEach { (id, spinner) ->
                if (spinner.isCompleted()) {
                    return@forEach
                }
                allCompleted = false
                spinner.tick(player, this)
            }

            if (allCompleted) {
                isFinished = true
                rewardSpinners.forEach { (id, data) ->
                    data.giveRewards(player, animation.settings.winSlots, crate)
                }
            }
        }

        animatedSpinners.forEach { (id, spinner) ->
            if (spinner.isCompleted()) return@forEach
            spinner.tick(player, this)
        }
    }

    fun updateRewardSlot(slot: Int, reward: Pair<String, Reward>) {
        this.setSlot(slot, cachedRewardStacks[reward.first] ?: reward.second.display.createItemStack(player))
    }

    override fun onClose() {
        if (!isFinished) {
            this.open()
            return
        }

        openingPlayers.remove(player.uuid)
    }
}
