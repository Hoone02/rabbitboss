package org.example.hoon.rabbitboss.entity

import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

class MissileIndicatorEntity(entityType: EntityType<out Entity>, level: Level) : Entity(entityType, level) {

    init {
        noPhysics = true
    }

    override fun tick() {
        super.tick()
        noPhysics = true
        if (!level().isClientSide && tickCount >= DURATION_TICKS) {
            discard()
        }
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
    }

    override fun readAdditionalSaveData(input: ValueInput) {
    }

    override fun addAdditionalSaveData(output: ValueOutput) {
    }

    override fun isPickable(): Boolean = false

    override fun shouldRender(x: Double, y: Double, z: Double): Boolean = true

    override fun shouldRenderAtSqrDistance(distance: Double): Boolean = distance <= FORCE_RENDER_DISTANCE_SQR

    override fun hurtServer(serverLevel: ServerLevel, damageSource: DamageSource, amount: Float): Boolean = false

    companion object {
        const val DURATION_TICKS = MissileEntity.DEFAULT_FLIGHT_TICKS
        private const val FORCE_RENDER_DISTANCE_SQR = 256.0 * 256.0
    }
}
