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
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.animation.AnimationController
import software.bernie.geckolib.animation.PlayState
import software.bernie.geckolib.animation.RawAnimation
import software.bernie.geckolib.util.GeckoLibUtil

class RabbitBossEntity(entityType: EntityType<out PathfinderMob>, level: Level) :
    PathfinderMob(entityType, level), GeoEntity {

    private val cache: AnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this)

    override fun registerGoals() {
        goalSelector.addGoal(0, FloatGoal(this))
        goalSelector.addGoal(1, MeleeAttackGoal(this, 1.15, true))
        goalSelector.addGoal(2, WaterAvoidingRandomStrollGoal(this, 0.9))
        goalSelector.addGoal(3, LookAtPlayerGoal(this, Player::class.java, 8.0f))
        goalSelector.addGoal(4, RandomLookAroundGoal(this))

        targetSelector.addGoal(1, HurtByTargetGoal(this))
        targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        controllers.add(
            AnimationController(this, "main", 0) { state ->
                state.setAndContinue(animationFor(currentAnimation()))
                PlayState.CONTINUE
            }.setAnimationSpeedHandler { animatable -> animatable.animationSpeed().toDouble() }
                .triggerableAnim("idle", IDLE_ANIMATION)
                .triggerableAnim("walk", WALK_ANIMATION)
                .triggerableAnim("tntskill", TNT_SKILL_ANIMATION)
                .triggerableAnim("trow", TROW_ANIMATION)
                .triggerableAnim("canonskill", CANON_SKILL_ANIMATION)
                .triggerableAnim("spawn", SPAWN_ANIMATION)
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
        builder.define(DATA_ANIMATION_SPEED, 1.0f)
    }

    fun playAnimation(animationName: String) {
        if (animationName in ANIMATION_NAMES) {
            entityData.set(DATA_ANIMATION, animationName)
        }
    }

    fun setAnimationSpeed(speed: Float) {
        entityData.set(DATA_ANIMATION_SPEED, speed.coerceAtLeast(0.0f))
    }

    private fun currentAnimation(): String = entityData.get(DATA_ANIMATION)

    private fun animationSpeed(): Float = entityData.get(DATA_ANIMATION_SPEED)

    companion object {
        private val DATA_ANIMATION: EntityDataAccessor<String> =
            SynchedEntityData.defineId(RabbitBossEntity::class.java, EntityDataSerializers.STRING)
        private val DATA_ANIMATION_SPEED: EntityDataAccessor<Float> =
            SynchedEntityData.defineId(RabbitBossEntity::class.java, EntityDataSerializers.FLOAT)
        val ANIMATION_NAMES: Set<String> = linkedSetOf("idle", "walk", "tntskill", "trow", "canonskill", "spawn")
        private val IDLE_ANIMATION: RawAnimation = RawAnimation.begin().thenLoop("idle")
        private val WALK_ANIMATION: RawAnimation = RawAnimation.begin().thenLoop("walk")
        private val TNT_SKILL_ANIMATION: RawAnimation = RawAnimation.begin().thenPlay("tntskill")
        private val TROW_ANIMATION: RawAnimation = RawAnimation.begin().thenPlay("trow")
        private val CANON_SKILL_ANIMATION: RawAnimation = RawAnimation.begin().thenPlay("canonskill")
        private val SPAWN_ANIMATION: RawAnimation = RawAnimation.begin().thenPlayAndHold("spawn")
        private const val FORCE_RENDER_DISTANCE_SQR = 256.0 * 256.0

        private fun animationFor(animationName: String): RawAnimation =
            when (animationName) {
                "walk" -> WALK_ANIMATION
                "tntskill" -> TNT_SKILL_ANIMATION
                "trow" -> TROW_ANIMATION
                "canonskill" -> CANON_SKILL_ANIMATION
                "spawn" -> SPAWN_ANIMATION
                else -> IDLE_ANIMATION
            }

        fun createAttributes(): AttributeSupplier.Builder = Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 120.0)
            .add(Attributes.MOVEMENT_SPEED, 0.28)
            .add(Attributes.ATTACK_DAMAGE, 10.0)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
    }
}
