package org.example.hoon.rabbitboss.entity

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
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

class PipeEntity(entityType: EntityType<out PathfinderMob>, level: Level) :
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
            val beforeBox = previousDamageBox ?: createPipeDamageBox()
            setDeltaMovement(launchVelocity)
            noPhysics = true
            setPos(x + launchVelocity.x, y + launchVelocity.y, z + launchVelocity.z)
            refreshPipeBoundingBox()
            val afterBox = createPipeDamageBox()
            spawnTrail()
            damageOverlappingPlayers(beforeBox.minmax(afterBox))
            previousDamageBox = afterBox
            launchTicks--
            if (launchTicks <= 0) {
                discard()
                return
            }
        }

        refreshPipeBoundingBox()
    }

    fun refreshPipeBoundingBox() {
        setBoundingBox(createPipeBoundingBox())
    }

    override fun isPushable(): Boolean = false

    override fun canCollideWith(other: Entity): Boolean = false

    override fun hurtServer(serverLevel: ServerLevel, damageSource: DamageSource, amount: Float): Boolean {
        if (damageSource.`is`(DamageTypes.GENERIC_KILL)) {
            return super.hurtServer(serverLevel, damageSource, amount)
        }

        return false
    }

    private fun spawnTrail() {
        val level = level()
        if (level !is ServerLevel || tickCount % 2 != 0) {
            return
        }

        val direction = if (launchVelocity.lengthSqr() > 1.0E-6) launchVelocity.normalize() else Vec3(0.0, 0.0, 1.0)
        val trailPos = position().subtract(direction.scale(LENGTH * 0.35)).add(0.0, HEIGHT * 0.45, 0.0)
        level.sendParticles(ParticleTypes.CLOUD, trailPos.x, trailPos.y, trailPos.z, 12, 0.8, 0.35, 0.8, 0.03)
        level.sendParticles(ParticleTypes.CRIT, trailPos.x, trailPos.y, trailPos.z, 8, 0.6, 0.25, 0.6, 0.08)
    }

    private fun damageOverlappingPlayers(damageBox: AABB) {
        val level = level()
        if (level !is ServerLevel || launchTicks <= 0) {
            return
        }

        level.getEntitiesOfClass(ServerPlayer::class.java, damageBox.inflate(0.18)) { player ->
            !player.isDeadOrDying && player.uuid !in hitPlayers
        }.forEach { player ->
            if (RabbitBossCommands.applyPatternHit(player)) {
                hitPlayers.add(player.uuid)
            }
        }
    }

    private fun createPipeBoundingBox(): AABB {
        return createOrientedBox(LENGTH.toDouble(), DIAMETER.toDouble(), HEIGHT.toDouble(), 0.0, 0.0)
    }

    private fun createPipeDamageBox(): AABB {
        val direction = if (launchVelocity.lengthSqr() > 1.0E-6) launchVelocity.normalize() else Vec3(0.0, 0.0, 1.0)
        return createOrientedBox(
            (LENGTH - DAMAGE_LENGTH_INSET).toDouble(),
            (DIAMETER - DAMAGE_DIAMETER_INSET).toDouble(),
            (HEIGHT - DAMAGE_HEIGHT_INSET).toDouble(),
            0.25,
            0.0
        ).move(direction.scale(-DAMAGE_FRONT_BACK_SHIFT))
    }

    private fun createOrientedBox(length: Double, diameter: Double, height: Double, yOffset: Double, forwardShift: Double): AABB {
        val radians = Math.toRadians(yRot.toDouble())
        val cos = kotlin.math.abs(kotlin.math.cos(radians))
        val sin = kotlin.math.abs(kotlin.math.sin(radians))
        val direction = Vec3(-kotlin.math.sin(radians), 0.0, kotlin.math.cos(radians))
        val center = position().add(direction.scale(forwardShift))
        val halfX = (cos * length + sin * diameter) / 2.0
        val halfZ = (sin * length + cos * diameter) / 2.0

        return AABB(
            center.x - halfX,
            y + yOffset,
            center.z - halfZ,
            center.x + halfX,
            y + yOffset + height,
            center.z + halfZ
        )
    }

    override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        controllers.add(
            AnimationController<PipeEntity>("main", 0) { state ->
                state.setAndContinue(RUN_ANIMATION)
                PlayState.CONTINUE
            }
        )
    }

    override fun getAnimatableInstanceCache(): AnimatableInstanceCache = cache

    override fun shouldRender(x: Double, y: Double, z: Double): Boolean = true

    override fun shouldRenderAtSqrDistance(distance: Double): Boolean = distance <= FORCE_RENDER_DISTANCE_SQR

    fun launch(direction: Vec3, speed: Double = 1.2, ticks: Int = 200) {
        launchVelocity = direction.normalize().scale(speed)
        launchTicks = ticks
        isNoGravity = true
        noPhysics = true
        setNoAi(true)
        previousDamageBox = createPipeDamageBox()
    }

    companion object {
        const val LENGTH: Float = 16.25f
        const val DIAMETER: Float = 5.0f
        const val HEIGHT: Float = 5.0f
        private const val DAMAGE_LENGTH_INSET = 1.0f
        private const val DAMAGE_DIAMETER_INSET = 0.35f
        private const val DAMAGE_HEIGHT_INSET = 0.25f
        private const val DAMAGE_FRONT_BACK_SHIFT = 0.25
        private const val FORCE_RENDER_DISTANCE_SQR = 256.0 * 256.0
        private val RUN_ANIMATION: RawAnimation = RawAnimation.begin().thenPlayAndHold("run")

        fun createAttributes(): AttributeSupplier.Builder = Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0)
    }
}
