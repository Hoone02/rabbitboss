package org.example.hoon.rabbitboss.entity

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import com.geckolib.animatable.GeoEntity
import com.geckolib.animatable.instance.AnimatableInstanceCache
import com.geckolib.animatable.manager.AnimatableManager
import com.geckolib.animation.AnimationController
import com.geckolib.animation.`object`.PlayState
import com.geckolib.animation.RawAnimation
import com.geckolib.util.GeckoLibUtil

class MissileEntity(entityType: EntityType<out PathfinderMob>, level: Level) :
    PathfinderMob(entityType, level), GeoEntity {

    private val cache: AnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this)
    private var startPos: Vec3 = Vec3.ZERO
    private var targetPos: Vec3 = Vec3.ZERO
    private var flightTicks = 0
    private var ageTicks = 0

    override fun registerGoals() {
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(DATA_RENDER_YAW, 0.0f)
        builder.define(DATA_RENDER_PITCH, 0.0f)
    }

    override fun tick() {
        super.tick()
        if (level().isClientSide || flightTicks <= 0) {
            return
        }

        ageTicks++
        val progress = (ageTicks.toDouble() / flightTicks).coerceIn(0.0, 1.0)
        val next = missilePosition(progress)
        val previousProgress = ((ageTicks - 1).toDouble() / flightTicks).coerceIn(0.0, 1.0)
        val previous = missilePosition(previousProgress)
        val velocity = next.subtract(previous)

        setPos(next.x, next.y, next.z)
        faceVelocity(velocity)
        spawnTrail(velocity)

        if (ageTicks >= flightTicks) {
            explode()
            discard()
        }
    }

    override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        controllers.add(
            AnimationController<MissileEntity>("main", 0) { state ->
                state.setAndContinue(IDLE_ANIMATION)
                PlayState.CONTINUE
            }
        )
    }

    override fun getAnimatableInstanceCache(): AnimatableInstanceCache = cache

    override fun isPushable(): Boolean = false

    override fun canCollideWith(other: Entity): Boolean = false

    override fun shouldRender(x: Double, y: Double, z: Double): Boolean = true

    override fun shouldRenderAtSqrDistance(distance: Double): Boolean = distance <= FORCE_RENDER_DISTANCE_SQR

    override fun hurtServer(serverLevel: ServerLevel, damageSource: DamageSource, amount: Float): Boolean {
        if (damageSource.`is`(DamageTypes.GENERIC_KILL)) {
            return super.hurtServer(serverLevel, damageSource, amount)
        }

        return false
    }

    fun launch(start: Vec3, target: Vec3, ticks: Int = DEFAULT_FLIGHT_TICKS) {
        startPos = start
        targetPos = target
        flightTicks = ticks
        ageTicks = 0
        isNoGravity = true
        noPhysics = true
        setNoAi(true)
        setPos(start.x, start.y, start.z)
        faceVelocity(target.subtract(start))
    }

    private fun missilePosition(progress: Double): Vec3 {
        val base = startPos.lerp(targetPos, progress)
        val arc = kotlin.math.sin(progress * Math.PI) * ARC_HEIGHT
        return base.add(0.0, arc, 0.0)
    }

    private fun faceVelocity(velocity: Vec3) {
        if (velocity.lengthSqr() < 1.0E-6) {
            return
        }

        val horizontal = kotlin.math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z)
        val yaw = Math.toDegrees(kotlin.math.atan2(-velocity.x, velocity.z)).toFloat()
        val pitch = Math.toDegrees(kotlin.math.atan2(-velocity.y, horizontal)).toFloat()
        snapTo(x, y, z, yaw, pitch)
        yHeadRot = yaw
        yBodyRot = yaw
        entityData.set(DATA_RENDER_YAW, yaw)
        entityData.set(DATA_RENDER_PITCH, pitch)
    }

    fun renderYaw(): Float = entityData.get(DATA_RENDER_YAW)

    fun renderPitch(): Float = entityData.get(DATA_RENDER_PITCH)

    private fun spawnTrail(velocity: Vec3) {
        val level = level()
        if (level !is ServerLevel) {
            return
        }

        val direction = if (velocity.lengthSqr() > 1.0E-6) velocity.normalize() else Vec3(0.0, 0.0, 1.0)
        val firePos = position().subtract(direction.scale(1.8)).add(0.0, 0.35, 0.0)
        level.sendParticles(ParticleTypes.FLAME, firePos.x, firePos.y, firePos.z, 16, 0.16, 0.16, 0.16, 0.045)
        level.sendParticles(ParticleTypes.SMOKE, firePos.x, firePos.y, firePos.z, 18, 0.24, 0.24, 0.24, 0.055)
        level.sendParticles(ParticleTypes.LARGE_SMOKE, firePos.x, firePos.y, firePos.z, 4, 0.16, 0.16, 0.16, 0.035)
    }

    private fun explode() {
        val level = level()
        if (level !is ServerLevel) {
            return
        }

        level.playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 4.5f, 0.75f)
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, targetPos.x, targetPos.y + 0.4, targetPos.z, 2, 0.4, 0.3, 0.4, 0.0)
        level.sendParticles(ParticleTypes.EXPLOSION, targetPos.x, targetPos.y + 0.5, targetPos.z, 32, 2.2, 0.55, 2.2, 0.03)
        level.sendParticles(ParticleTypes.FLAME, targetPos.x, targetPos.y + 0.3, targetPos.z, 150, 2.3, 0.35, 2.3, 0.06)
        level.sendParticles(ParticleTypes.LARGE_SMOKE, targetPos.x, targetPos.y + 0.7, targetPos.z, 90, 2.0, 0.8, 2.0, 0.04)

        level.players().forEach { player ->
            val dx = kotlin.math.abs(player.x - targetPos.x)
            val dz = kotlin.math.abs(player.z - targetPos.z)
            val dy = player.y - targetPos.y
            if (dx <= 2.5 && dz <= 2.5 && dy >= -1.0 && dy <= 4.0) {
                player.hurt(level.damageSources().explosion(null, null), MISSILE_DAMAGE)
            }
        }
    }

    companion object {
        const val DEFAULT_FLIGHT_TICKS = 45
        private const val ARC_HEIGHT = 12.0
        private const val MISSILE_DAMAGE = 0.1f
        private const val FORCE_RENDER_DISTANCE_SQR = 256.0 * 256.0
        private val DATA_RENDER_YAW: EntityDataAccessor<Float> =
            SynchedEntityData.defineId(MissileEntity::class.java, EntityDataSerializers.FLOAT)
        private val DATA_RENDER_PITCH: EntityDataAccessor<Float> =
            SynchedEntityData.defineId(MissileEntity::class.java, EntityDataSerializers.FLOAT)
        private val IDLE_ANIMATION: RawAnimation = RawAnimation.begin().thenLoop("idle")

        fun createAttributes(): AttributeSupplier.Builder = Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0)
    }
}
