package com.pokeskies.skiescrates.placeholders.type.player

import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.placeholders.GenericResult
import com.pokeskies.skiescrates.placeholders.PlayerPlaceholder
import net.kyori.adventure.text.Component
import net.minecraft.server.level.ServerPlayer

class PlayerKeys : PlayerPlaceholder {
    override fun handle(player: ServerPlayer, args: List<String>): GenericResult {
        val key: String? = args.getOrNull(0)
        if (key.isNullOrBlank()) {
            return GenericResult.invalid(Component.text(
                "Key ID cannot be blank!"
            ))
        }

        return GenericResult.valid(Component.text(SkiesCrates.INSTANCE.getCachedKeys(player.uuid, key)))
    }

    override fun id(): String = "keys"
}
