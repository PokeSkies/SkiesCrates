package com.pokeskies.skiescrates.placeholders

import com.pokeskies.skiescrates.placeholders.services.DefaultPlaceholderService
import com.pokeskies.skiescrates.placeholders.services.ImpactorPlaceholderService
import com.pokeskies.skiescrates.placeholders.services.MiniPlaceholdersService
import com.pokeskies.skiescrates.placeholders.services.PlaceholderAPIService
import com.pokeskies.skiescrates.placeholders.type.player.PlayerKeys
import net.minecraft.server.level.ServerPlayer
import java.util.stream.Stream

object PlaceholderManager {
    private val services: MutableList<IPlaceholderService> = mutableListOf()

    fun init() {
        services.add(DefaultPlaceholderService())
        for (service in PlaceholderMods.entries) {
            if (service.isModPresent()) {
                services.add(getServiceForType(service))
            }
        }
        registerPlaceholders()
    }

    private fun registerPlaceholders() {
        // SERVER PLACEHOLDERS
//        Stream.of(
//          None yet :)
//        ).forEach { placeholder -> services.forEach { it.registerServer(placeholder) } }

        // PLAYER PLACEHOLDERS
        Stream.of(
            PlayerKeys()
        ).forEach { placeholder -> services.forEach { it.registerPlayer(placeholder) } }

        services.forEach { it.finalizeRegister() }
    }

    fun parse(player: ServerPlayer, text: String): String {
        var returnValue = text
        for (service in services) {
            returnValue = service.parsePlaceholders(player, returnValue)
        }
        return returnValue
    }

    private fun getServiceForType(placeholderMod: PlaceholderMods): IPlaceholderService {
        return when (placeholderMod) {
            PlaceholderMods.IMPACTOR -> ImpactorPlaceholderService()
            PlaceholderMods.PLACEHOLDERAPI -> PlaceholderAPIService()
            PlaceholderMods.MINIPLACEHOLDERS -> MiniPlaceholdersService()
        }
    }
}
