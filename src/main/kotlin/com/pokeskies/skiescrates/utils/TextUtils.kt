package com.pokeskies.skiescrates.utils

import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.placeholders.PlaceholderManager
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object TextUtils {
    fun toNative(text: String): Component {
        return SkiesCrates.INSTANCE.adventure.toNative(SkiesCrates.MINI_MESSAGE.deserialize(text))
    }

    fun toComponent(text: String): net.kyori.adventure.text.Component {
        return SkiesCrates.MINI_MESSAGE.deserialize(text)
    }

    fun parseAll(player: ServerPlayer, text: String): Component {
        return toNative(PlaceholderManager.parse(player, text))
    }
}
