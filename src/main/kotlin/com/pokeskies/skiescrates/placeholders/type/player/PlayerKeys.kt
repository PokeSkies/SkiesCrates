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

        val playerData = SkiesCrates.INSTANCE.storage?.getUser(player.uuid) ?: run {
            return GenericResult.invalid(Component.text(
                "Storage Error!"
            ))
        }

        return GenericResult.valid(Component.text(playerData.keys[key] ?: 0))
    }

    override fun id(): String = "keys"
}
