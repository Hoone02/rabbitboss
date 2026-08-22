package org.example.hoon.rabbitboss.entity

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
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
import org.example.hoon.rabbitboss.command.RabbitBossCommands
import com.geckolib.animatable.GeoEntity
import com.geckolib.animatable.instance.AnimatableInstanceCache
import com.geckolib.animatable.manager.AnimatableManager
import com.geckolib.animation.AnimationController
import com.geckolib.animation.`object`.PlayState
import com.geckolib.animation.RawAnimation
import com.geckolib.util.GeckoLibUtil
import java.util.UUID
import net.minecraft.world.phys.AABB

class BoomEntity(entityType: EntityType<out PathfinderMob>, level: Level) :
    PathfinderMob(entityType, level), GeoEntity {

    private val cache: AnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this)
    private var launchVelocity: Vec3 = Vec3.ZERO
    private var launchTicks = 0
    private var previousDamageBox: AABB? = null
    private val hitPlayers = mutableSetOf<UUID>()

    override fun registerGoals() {
    }

    override fun tick() {
        super.tick()

        if (!level().isClientSide && launchTicks > 0) {
            val beforeBox = previousDamageBox ?: contactDamageBox()
            setDeltaMovement(launchVelocity)
            noPhysics = true
            setPos(x + launchVelocity.x, y + launchVelocity.y, z + launchVelocity.z)
            spawnTrail()
            val afterBox = contactDamageBox()
            damageOverlappingPlayers(beforeBox.minmax(afterBox))
            previousDamageBox = afterBox
            launchTicks--
            if (launchTicks <= 0) {
                discard()
            }
        }
    }

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

    fun launch(direction: Vec3, speed: Double = 3.0, ticks: Int = 120) {
        launchVelocity = direction.normalize().scale(speed)
        launchTicks = ticks
        isNoGravity = true
        noPhysics = true
        setNoAi(true)
        previousDamageBox = contactDamageBox()
    }

    private fun spawnTrail() {
        val level = level()
        if (level !is ServerLevel) {
            return
        }

        val direction = if (launchVelocity.lengthSqr() > 1.0E-6) launchVelocity.normalize() else Vec3(0.0, 0.0, 1.0)
        val trailPos = position().subtract(direction.scale(1.8))
        level.sendParticles(ParticleTypes.FLAME, trailPos.x, trailPos.y, trailPos.z, 10, 0.25, 0.25, 0.25, 0.04)
        level.sendParticles(ParticleTypes.LARGE_SMOKE, trailPos.x, trailPos.y, trailPos.z, 14, 0.35, 0.35, 0.35, 0.05)
    }

    private fun damageOverlappingPlayers(damageBox: AABB) {
        val level = level()
        if (level !is ServerLevel || launchTicks <= 0) {
            return
        }

        level.getEntitiesOfClass(ServerPlayer::class.java, damageBox.inflate(0.25)) { player ->
            !player.isDeadOrDying && player.uuid !in hitPlayers
        }.forEach { player ->
            if (RabbitBossCommands.applyPatternHit(player)) {
                hitPlayers.add(player.uuid)
            }
        }
    }

    private fun contactDamageBox() =
        boundingBox.inflate(0.05, 0.05, 0.05).move(
            if (launchVelocity.lengthSqr() > 1.0E-6) launchVelocity.normalize().scale(-0.1) else Vec3.ZERO
        )

    override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        controllers.add(
            AnimationController<BoomEntity>("main", 0) { state ->
                state.setAndContinue(SPAWN_ANIMATION)
                PlayState.CONTINUE
            }
        )
    }

    override fun getAnimatableInstanceCache(): AnimatableInstanceCache = cache

    companion object {
        private const val FORCE_RENDER_DISTANCE_SQR = 256.0 * 256.0
        private val SPAWN_ANIMATION: RawAnimation = RawAnimation.begin().thenLoop("spawn")

        fun createAttributes(): AttributeSupplier.Builder = Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0)
    }
}
