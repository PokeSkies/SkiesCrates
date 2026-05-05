package com.pokeskies.skiescrates.events

import com.pokeskies.skiescrates.data.Crate
import com.pokeskies.skiescrates.data.CrateOpenData
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult

fun interface CrateInteractionEvent {
    companion object {
        @JvmField
        val EVENT: Event<CrateInteractionEvent> =
            EventFactory.createArrayBacked(CrateInteractionEvent::class.java) { listeners ->
                CrateInteractionEvent { player, itemStack, openData ->
                    for (listener in listeners) {
                        val result = listener.interact(player, itemStack, openData)

                        if (result != InteractionResult.PASS) {
                            return@CrateInteractionEvent result
                        }
                    }

                    return@CrateInteractionEvent InteractionResult.PASS
                }
            }
    }

    fun interact(player: ServerPlayer, crate: Crate, openData: CrateOpenData): InteractionResult

    enum class InteractionType {
        LEFT_CLICK,
        RIGHT_CLICK,
        SHIFT_LEFT_CLICK,
        SHIFT_RIGHT_CLICK,
        DROP
    }
}