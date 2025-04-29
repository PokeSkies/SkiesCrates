package com.pokeskies.skiescrates.gui

import com.pokeskies.skiescrates.data.Crate
import com.pokeskies.skiescrates.data.previews.Preview
import com.pokeskies.skiescrates.data.rewards.Reward
import com.pokeskies.skiescrates.utils.TextUtils
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.alchemy.PotionContents.createItemStack

class PreviewInventory(player: ServerPlayer, val crate: Crate, val preview: Preview): SimpleGui(
    preview.settings.menuType.type, player, false
) {
    // This is a map because we can implement interesting interaction features in the future
    private val rewards: MutableMap<String, Pair<Reward, ItemStack>> = mutableMapOf()

    private var page = 0
    private var maxPages = 1

    init {
        this.title = TextUtils.toNative(crate.parsePlaceholder(preview.settings.title))

        preview.items.forEach { (id, item) ->
            item.slots.forEach { slot ->
                this.setSlot(slot, item.createItemStack(player))
            }
        }

        crate.rewards.forEach { (id, reward) ->
            rewards[id] = reward to preview.buttons.reward.createItemStack(player, id, reward)
        }

        maxPages = (rewards.size / (preview.buttons.reward.slots.size + 1)) + 1

        preview.buttons.close?.let { it.slots.forEach { slot ->
            this.setSlot(slot, GuiElementBuilder.from(it.createItemStack(player))
                .setCallback { i, clickType, vanillaClickType ->
                    this.close()
                })
        } }
        preview.buttons.pageNext?.let { it.slots.forEach { slot ->
            this.setSlot(slot, GuiElementBuilder.from(it.createItemStack(player))
                .setCallback { i, clickType, vanillaClickType ->
                    if (page < maxPages - 1) {
                        page++
                        renderRewards()
                    }
                })
        } }
        preview.buttons.pagePrevious?.let { it.slots.forEach { slot ->
            this.setSlot(slot, GuiElementBuilder.from(it.createItemStack(player))
                .setCallback { i, clickType, vanillaClickType ->
                    if (page > 0) {
                        page--
                        renderRewards()
                    }
                })
        } }

        renderRewards()
    }

    private fun renderRewards() {
        var index = 0
        for ((id, pair) in rewards.toList().subList(preview.buttons.reward.slots.size * page, rewards.size)) {
            if (index < preview.buttons.reward.slots.size) {
                this.setSlot(
                    preview.buttons.reward.slots[index++],
                    GuiElementBuilder.from(pair.second)
                )
            }
        }
    }

    fun parsePlaceholders(string: String): String {
        return string.replace("%player%", player.name.string)
            .replace("%crate_name%", crate.name)
    }
}
