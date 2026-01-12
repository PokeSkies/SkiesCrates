package com.pokeskies.skiescrates.data.previews

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.config.item.MenuItem
import com.pokeskies.skiescrates.data.Crate
import com.pokeskies.skiescrates.data.rewards.Reward
import com.pokeskies.skiescrates.data.userdata.UserData
import com.pokeskies.skiescrates.gui.InventoryType
import com.pokeskies.skiescrates.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiescrates.utils.TextUtils
import com.pokeskies.skiescrates.utils.Utils
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore

class Preview(
    val settings: Settings,
    val buttons: PreviewButtons,
    val items: MutableMap<String, MenuItem>,
) {
    // This is the general settings for this inventory animation
    class Settings(
        val title: String,
        @SerializedName("menu_type")
        val menuType: InventoryType
    )

    class PreviewButtons(
        val reward: RewardButton,
        @SerializedName("page_next")
        val pageNext: MenuItem?,
        @SerializedName("page_previous")
        val pagePrevious: MenuItem?,
        val close: MenuItem?
    )

    class RewardButton(
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val slots: List<Int> = emptyList(),
        val name: String? = null,
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val lore: List<String> = emptyList()
    ) {
        fun createItemStack(player: ServerPlayer, reward: Reward, crate: Crate, userData: UserData): ItemStack {
            val guiItem = reward.preview ?: run {
                return reward.getDisplayItem(player, reward.getPlaceholders(userData, crate))
            }

            if (guiItem.item.isEmpty()) return ItemStack(Items.BARRIER, guiItem.amount)

            val parsedItem = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(guiItem.item))

            if (parsedItem.isEmpty) {
                Utils.printError("Error while getting Item, defaulting to Barrier: ${guiItem.item}")
                return ItemStack(Items.BARRIER, guiItem.amount)
            }

            val stack = ItemStack(parsedItem.get(), guiItem.amount)

            val placeholders = reward.getPlaceholders(userData, crate)

            if (guiItem.components != null) {
                // Parses the nbt and attempts to replace any placeholders
                val nbtCopy = guiItem.components.copy()
                for (key in guiItem.components.allKeys) {
                    val element = guiItem.components.get(key)
                    if (element != null) {
                        if (element is StringTag) {
                            nbtCopy.putString(key, element.asString)
                        } else if (element is ListTag) {
                            val parsed = ListTag()
                            for (entry in element) {
                                if (entry is StringTag) {
                                    parsed.add(StringTag.valueOf(entry.asString))
                                } else {
                                    parsed.add(entry)
                                }
                            }
                            nbtCopy.put(key, parsed)
                        }
                    }
                }

                DataComponentPatch.CODEC.decode(SkiesCrates.INSTANCE.nbtOpts, nbtCopy).result().ifPresent { result ->
                    stack.applyComponents(result.first)
                }
            }

            val dataComponents = DataComponentPatch.builder()

            (reward.preview.name ?: name)?.let { name ->
                dataComponents.set(
                    DataComponents.ITEM_NAME, Component.empty().setStyle(Style.EMPTY.withItalic(false))
                        .append(TextUtils.toNative(
                            name.let {  placeholders.entries.fold(it) { acc, (key, value) -> acc.replace(key, value) } }
                        )))
            }

            (reward.preview.lore ?: lore).let { lore ->
                if (lore.isNotEmpty()) {
                    val parsedLore: MutableList<String> = mutableListOf()
                    for (line in lore.stream().map { it }.toList()) {
                        val parsedLine = line.let { placeholders.entries.fold(it) { acc, (key, value) -> acc.replace(key, value) } }
                        if (parsedLine.contains("\n")) {
                            parsedLine.split("\n").forEach { parsedLore.add(it) }
                        } else {
                            parsedLore.add(parsedLine)
                        }
                    }
                    dataComponents.set(DataComponents.LORE, ItemLore(parsedLore.stream().map {
                        Component.empty().setStyle(Style.EMPTY.withItalic(false)).append(TextUtils.toNative(
                            it
                        )) as Component
                    }.toList()))
                }
            }

            stack.applyComponents(dataComponents.build())

            return stack
        }
    }
}
