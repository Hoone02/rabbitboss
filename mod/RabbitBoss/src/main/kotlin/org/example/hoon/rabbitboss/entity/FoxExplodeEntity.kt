package org.example.hoon.rabbitboss.entity

import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
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

class FoxExplodeEntity(entityType: EntityType<out PathfinderMob>, level: Level) :
    PathfinderMob(entityType, level), GeoEntity {

    private val cache: AnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this)

    override fun registerGoals() {
    }

    override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        controllers.add(
            AnimationController(this, "main", 0) { state ->
                state.setAndContinue(animationFor(currentAnimation()))
                PlayState.CONTINUE
            }
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
        builder.define(DATA_ANIMATION, "idle")
    }

    fun playAnimation(animationName: String) {
        if (animationName in ANIMATION_NAMES) {
            entityData.set(DATA_ANIMATION, animationName)
        }
    }

    private fun currentAnimation(): String = entityData.get(DATA_ANIMATION)

    companion object {
        private val DATA_ANIMATION: EntityDataAccessor<String> =
            SynchedEntityData.defineId(FoxExplodeEntity::class.java, EntityDataSerializers.STRING)
        private val IDLE_ANIMATION: RawAnimation = RawAnimation.begin().thenLoop("idle")
        private val EXPLODE_ANIMATION: RawAnimation = RawAnimation.begin().thenPlayAndHold("explode")
        private val ANIMATION_NAMES: Set<String> = setOf("idle", "explode")
        private const val FORCE_RENDER_DISTANCE_SQR = 256.0 * 256.0

        private fun animationFor(animationName: String): RawAnimation =
            when (animationName) {
                "explode" -> EXPLODE_ANIMATION
                else -> IDLE_ANIMATION
            }

        fun createAttributes(): AttributeSupplier.Builder = Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0)
    }
}
