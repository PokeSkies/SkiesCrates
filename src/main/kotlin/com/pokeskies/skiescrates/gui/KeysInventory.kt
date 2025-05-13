package com.pokeskies.skiescrates.gui

import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.config.ConfigManager
import com.pokeskies.skiescrates.config.lang.Lang
import com.pokeskies.skiescrates.utils.TextUtils
import com.pokeskies.skiescrates.utils.Utils
import eu.pb4.sgui.api.gui.SimpleGui
import kotlinx.coroutines.runBlocking
import net.minecraft.server.level.ServerPlayer
import java.util.concurrent.CompletableFuture

class KeysInventory(viewer: ServerPlayer, private val target: ServerPlayer): SimpleGui(
    ConfigManager.KEYS_MENU.menuType.type, viewer, false
) {
    private val keysMenu = ConfigManager.KEYS_MENU

    init {
        this.title = TextUtils.parseAll(player, keysMenu.title)
        refresh()
    }

    private fun refresh() {
        val storage = SkiesCrates.INSTANCE.storage ?: run {
            Lang.ERROR_STORAGE.forEach {
                player.sendMessage(TextUtils.toNative(it))
            }
            close()
            return
        }

        keysMenu.items.forEach { (_, item) ->
            item.createItemStack(player).let {
                item.slots.forEach { slot ->
                    this.setSlot(slot, it)
                }
            }
        }

        storage.getUserAsync(target.uuid).thenAccept { playerData ->
            keysMenu.keys.forEach { (id, item) ->
                val key = ConfigManager.KEYS[id] ?: run {
                    Utils.printError("Key $id could not be found while opening keys menu!")
                    return@forEach
                }
                item.createItemStack(player, key, playerData.keys[id] ?: 0).let {
                    item.slots.forEach { slot ->
                        this.setSlot(slot, it)
                    }
                }
            }
        }.exceptionally {
            Utils.printError("Player data for ${target.name} could not be found while opening keys menu!")
            Lang.ERROR_STORAGE.forEach {
                player.sendMessage(TextUtils.toNative(it))
            }
            close()
            return@exceptionally null
        }
    }
}
