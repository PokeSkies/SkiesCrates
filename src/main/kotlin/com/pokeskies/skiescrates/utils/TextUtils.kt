package com.pokeskies.skiescrates.utils

import com.pokeskies.skiescrates.SkiesCrates
import net.minecraft.network.chat.Component

object TextUtils {
    fun toNative(text: String): Component {
        return SkiesCrates.INSTANCE.adventure.toNative(SkiesCrates.MINI_MESSAGE.deserialize(text))
    }

    fun toComponent(text: String): net.kyori.adventure.text.Component {
        return SkiesCrates.MINI_MESSAGE.deserialize(text)
    }
}
