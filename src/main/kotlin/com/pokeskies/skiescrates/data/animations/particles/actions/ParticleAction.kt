package com.pokeskies.skiescrates.data.animations.particles.actions

import com.pokeskies.skiescrates.data.animations.particles.AnimationAction
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.server.level.ServerPlayer

class ParticleAction(
    val packets: List<ClientboundLevelParticlesPacket>,
): AnimationAction() {
    override fun tick(players: List<ServerPlayer>) {
        packets.forEach { p ->
            players.forEach { player ->
                player.connection.send(p)
            }
        }
    }
}