package com.pokeskies.skiescrates.integrations.bil

import com.pokeskies.skiescrates.config.block.ModelOptions
import com.pokeskies.skiescrates.data.CrateInstance
import com.pokeskies.skiescrates.data.CrateOpenData
import com.pokeskies.skiescrates.events.CrateInteractionEvent
import com.pokeskies.skiescrates.integrations.ModIntegration
import com.pokeskies.skiescrates.utils.Utils
import de.tomalbrc.bil.core.holder.positioned.PositionedHolder
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement.InteractionHandler
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.phys.Vec3

class BILCrateData(
    var holder: PositionedHolder
) {
    fun isAttached(): Boolean {
        return holder.attachment != null
    }

    companion object {
        fun create(instance: CrateInstance, chunk: LevelChunk?, modelOptions: ModelOptions): BILCrateData? {
            val integration = ModIntegration.BIL.getIntegration() as? BILIntegration ?: run {
                Utils.printError("BIL Integration is not initialized!")
                return null
            }

            val model = integration.getModel(modelOptions.id) ?: run {
                Utils.printError("The crate '${instance.crate.id}' is using a model '${modelOptions.id}' which could not be found!")
                return null
            }

            val holder = CrateModelHolder(
                instance.level,
                instance.pos.bottomCenter,
                model,
                modelOptions.rotation,
                modelOptions.offset
            )
            holder.scale = modelOptions.scale
            modelOptions.animations.idle?.let { holder.animator.playAnimation(it) }
            val element = InteractionElement()
            element.setSize(modelOptions.hitbox.width, modelOptions.hitbox.height)
            element.offset = modelOptions.hitbox.offset
            element.setHandler(object : InteractionHandler {
                override fun interactAt(player: ServerPlayer, hand: InteractionHand, pos: Vec3) {
                    CrateInteractionEvent.EVENT.invoker().interact(player, instance.crate, CrateOpenData(
                        instance.dimPos,
                        null,
                        if (player.isShiftKeyDown) CrateInteractionEvent.InteractionType.SHIFT_RIGHT_CLICK else CrateInteractionEvent.InteractionType.RIGHT_CLICK
                    ))
                }

                override fun attack(player: ServerPlayer) {
                    CrateInteractionEvent.EVENT.invoker().interact(player, instance.crate, CrateOpenData(
                        instance.dimPos,
                        null,
                        if (player.isShiftKeyDown) CrateInteractionEvent.InteractionType.SHIFT_LEFT_CLICK else CrateInteractionEvent.InteractionType.LEFT_CLICK
                    ))
                }
            })
            holder.addElement(element)

            val pos = Vec3.atCenterOf(instance.pos)
            if (chunk != null) {
                ChunkAttachment(
                    holder,
                    chunk,
                    pos,
                    true
                )
            }

            return BILCrateData(holder)
        }
    }
}