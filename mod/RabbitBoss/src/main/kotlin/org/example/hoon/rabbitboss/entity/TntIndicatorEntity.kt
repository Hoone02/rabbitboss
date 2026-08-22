package org.example.hoon.rabbitboss.entity

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

class TntIndicatorEntity(entityType: EntityType<out Entity>, level: Level) : Entity(entityType, level) {

    init {
        noPhysics = true
    }

    override fun tick() {
        super.tick()
        noPhysics = true

        if (!level().isClientSide && tickCount >= durationTicks()) {
            discard()
        }
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        builder.define(DATA_DURATION_TICKS, DEFAULT_DURATION_TICKS)
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        setDurationTicks(if (compound.contains("duration_ticks")) compound.getInt("duration_ticks") else DEFAULT_DURATION_TICKS)
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        compound.putInt("duration_ticks", durationTicks())
    }

    fun setDurationTicks(durationTicks: Int) {
        entityData.set(DATA_DURATION_TICKS, durationTicks.coerceAtLeast(1))
    }

    fun durationTicks(): Int = entityData.get(DATA_DURATION_TICKS)

    override fun isPickable(): Boolean = false

    override fun shouldRender(x: Double, y: Double, z: Double): Boolean = true

    override fun shouldRenderAtSqrDistance(distance: Double): Boolean = distance <= FORCE_RENDER_DISTANCE_SQR

    override fun hurtServer(serverLevel: ServerLevel, damageSource: DamageSource, amount: Float): Boolean = false

    companion object {
        private val DATA_DURATION_TICKS: EntityDataAccessor<Int> =
            SynchedEntityData.defineId(TntIndicatorEntity::class.java, EntityDataSerializers.INT)
        private const val DEFAULT_DURATION_TICKS = 90
        private const val FORCE_RENDER_DISTANCE_SQR = 256.0 * 256.0
    }
}
