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
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.example.hoon.rabbitboss.command.RabbitBossCommands
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.animation.AnimationController
import software.bernie.geckolib.animation.PlayState
import software.bernie.geckolib.animation.RawAnimation
import software.bernie.geckolib.util.GeckoLibUtil
import java.util.UUID

class RazerEntity(entityType: EntityType<out PathfinderMob>, level: Level) :
    PathfinderMob(entityType, level), GeoEntity {

    private val cache: AnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this)
    private var launchVelocity: Vec3 = Vec3.ZERO
    private var launchTicks = 0
    private var launchDelayTicks = 0
    private var previousDamageBox: AABB? = null
    private val hitPlayers = mutableSetOf<UUID>()

    override fun registerGoals() {
    }

    override fun tick() {
        super.tick()

        if (!level().isClientSide && launchDelayTicks > 0) {
            noPhysics = true
            setDeltaMovement(Vec3.ZERO)
            refreshRazerBoundingBox()
            spawnChargeGlow()
            damageOverlappingPlayers()
            launchDelayTicks--
        } else if (!level().isClientSide && launchTicks > 0) {
            val beforeBox = previousDamageBox ?: createRazerDamageBox()
            setDeltaMovement(launchVelocity)
            noPhysics = true
            setPos(x + launchVelocity.x, y + launchVelocity.y, z + launchVelocity.z)
            refreshRazerBoundingBox()
            val afterBox = createRazerDamageBox()
            spawnTrail()
            damageOverlappingPlayers(beforeBox.minmax(afterBox))
            previousDamageBox = afterBox
            hasImpulse = true
            launchTicks--
            if (launchTicks <= 0) {
                discard()
            }
        }

        refreshRazerBoundingBox()
    }

    fun refreshRazerBoundingBox() {
        setBoundingBox(createRazerBoundingBox())
    }

    override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        controllers.add(
            AnimationController(this, "main", 0) { state ->
                state.setAndContinue(SPAWN_ANIMATION)
                PlayState.CONTINUE
            }
        )
    }

    override fun getAnimatableInstanceCache(): AnimatableInstanceCache = cache

    override fun shouldRender(x: Double, y: Double, z: Double): Boolean = true

    override fun shouldRenderAtSqrDistance(distance: Double): Boolean = distance <= FORCE_RENDER_DISTANCE_SQR

    override fun isPushable(): Boolean = false

    override fun canCollideWith(other: Entity): Boolean = false

    override fun canBeCollidedWith(): Boolean = false

    override fun push(entity: Entity) {
    }

    override fun push(vector: Vec3) {
    }

    override fun push(x: Double, y: Double, z: Double) {
    }

    override fun pushEntities() {
    }

    override fun doPush(entity: Entity) {
    }

    override fun hurtServer(serverLevel: ServerLevel, damageSource: DamageSource, amount: Float): Boolean {
        if (damageSource.`is`(DamageTypes.GENERIC_KILL)) {
            return super.hurtServer(serverLevel, damageSource, amount)
        }

        return false
    }

    fun launch(direction: Vec3, speed: Double = 2.0, ticks: Int = 90, delayTicks: Int = START_HOLD_TICKS) {
        launchVelocity = direction.normalize().scale(speed)
        launchTicks = ticks
        launchDelayTicks = delayTicks
        isNoGravity = true
        noPhysics = true
        setNoAi(true)
        refreshRazerBoundingBox()
        previousDamageBox = createRazerDamageBox()
    }

    private fun createRazerBoundingBox(): AABB {
        val radians = Math.toRadians(yRot.toDouble())
        val cos = kotlin.math.abs(kotlin.math.cos(radians))
        val sin = kotlin.math.abs(kotlin.math.sin(radians))
        val halfX = (cos * LENGTH + sin * THICKNESS) / 2.0
        val halfZ = (sin * LENGTH + cos * THICKNESS) / 2.0

        return AABB(
            x - halfX,
            y + HITBOX_Y_OFFSET,
            z - halfZ,
            x + halfX,
            y + HITBOX_Y_OFFSET + HEIGHT,
            z + halfZ
        )
    }

    private fun spawnChargeGlow() {
        val level = level()
        if (level !is ServerLevel) {
            return
        }

        val center = position().add(0.0, HITBOX_Y_OFFSET + HEIGHT * 0.5, 0.0)
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 18, 1.1, 0.18, 1.1, 0.035)
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 22, 1.3, 0.2, 1.3, 0.08)
    }

    private fun spawnTrail() {
        val level = level()
        if (level !is ServerLevel || tickCount % 2 != 0) {
            return
        }

        val direction = if (launchVelocity.lengthSqr() > 1.0E-6) launchVelocity.normalize() else Vec3(0.0, 0.0, 1.0)
        val trailPos = position().subtract(direction.scale(2.0)).add(0.0, 0.2, 0.0)
        level.sendParticles(ParticleTypes.END_ROD, trailPos.x, trailPos.y, trailPos.z, 8, 0.25, 0.1, 0.25, 0.02)
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, trailPos.x, trailPos.y, trailPos.z, 10, 0.35, 0.12, 0.35, 0.04)
    }

    private fun damageOverlappingPlayers(damageBox: AABB = createRazerDamageBox()) {
        val level = level()
        if (level !is ServerLevel || (launchDelayTicks <= 0 && launchTicks <= 0)) {
            return
        }

        level.getEntitiesOfClass(ServerPlayer::class.java, damageBox.inflate(0.12)) { player ->
            !player.isDeadOrDying && player.uuid !in hitPlayers
        }.forEach { player ->
            if (RabbitBossCommands.applyPatternHit(player)) {
                hitPlayers.add(player.uuid)
            }
        }
    }

    private fun createRazerDamageBox(): AABB {
        val box = createOrientedBox(
            (LENGTH - DAMAGE_LENGTH_INSET).toDouble(),
            (THICKNESS - DAMAGE_THICKNESS_INSET).toDouble(),
            (HEIGHT - DAMAGE_HEIGHT_INSET).toDouble(),
            HITBOX_Y_OFFSET + DAMAGE_Y_INSET,
            0.0
        )
        val shift = if (launchVelocity.lengthSqr() > 1.0E-6) {
            launchVelocity.normalize().scale(-DAMAGE_FRONT_SHIFT)
        } else {
            Vec3.ZERO
        }
        return box.move(shift)
    }

    private fun createOrientedBox(length: Double, thickness: Double, height: Double, yOffset: Double, forwardShift: Double): AABB {
        val radians = Math.toRadians(yRot.toDouble())
        val cos = kotlin.math.abs(kotlin.math.cos(radians))
        val sin = kotlin.math.abs(kotlin.math.sin(radians))
        val direction = Vec3(-kotlin.math.sin(radians), 0.0, kotlin.math.cos(radians))
        val center = position().add(direction.scale(forwardShift))
        val halfX = (cos * length + sin * thickness) / 2.0
        val halfZ = (sin * length + cos * thickness) / 2.0

        return AABB(
            center.x - halfX,
            y + yOffset,
            center.z - halfZ,
            center.x + halfX,
            y + yOffset + height,
            center.z + halfZ
        )
    }

    companion object {
        const val LENGTH: Float = 25.0f
        const val THICKNESS: Float = 0.94f
        const val HEIGHT: Float = 0.94f
        private const val HITBOX_Y_OFFSET = 0.25
        private const val START_HOLD_TICKS = 12
        private const val DAMAGE_LENGTH_INSET = 0.75f
        private const val DAMAGE_THICKNESS_INSET = 0.12f
        private const val DAMAGE_HEIGHT_INSET = 0.12f
        private const val DAMAGE_Y_INSET = 0.06
        private const val DAMAGE_FRONT_SHIFT = 0.15
        private const val FORCE_RENDER_DISTANCE_SQR = 256.0 * 256.0
        private val SPAWN_ANIMATION: RawAnimation = RawAnimation.begin().thenPlayAndHold("spawn")

        fun createAttributes(): AttributeSupplier.Builder = Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0)
    }
}
