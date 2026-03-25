package com.pokeskies.skiescrates.utils

import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.placeholders.PlaceholderManager
import com.pokeskies.skiescrates.utils.TextUtils.plainSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object TextUtils {
    val plainSerializer = PlainTextComponentSerializer.plainText()
}

fun String.asNative(): Component {
    return SkiesCrates.INSTANCE.adventure.toNative(SkiesCrates.MINI_MESSAGE.deserialize(this))
}

fun String.asNative(player: ServerPlayer, placeholders: Map<String, String> = emptyMap()): Component {
    return PlaceholderManager.parse(player, this, placeholders).asNative()
}

fun net.kyori.adventure.text.Component.asNative(): Component {
    return SkiesCrates.INSTANCE.adventure.toNative(this)
}

fun String.asAdventure(): net.kyori.adventure.text.Component {
    return SkiesCrates.MINI_MESSAGE.deserialize(this)
}

fun String.asAdventure(player: ServerPlayer, placeholders: Map<String, String> = emptyMap()): net.kyori.adventure.text.Component {
    return PlaceholderManager.parse(player, this, placeholders).asAdventure()
}

fun net.kyori.adventure.text.Component.asPlain(): String {
    return plainSerializer.serialize(this)
}

fun Component.asPlain(): String {
    return plainSerializer.serialize(this.asComponent())
}