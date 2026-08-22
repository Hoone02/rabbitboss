package org.example.hoon.rabbitboss.entity

import net.minecraft.server.level.ServerLevel
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.level.Level
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.animation.AnimationController
import software.bernie.geckolib.animation.PlayState
import software.bernie.geckolib.animation.RawAnimation
import software.bernie.geckolib.util.GeckoLibUtil

class TntEntity(entityType: EntityType<out PathfinderMob>, level: Level) :
    PathfinderMob(entityType, level), GeoEntity {

    private val cache: AnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this)

    override fun registerGoals() {
    }

    override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        controllers.add(
            AnimationController(this, "main", 0) { state ->
                state.setAndContinue(SPAWN_ANIMATION)
                PlayState.CONTINUE
            }.setAnimationSpeedHandler { animatable -> animatable.animationSpeed().toDouble() }
        )
    }

    override fun getAnimatableInstanceCache(): AnimatableInstanceCache = cache

    override fun shouldRender(x: Double, y: Double, z: Double): Boolean = true

    override fun shouldRenderAtSqrDistance(distance: Double): Boolean = distance <= FORCE_RENDER_DISTANCE_SQR

    override fun hurtServer(serverLevel: ServerLevel, damageSource: DamageSource, amount: Float): Boolean {
        if (damageSource.`is`(DamageTypes.GENERIC_KILL)) {
            return super.hurtServer(serverLevel, damageSource, amount)
        }

        return false
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(DATA_ANIMATION_SPEED, 1.0f)
    }

    fun setAnimationSpeed(speed: Float) {
        entityData.set(DATA_ANIMATION_SPEED, speed.coerceAtLeast(0.0f))
    }

    private fun animationSpeed(): Float = entityData.get(DATA_ANIMATION_SPEED)

    companion object {
        private val DATA_ANIMATION_SPEED: EntityDataAccessor<Float> =
            SynchedEntityData.defineId(TntEntity::class.java, EntityDataSerializers.FLOAT)
        private const val FORCE_RENDER_DISTANCE_SQR = 256.0 * 256.0
        private val SPAWN_ANIMATION: RawAnimation = RawAnimation.begin().thenPlayAndHold("spawn")

        fun createAttributes(): AttributeSupplier.Builder = Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0)
    }
}
