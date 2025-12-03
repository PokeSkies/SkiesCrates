package com.pokeskies.skiescrates.integrations.bil

import de.tomalbrc.bil.api.AnimatedEntity
import de.tomalbrc.bil.api.AnimatedEntityHolder
import de.tomalbrc.bil.core.model.Model
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3

class VirtualCrateModel(
    entityType: EntityType<out Entity>,
    model: Model,
    level: ServerLevel,
    pos: Vec3
): Entity(entityType, level), AnimatedEntity {
    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        TODO("Not yet implemented")
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        TODO("Not yet implemented")
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        TODO("Not yet implemented")
    }

    override fun getHolder(): AnimatedEntityHolder? {
        TODO("Not yet implemented")
    }



    //    private val staticElement: GenericEntityElement = StaticElement()
//    private var yaw = 0f
//    private var pitch: Float = 0f
//
//    init {
//        this.staticElement.setInteractionHandler(object : InteractionHandler {
//            override fun interact(player: ServerPlayer?, hand: InteractionHand?) {
//                println("Interacted with crate model at position: $pos")
//                player?.sendMessage(Component.literal("You interacted with the crate model!"))
//            }
//        })
//        this.addElement<GenericEntityElement>(this.staticElement)
//    }
//
//    override fun startWatching(player: ServerGamePacketListenerImpl): Boolean {
//        val value = super.startWatching(player)
//        if (value) {
//            val ids = IntArrayList()
//            for (bone in this.bones) {
//                ids.add(bone.element().entityId)
//            }
//
//            val ridePacket = VirtualEntityUtils.createRidePacket(this.staticElement.entityId, ids)
//            val list = ObjectArrayList.of<Packet<in ClientGamePacketListener?>?>(ridePacket)
//
//            val attributeInstance = AttributeInstance(Attributes.SCALE, Consumer { instance: AttributeInstance? -> })
//            attributeInstance.baseValue = 0.5
//            val attributesPacket = ClientboundUpdateAttributesPacket(
//                this.staticElement.entityId,
//                listOf<AttributeInstance?>(attributeInstance)
//            )
//            list.add(attributesPacket)
//
//            player.send(ClientboundBundlePacket(list))
//        }
//
//        return value
//    }
//
//    fun setPosition(position: Vec3?) {
//        this.staticElement.overridePos = position
//    }
//
//    fun leashedEntityId(): Int {
//        return this.staticElement.entityId
//    }
//
//    fun setYaw(yaw: Float) {
//        this.yaw = yaw
//    }
//
//    fun setPitch(pitch: Float) {
//        this.pitch = pitch
//    }
//
//    override fun updateElement(display: DisplayWrapper<*>, pose: Pose?) {
//        if (pose != null) {
//            this.applyPose(pose, display)
//        } else {
//            this.applyPose(display.defaultPose, display)
//        }
//    }
//
//    override fun createCommandSourceStack(): CommandSourceStack {
//        val name = String.format("VirtualCrateModel[%.1f, %.1f, %.1f]", this.pos.x, this.pos.y, this.pos.z)
//        return CommandSourceStack(
//            this.getLevel().server,
//            this.pos,
//            Vec2.ZERO,
//            this.getLevel(),
//            0,
//            name,
//            Component.literal(name),
//            this.getLevel().server,
//            null
//        )
//    }
//
//    private class StaticElement : GenericEntityElement() {
//        init {
//            this.dataTracker.set<Boolean?>(EntityTrackedData.SILENT, true)
//            this.dataTracker.set<Boolean?>(EntityTrackedData.NO_GRAVITY, true)
//            this.dataTracker.set<Byte?>(
//                EntityTrackedData.FLAGS,
//                ((1 shl EntityTrackedData.INVISIBLE_FLAG_INDEX)).toByte()
//            )
//        }
//
//        override fun getEntityType(): EntityType<out Entity?> {
//            return EntityType.SILVERFISH
//        }
//    }
}