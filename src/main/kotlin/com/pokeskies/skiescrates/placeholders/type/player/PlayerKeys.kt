package com.pokeskies.skiescrates.placeholders.type.player

import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.data.KeyCacheKey
import com.pokeskies.skiescrates.placeholders.GenericResult
import com.pokeskies.skiescrates.placeholders.PlayerPlaceholder
import com.pokeskies.skiescrates.utils.Utils
import net.kyori.adventure.text.Component
import net.minecraft.server.level.ServerPlayer

class PlayerKeys : PlayerPlaceholder {
    override fun handle(player: ServerPlayer, args: List<String>): GenericResult {
        val keyId = args.firstOrNull() ?: return GenericResult.invalid(Component.text("Key ID Required"))
        return GenericResult.valid(Component.text(SkiesCrates.INSTANCE.getCachedKeys(player.uuid, keyId).toString()))
    }

    override fun id(): String = "keys"
}
