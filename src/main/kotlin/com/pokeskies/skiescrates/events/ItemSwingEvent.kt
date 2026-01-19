package com.pokeskies.skiescrates.events

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult

fun interface ItemSwingEvent {
    companion object {
        @JvmField
        val EVENT: Event<ItemSwingEvent> =
            EventFactory.createArrayBacked(ItemSwingEvent::class.java) { listeners ->
                ItemSwingEvent { player, hand ->
                    for (listener in listeners) {
                        val result = listener.interact(player, hand)

                        if (result != InteractionResult.PASS) {
                            return@ItemSwingEvent result
                        }
                    }

                    return@ItemSwingEvent InteractionResult.PASS
                }
            }
    }

    fun interact(player: ServerPlayer, hand: InteractionHand): InteractionResult
}