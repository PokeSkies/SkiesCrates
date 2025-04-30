package com.pokeskies.skiescrates.gui

import com.pokeskies.skiescrates.data.Crate
import com.pokeskies.skiescrates.data.previews.Preview
import com.pokeskies.skiescrates.data.rewards.Reward
import com.pokeskies.skiescrates.utils.TextUtils
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

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
            item.createItemStack(player).let {
                item.slots.forEach { slot ->
                    this.setSlot(slot, it)
                }
            }
        }

        crate.rewards.forEach { (id, reward) ->
            rewards[id] = reward to preview.buttons.reward.createItemStack(player, id, reward, crate)
        }

        maxPages = (rewards.size / (preview.buttons.reward.slots.size + 1)) + 1

        preview.buttons.close?.let { item ->
            GuiElementBuilder.from(item.createItemStack(player))
                .setCallback { i, clickType, vanillaClickType ->
                    this.close()
                }.let {
                    item.slots.forEach { slot ->
                        this.setSlot(slot, it)
                    }
                }
            }
        preview.buttons.pageNext?.let { item ->
            item.slots.forEach { slot ->
                GuiElementBuilder.from(item.createItemStack(player))
                    .setCallback { i, clickType, vanillaClickType ->
                        if (page < maxPages - 1) {
                            page++
                            renderRewards()
                        }
                    }.let {
                        this.setSlot(slot, it)
                    }
            }
        }
        preview.buttons.pagePrevious?.let { item ->
            item.slots.forEach { slot ->
                GuiElementBuilder.from(item.createItemStack(player))
                    .setCallback { i, clickType, vanillaClickType ->
                        if (page > 0) {
                            page--
                            renderRewards()
                        }
                    }.let {
                        this.setSlot(slot, it)
                    }
            }
        }

        renderRewards()
    }

    private fun renderRewards() {
        var index = 0
        for ((id, pair) in rewards.toList().subList(preview.buttons.reward.slots.size * page, rewards.size)) {
            if (index < preview.buttons.reward.slots.size) {
                GuiElementBuilder.from(pair.second).let {
                    this.setSlot(preview.buttons.reward.slots[index++], it)
                }
            }
        }
    }

    fun parsePlaceholders(string: String): String {
        return string.replace("%player%", player.name.string)
            .replace("%crate_name%", crate.name)
    }
}
