package org.example.hoon.rabbitboss.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import net.minecraft.core.registries.Registries
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
import net.minecraft.network.chat.Component
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.example.hoon.rabbitboss.entity.BoomEntity
import org.example.hoon.rabbitboss.entity.CanonEntity
import org.example.hoon.rabbitboss.entity.FoxExplodeEntity
import org.example.hoon.rabbitboss.entity.GlobalTwoIndicatorEntity
import org.example.hoon.rabbitboss.entity.MissileEntity
import org.example.hoon.rabbitboss.entity.MissileIndicatorEntity
import org.example.hoon.rabbitboss.entity.MissileLauncherEntity
import org.example.hoon.rabbitboss.entity.ModEntities
import org.example.hoon.rabbitboss.entity.PipeEntity
import org.example.hoon.rabbitboss.entity.RabbitBossEntity
import org.example.hoon.rabbitboss.entity.RazerEntity
import org.example.hoon.rabbitboss.entity.TntEntity
import org.example.hoon.rabbitboss.entity.TntIndicatorEntity
import org.example.hoon.rabbitboss.network.LifeHudPayload
import java.nio.file.Files
import java.util.Properties
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

object RabbitBossCommands {
    private const val CANON_COUNT = 5
    private const val CANON_SKILL_DELAY_TICKS = 40
    private const val CANON_SEQUENCE_INTERVAL_TICKS = 10
    private const val CANON_SKILL_ANIMATION_TICKS = 58
    private const val CANON_FIRE_AFTER_ALL_SPAWNED_TICKS = 60
    private const val CANON_FIRE_INTERVAL_TICKS = 16
    private const val CANON_FIRE_ROUNDS = 3
    private const val CANON_DISCARD_AFTER_FIRE_TICKS = 40
    private const val CANON_MODEL_SCALE = 5.0 / 16.0
    private const val CANON_EXPLODE_ANIMATED_Y = 5.975
    private const val CANON_EXPLODE_ANIMATED_FORWARD = 20.025
    private const val CANON_EXPLODE_Y_OFFSET = -2.5
    private const val TNT_AREA_COUNT = 5
    private const val TNT_SKILL_ANIMATION_TICKS = 228
    private const val TNT_SPAWN_DELAY_TICKS = 140
    private const val TNT_SPAWN_INTERVAL_TICKS = 30
    private const val TNT_SPAWN_RETRY_TICKS = 5
    private const val TNT_EXPLOSION_DELAY_TICKS = 90
    private const val TNT_AREA_FORWARD_RANGE = 11
    private const val TNT_AREA_SIDE_RANGE = 2
    private const val TNT_AREA_DAMAGE = 0.1f
    private const val PLAYER_LIVES = 5
    private const val PLAYER_HIT_DAMAGE = 0.1f
    private const val MISSILE_LAUNCHER_SIDE_OFFSET = 8.0
    private const val MISSILE_LAUNCHER_FORWARD_OFFSET = 6.0
    private const val MISSILE_LAUNCHER_FIRE_DELAY_TICKS = 50
    private const val MISSILE_FIRE_INTERVAL_TICKS = 10
    private const val MISSILES_PER_LAUNCHER = 25
    private const val MISSILE_AREA_FORWARD_RANGE = 11
    private const val MISSILE_AREA_SIDE_RANGE = 12
    private const val RAZER_COUNT = 10
    private const val RAZER_FIRE_INTERVAL_TICKS = 20
    private const val RAZER_FORWARD_OFFSET = 5.0
    private const val RAZER_HIGH_OFFSET = 1.6
    private const val AUTO_PATTERN_LEAD_TICKS = 20
    private const val START_FOX_TO_BOSS_TICKS = 100
    private const val START_BOSS_TO_FOX_EXPLODE_TICKS = 10
    private const val START_FOX_REMOVE_AFTER_EXPLODE_TICKS = 80
    private const val START_PATTERN_AFTER_FOX_REMOVE_TICKS = 100
    private const val SKILL_ONE_PATTERN_TICKS = 240
    private const val SKILL_TWO_PATTERN_TICKS = CANON_SKILL_DELAY_TICKS +
        (CANON_COUNT - 1) * CANON_SEQUENCE_INTERVAL_TICKS +
        CANON_FIRE_AFTER_ALL_SPAWNED_TICKS +
        (CANON_FIRE_ROUNDS * CANON_COUNT - 1) * CANON_FIRE_INTERVAL_TICKS + 1
    private const val SKILL_FOUR_PATTERN_TICKS = 350
    private const val GLOBAL_ONE_PATTERN_TICKS = (RAZER_COUNT - 1) * RAZER_FIRE_INTERVAL_TICKS + 90
    private const val GLOBAL_TWO_BASE_FREEZE_START_TICKS = 120
    private const val GLOBAL_TWO_FREEZE_DURATION_TICKS = 100
    private const val GLOBAL_TWO_MOVE_THRESHOLD_SQR = 0.0009
    private const val NORMAL_PATTERNS_BEFORE_GLOBAL = 5
    private const val POWER_SPEED_CAP = 12
    private val spawnFile = FabricLoader.getInstance().configDir.resolve("rabbitboss-spawn.properties")
    private val canonFile = FabricLoader.getInstance().configDir.resolve("rabbitboss-canons.properties")
    private val tntAreaFile = FabricLoader.getInstance().configDir.resolve("rabbitboss-tntareas.properties")
    private val areaFile = FabricLoader.getInstance().configDir.resolve("rabbitboss-area.properties")
    private val legacyMissileAreaFile = FabricLoader.getInstance().configDir.resolve("rabbitboss-missilearea.properties")
    private val controlFile = FabricLoader.getInstance().configDir.resolve("rabbitboss-control.properties")
    private var activeBossUuid: UUID? = null
    private var activeBossEntity: RabbitBossEntity? = null
    private var activeBossSpawnPosition: Vec3? = null
    private var activeBossSpawnYaw: Float = 0.0f
    private val scheduledTasks = mutableListOf<ScheduledTask>()
    private val skillTasks = mutableListOf<SkillTask>()
    private val activeTntAreas = mutableSetOf<TntAreaPosition>()
    private val playerLives = mutableMapOf<UUID, Int>()
    private var lifeHudTicks = 0
    private var autoPatternTask: AutoPatternTask? = null
    private var startSequenceRunning = false
    private var globalPowerLevel = 0
    private var externalStartRequested = false
    private var externalStopRequested = false
    private val animationSuggestions = SuggestionProvider<CommandSourceStack> { _, builder ->
        RabbitBossEntity.ANIMATION_NAMES.forEach(builder::suggest)
        builder.buildFuture()
    }

    private fun powerLevel(): Int = globalPowerLevel.coerceAtLeast(0)

    private fun speedPowerLevel(): Int = powerLevel().coerceAtMost(POWER_SPEED_CAP)

    private fun increasePowerAfterGlobal() {
        globalPowerLevel++
    }

    private fun pipeLaunchSpeed(power: Int = speedPowerLevel()): Double = 1.2 * (1.0 + power * 0.5)

    private fun boomLaunchSpeed(power: Int = speedPowerLevel()): Double = 3.0 + power * 0.35

    private fun razerLaunchSpeed(power: Int = speedPowerLevel()): Double = 2.0 + power * 0.3

    private fun missileFlightTicks(power: Int = speedPowerLevel()): Int = (MissileEntity.DEFAULT_FLIGHT_TICKS - power * 3).coerceAtLeast(20)

    private fun missilesPerLauncher(power: Int = powerLevel()): Int = MISSILES_PER_LAUNCHER + power

    private fun cannonShotsPerVolley(power: Int = powerLevel()): Int = (1 + power).coerceIn(1, 3)

    private fun tntSpeedMultiplier(power: Int = speedPowerLevel()): Double = 1.2.pow(power)

    private fun tntSkillAnimationSpeed(power: Int = speedPowerLevel()): Float = tntSpeedMultiplier(power).toFloat().coerceAtMost(3.0f)

    private fun tntSkillAnimationTicks(power: Int = speedPowerLevel()): Int =
        ceil(TNT_SKILL_ANIMATION_TICKS / tntSkillAnimationSpeed(power).toDouble()).roundToInt().coerceAtLeast(20)

    private fun tntExplosionDelayTicks(power: Int = speedPowerLevel()): Int =
        ceil(TNT_EXPLOSION_DELAY_TICKS / tntSpeedMultiplier(power)).roundToInt().coerceAtLeast(20)

    private fun tntSpawnDelayTicks(power: Int = speedPowerLevel()): Int =
        ceil(TNT_SPAWN_DELAY_TICKS / tntSpeedMultiplier(power)).roundToInt().coerceAtLeast(40)

    private fun tntSpawnIntervalTicks(power: Int = speedPowerLevel()): Int =
        ceil(TNT_SPAWN_INTERVAL_TICKS / tntSpeedMultiplier(power)).roundToInt().coerceAtLeast(10)

    private fun tntSpawnBatchSize(power: Int = powerLevel()): Int = if (power >= 3) 2 else 1

    private fun globalTwoSpeedMultiplier(power: Int = speedPowerLevel()): Double = 1.0 + power * 0.12

    private fun globalTwoDelayTicks(baseTicks: Int, power: Int = speedPowerLevel()): Int =
        (baseTicks / globalTwoSpeedMultiplier(power)).roundToInt().coerceAtLeast(0)

    private fun globalTwoFreezeStartTicks(power: Int = speedPowerLevel()): Int =
        globalTwoDelayTicks(GLOBAL_TWO_BASE_FREEZE_START_TICKS, power).coerceAtLeast(40)

    private fun skillTwoPatternTicks(power: Int = powerLevel()): Int {
        val groupsPerRound = ceil(CANON_COUNT / cannonShotsPerVolley(power).toDouble()).roundToInt()
        return CANON_SKILL_DELAY_TICKS +
            (CANON_COUNT - 1) * CANON_SEQUENCE_INTERVAL_TICKS +
            CANON_FIRE_AFTER_ALL_SPAWNED_TICKS +
            (CANON_FIRE_ROUNDS * groupsPerRound - 1) * CANON_FIRE_INTERVAL_TICKS + 1
    }

    private fun skillThreePatternTicks(power: Int = powerLevel()): Int {
        val waves = ceil((TNT_AREA_COUNT * 2) / tntSpawnBatchSize(power).toDouble()).roundToInt()
        return tntSpawnDelayTicks(power) + (waves - 1) * tntSpawnIntervalTicks(power) + 1
    }

    private fun skillFourPatternTicks(power: Int = powerLevel()): Int =
        MISSILE_LAUNCHER_FIRE_DELAY_TICKS + missilesPerLauncher(power) * MISSILE_FIRE_INTERVAL_TICKS + missileFlightTicks(power) + 40

    private fun globalTwoPatternTicks(power: Int = speedPowerLevel()): Int =
        globalTwoFreezeStartTicks(power) + GLOBAL_TWO_FREEZE_DURATION_TICKS + 1

    fun register() {
        ServerTickEvents.END_SERVER_TICK.register { server ->
            tickExternalRequests(server)
            tickAutoPatternTask()
            tickSkillTasks()
            tickScheduledTasks()
            tickLifeHud()
        }
        ServerLivingEntityEvents.ALLOW_DAMAGE.register { entity, _, amount ->
            handlePlayerDamage(entity, amount)
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                Commands.literal("rabbitbossmod")
                    .requires { source -> source.hasPermission(2) }
                    .then(
                        Commands.literal("setspawn")
                            .executes { context -> setSpawn(context.source) }
                    )
                    .then(
                        Commands.literal("start")
                            .executes { context -> start(context.source) }
                    )
                    .then(
                        Commands.literal("stop")
                            .executes { context -> stop(context.source) }
                    )
                    .then(
                        Commands.literal("animation")
                            .then(
                                Commands.argument("name", StringArgumentType.word())
                                    .suggests(animationSuggestions)
                                    .executes { context ->
                                        playAnimation(
                                            context.source,
                                            StringArgumentType.getString(context, "name")
                                        )
                                    }
                            )
                    )
                    .then(
                        Commands.literal("summon")
                            .executes { context -> summon(context.source, context.source.position) }
                            .then(
                                Commands.argument("pos", Vec3Argument.vec3())
                                    .executes { context ->
                                        summon(context.source, Vec3Argument.getVec3(context, "pos"))
                                    }
                            )
                    )
                    .then(
                        Commands.literal("pipe")
                            .executes { context -> summonPipe(context.source, context.source.position) }
                            .then(
                                Commands.argument("pos", Vec3Argument.vec3())
                                    .executes { context ->
                                        summonPipe(context.source, Vec3Argument.getVec3(context, "pos"))
                                    }
                            )
                    )
                    .then(
                        Commands.literal("setting")
                            .then(
                                Commands.literal("canon")
                                    .then(
                                        Commands.literal("setpos")
                                            .then(
                                                Commands.argument("number", IntegerArgumentType.integer(1, CANON_COUNT))
                                                    .executes { context ->
                                                        setCanonPosition(
                                                            context.source,
                                                            IntegerArgumentType.getInteger(context, "number")
                                                        )
                                                    }
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("tntarea")
                                    .then(
                                        Commands.literal("setpos")
                                            .then(
                                                Commands.argument("number", IntegerArgumentType.integer(1, TNT_AREA_COUNT))
                                                    .executes { context ->
                                                        setTntAreaPosition(
                                                            context.source,
                                                            IntegerArgumentType.getInteger(context, "number")
                                                        )
                                                    }
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("area")
                                    .then(
                                        Commands.literal("setpos")
                                            .executes { context -> setAreaPosition(context.source) }
                                    )
                            )
                    )
                    .then(
                        Commands.literal("skill")
                            .then(
                                Commands.literal("1")
                                    .executes { context -> castSkillOne(context.source) }
                            )
                            .then(
                                Commands.literal("2")
                                    .executes { context -> castSkillTwo(context.source) }
                            )
                            .then(
                                Commands.literal("3")
                                    .executes { context -> castSkillThree(context.source) }
                            )
                            .then(
                                Commands.literal("4")
                                    .executes { context -> castSkillFour(context.source) }
                            )
                            .then(
                                Commands.literal("global1")
                                    .executes { context -> castGlobalOne(context.source) }
                            )
                            .then(
                                Commands.literal("global2")
                                    .executes { context -> castGlobalTwo(context.source) }
                            )
                    )
            )
        }
    }

    fun requestExternalStart() {
        externalStartRequested = true
    }

    fun requestExternalStop() {
        externalStopRequested = true
    }

    fun isGameRunning(): Boolean = autoPatternTask != null || startSequenceRunning

    fun getPlayerLives(uuid: UUID): Int = playerLives[uuid] ?: 0

    fun getAlivePlayerIds(): List<UUID> =
        if (isGameRunning()) playerLives.filterValues { lives -> lives > 0 }.keys.toList() else emptyList()

    fun getRemainingPlayerCount(): Int = getAlivePlayerIds().size

    fun applyPatternHit(player: ServerPlayer): Boolean {
        if (!isGameRunning()) {
            return false
        }
        val currentLives = playerLives[player.uuid] ?: return false
        if (currentLives <= 0 || !isPlayerInLifeArea(player)) {
            return false
        }

        val remaining = (currentLives - 1).coerceAtLeast(0)
        playerLives[player.uuid] = remaining
        sendLifeHud(player)

        if (remaining <= 0) {
            player.health = 0.0f
        } else {
            player.health = (player.health - PLAYER_HIT_DAMAGE).coerceAtLeast(1.0f)
            player.invulnerableTime = 0
        }
        return true
    }

    private fun tickExternalRequests(server: MinecraftServer) {
        readExternalControlFile()
        if (externalStopRequested) {
            externalStopRequested = false
            stop(server.createCommandSourceStack().withSuppressedOutput())
        }
        if (externalStartRequested) {
            externalStartRequested = false
            start(server.createCommandSourceStack().withSuppressedOutput())
        }
    }

    private fun readExternalControlFile() {
        if (!Files.exists(controlFile)) {
            return
        }

        val props = Properties()
        runCatching {
            Files.newInputStream(controlFile).use(props::load)
            Files.deleteIfExists(controlFile)
        }.onFailure {
            return
        }

        when (props.getProperty("command")) {
            "start" -> externalStartRequested = true
            "stop" -> externalStopRequested = true
        }
    }

    private fun summon(source: CommandSourceStack, pos: Vec3): Int {
        val entity = RabbitBossEntity(ModEntities.RABBIT_BOSS, source.level)
        forceChunk(source.level, pos.x, pos.z)
        entity.moveTo(pos.x, pos.y, pos.z, source.rotation.y, source.rotation.x)
        source.level.addFreshEntity(entity)
        activeBossUuid = entity.uuid
        activeBossEntity = entity
        activeBossSpawnPosition = pos
        activeBossSpawnYaw = source.rotation.y
        source.sendSuccess({ Component.literal("Summoned Rabbit Boss") }, false)
        return Command.SINGLE_SUCCESS
    }

    private fun summonPipe(source: CommandSourceStack, pos: Vec3): Int {
        val entity = PipeEntity(ModEntities.PIPE, source.level)
        forceChunk(source.level, pos.x, pos.z)
        entity.setNoAi(true)
        entity.moveTo(pos.x, pos.y, pos.z, source.rotation.y, source.rotation.x)
        entity.yHeadRot = source.rotation.y
        entity.yBodyRot = source.rotation.y
        entity.refreshPipeBoundingBox()
        source.level.addFreshEntity(entity)
        source.sendSuccess({ Component.literal("Summoned Pipe") }, false)
        return Command.SINGLE_SUCCESS
    }

    private fun castSkillOne(source: CommandSourceStack): Int {
        val boss = findActiveBoss(source)
        if (boss == null) {
            source.sendFailure(Component.literal("No active Rabbit Boss. Run /rabbitboss start first."))
            return 0
        }

        skillTasks.removeAll { it.boss == boss && it is SkillOneTask }
        boss.setAnimationSpeed(1.0f)
        playSkillCue(boss.level(), boss.position().add(0.0, 2.5, 0.0), 1)
        skillTasks.add(
            SkillOneTask(
                boss,
                activeBossSpawnPosition ?: boss.position(),
                activeBossSpawnYaw
            )
        )

        source.sendSuccess({ Component.literal("Rabbit Boss skill 1 started") }, false)
        return Command.SINGLE_SUCCESS
    }

    private fun castSkillTwo(source: CommandSourceStack): Int {
        val boss = findActiveBoss(source)
        if (boss == null) {
            source.sendFailure(Component.literal("No active Rabbit Boss. Run /rabbitboss start first."))
            return 0
        }

        val canonPositions = loadCanonPositions(source)
        if (canonPositions == null) {
            source.sendFailure(Component.literal("Canon positions are not fully set. Set 1-$CANON_COUNT first."))
            return 0
        }

        faceBossFront(boss)
        boss.playAnimation("canonskill")
        playSkillCue(boss.level(), boss.position().add(0.0, 2.5, 0.0), 2)
        scheduledTasks.add(
            ScheduledTask(CANON_SKILL_ANIMATION_TICKS) {
                if (boss.isAlive) {
                    boss.playAnimation("idle")
                }
            }
        )
        val spawnedCanons = mutableListOf<CanonEntity>()
        canonPositions.forEachIndexed { index, canonPosition ->
            scheduledTasks.add(
                ScheduledTask(CANON_SKILL_DELAY_TICKS + index * CANON_SEQUENCE_INTERVAL_TICKS) {
                    spawnedCanons.add(spawnCanon(source, canonPosition))
                }
            )
        }
        scheduleCanonFireSequence(spawnedCanons)

        source.sendSuccess({ Component.literal("Rabbit Boss skill 2 started") }, false)
        return Command.SINGLE_SUCCESS
    }

    private fun castSkillThree(source: CommandSourceStack): Int {
        val boss = findActiveBoss(source)
        if (boss == null) {
            source.sendFailure(Component.literal("No active Rabbit Boss. Run /rabbitboss start first."))
            return 0
        }

        val tntAreas = loadTntAreaPositions(source)
        if (tntAreas == null) {
            source.sendFailure(Component.literal("TNT areas are not fully set. Set 1-$TNT_AREA_COUNT first."))
            return 0
        }

        faceBossFront(boss)
        val power = speedPowerLevel()
        boss.setAnimationSpeed(tntSkillAnimationSpeed(power))
        boss.playAnimation("tntskill")
        playSkillCue(boss.level(), boss.position().add(0.0, 2.5, 0.0), 3)
        scheduledTasks.add(
            ScheduledTask(tntSkillAnimationTicks(power)) {
                if (boss.isAlive) {
                    boss.setAnimationSpeed(1.0f)
                    boss.playAnimation("idle")
                }
            }
        )

        skillTasks.removeAll { it.boss == boss && it is TntSequenceTask }
        val tntSequence = (tntAreas.shuffled() + tntAreas.shuffled()).toMutableList()
        skillTasks.add(
            TntSequenceTask(
                source,
                boss,
                tntSequence,
                tntExplosionDelayTicks(power),
                tntSpawnDelayTicks(power),
                tntSpawnIntervalTicks(power),
                tntSpawnBatchSize(power)
            )
        )

        source.sendSuccess({ Component.literal("Rabbit Boss skill 3 started") }, false)
        return Command.SINGLE_SUCCESS
    }

    private fun castSkillFour(source: CommandSourceStack): Int {
        val boss = findActiveBoss(source)
        if (boss == null) {
            source.sendFailure(Component.literal("No active Rabbit Boss. Run /rabbitboss start first."))
            return 0
        }

        val missileArea = loadAreaPosition(source)
        if (missileArea == null) {
            source.sendFailure(Component.literal("Area is not set. Run /rabbitboss setting area setpos first."))
            return 0
        }

        faceBossFront(boss)
        val launchers = spawnMissileLaunchers(boss)
        playSkillCue(boss.level(), boss.position().add(0.0, 2.5, 0.0), 4)
        val missileCount = missilesPerLauncher()
        val flightTicks = missileFlightTicks()
        val allTargets = distributedMissileTargets(missileArea, launchers.size * missileCount)
        launchers.forEachIndexed { launcherIndex, launcher ->
            val targets = (0 until missileCount).map { missileIndex ->
                allTargets[missileIndex * launchers.size + launcherIndex]
            }
            targets.forEachIndexed { index, target ->
                scheduledTasks.add(
                    ScheduledTask(MISSILE_LAUNCHER_FIRE_DELAY_TICKS + index * MISSILE_FIRE_INTERVAL_TICKS) {
                        if (launcher.isAlive) {
                            fireMissile(source, launcher, missileArea, target, flightTicks)
                        }
                    }
                )
            }
            scheduledTasks.add(
                ScheduledTask(MISSILE_LAUNCHER_FIRE_DELAY_TICKS + missileCount * MISSILE_FIRE_INTERVAL_TICKS + 40) {
                    if (launcher.isAlive) {
                        launcher.discard()
                    }
                }
            )
        }

        source.sendSuccess({ Component.literal("Rabbit Boss skill 4 started") }, false)
        return Command.SINGLE_SUCCESS
    }

    private fun castGlobalOne(source: CommandSourceStack): Int {
        val boss = findActiveBoss(source)
        if (boss == null) {
            source.sendFailure(Component.literal("No active Rabbit Boss. Run /rabbitboss start first."))
            return 0
        }

        faceBossFront(boss)
        val power = speedPowerLevel()
        repeat(RAZER_COUNT) { index ->
            scheduledTasks.add(
                ScheduledTask(index * RAZER_FIRE_INTERVAL_TICKS) {
                    if (boss.isAlive) {
                        spawnRazer(boss, Random.nextBoolean(), power)
                    }
                }
            )
        }

        increasePowerAfterGlobal()
        source.sendSuccess({ Component.literal("Rabbit Boss global1 started") }, false)
        return Command.SINGLE_SUCCESS
    }

    private fun castGlobalTwo(source: CommandSourceStack): Int {
        val boss = findActiveBoss(source)
        if (boss == null) {
            source.sendFailure(Component.literal("No active Rabbit Boss. Run /rabbitboss start first."))
            return 0
        }

        faceBossFront(boss)
        val power = speedPowerLevel()
        val area = loadAreaPosition(source)
        if (area == null) {
            source.sendFailure(Component.literal("Area is not set. Run /rabbitboss setting area setpos first."))
            return 0
        }

        val freezeStartTicks = globalTwoFreezeStartTicks(power)
        spawnGlobalTwoIndicator(boss.level(), area, freezeStartTicks)
        scheduledTasks.add(
            ScheduledTask(freezeStartTicks) {
                if (boss.isAlive) {
                    skillTasks.add(GlobalTwoFreezeTask(boss, area))
                }
            }
        )
        playFreezeDanceMelody(boss.level(), bossSpawnOrigin(boss).add(0.0, 2.5, 0.0), area, power)
        increasePowerAfterGlobal()
        source.sendSuccess({ Component.literal("Rabbit Boss global2 started") }, false)
        return Command.SINGLE_SUCCESS
    }

    private fun setCanonPosition(source: CommandSourceStack, number: Int): Int {
        val props = Properties()
        if (Files.exists(canonFile)) {
            Files.newInputStream(canonFile).use(props::load)
        }

        val pos = source.position
        val yaw = snapYawToRightAngle(source.rotation.y)
        val prefix = "canon.$number"

        props["$prefix.dimension"] = source.level.dimension().location().toString()
        props["$prefix.x"] = pos.x.toString()
        props["$prefix.y"] = pos.y.toString()
        props["$prefix.z"] = pos.z.toString()
        props["$prefix.yaw"] = yaw.toString()

        Files.createDirectories(canonFile.parent)
        Files.newOutputStream(canonFile).use { output ->
            props.store(output, "RabbitBoss canon positions")
        }

        source.sendSuccess({ Component.literal("Canon $number position set. yaw=$yaw") }, false)
        return Command.SINGLE_SUCCESS
    }

    private fun setTntAreaPosition(source: CommandSourceStack, number: Int): Int {
        val props = Properties()
        if (Files.exists(tntAreaFile)) {
            Files.newInputStream(tntAreaFile).use(props::load)
        }

        val pos = source.position
        val yaw = snapYawToRightAngle(source.rotation.y)
        val prefix = "tntarea.$number"

        props["$prefix.dimension"] = source.level.dimension().location().toString()
        props["$prefix.x"] = pos.x.toString()
        props["$prefix.y"] = pos.y.toString()
        props["$prefix.z"] = pos.z.toString()
        props["$prefix.yaw"] = yaw.toString()

        Files.createDirectories(tntAreaFile.parent)
        Files.newOutputStream(tntAreaFile).use { output ->
            props.store(output, "RabbitBoss TNT area positions")
        }

        source.sendSuccess({ Component.literal("TNT area $number position set. yaw=$yaw") }, false)
        return Command.SINGLE_SUCCESS
    }

    private fun setAreaPosition(source: CommandSourceStack): Int {
        val props = Properties()
        val pos = source.position
        val yaw = snapYawToRightAngle(source.rotation.y)

        props["dimension"] = source.level.dimension().location().toString()
        props["x"] = pos.x.toString()
        props["y"] = pos.y.toString()
        props["z"] = pos.z.toString()
        props["yaw"] = yaw.toString()

        Files.createDirectories(areaFile.parent)
        Files.newOutputStream(areaFile).use { output ->
            props.store(output, "RabbitBoss area position")
        }

        source.sendSuccess({ Component.literal("Area position set. yaw=$yaw") }, false)
        return Command.SINGLE_SUCCESS
    }

    private fun loadCanonPositions(source: CommandSourceStack): List<CanonPosition>? {
        if (!Files.exists(canonFile)) {
            return null
        }

        val props = Properties()
        Files.newInputStream(canonFile).use(props::load)

        return (1..CANON_COUNT).map { number ->
            val prefix = "canon.$number"
            val dimension = props.getProperty("$prefix.dimension") ?: return null
            val x = props.getProperty("$prefix.x")?.toDoubleOrNull() ?: return null
            val y = props.getProperty("$prefix.y")?.toDoubleOrNull() ?: return null
            val z = props.getProperty("$prefix.z")?.toDoubleOrNull() ?: return null
            val yaw = props.getProperty("$prefix.yaw")?.toFloatOrNull() ?: return null
            CanonPosition(dimension, Vec3(x, y, z), yaw)
        }
    }

    private fun loadTntAreaPositions(source: CommandSourceStack): List<TntAreaPosition>? {
        if (!Files.exists(tntAreaFile)) {
            return null
        }

        val props = Properties()
        Files.newInputStream(tntAreaFile).use(props::load)

        return (1..TNT_AREA_COUNT).map { number ->
            val prefix = "tntarea.$number"
            val dimension = props.getProperty("$prefix.dimension") ?: return null
            val x = props.getProperty("$prefix.x")?.toDoubleOrNull() ?: return null
            val y = props.getProperty("$prefix.y")?.toDoubleOrNull() ?: return null
            val z = props.getProperty("$prefix.z")?.toDoubleOrNull() ?: return null
            val yaw = props.getProperty("$prefix.yaw")?.toFloatOrNull() ?: return null
            TntAreaPosition(dimension, Vec3(x, y, z), yaw)
        }
    }

    private fun loadAreaPosition(source: CommandSourceStack): AreaPosition? {
        val file = when {
            Files.exists(areaFile) -> areaFile
            Files.exists(legacyMissileAreaFile) -> legacyMissileAreaFile
            else -> null
        } ?: return null

        if (!Files.exists(file)) {
            return null
        }

        val props = Properties()
        Files.newInputStream(file).use(props::load)

        val dimension = props.getProperty("dimension") ?: return null
        val x = props.getProperty("x")?.toDoubleOrNull() ?: return null
        val y = props.getProperty("y")?.toDoubleOrNull() ?: return null
        val z = props.getProperty("z")?.toDoubleOrNull() ?: return null
        val yaw = props.getProperty("yaw")?.toFloatOrNull() ?: return null
        return AreaPosition(dimension, Vec3(x, y, z), yaw)
    }

    private fun loadSpawnConfig(source: CommandSourceStack): SpawnConfig? {
        if (!Files.exists(spawnFile)) {
            return null
        }

        val props = Properties()
        Files.newInputStream(spawnFile).use(props::load)

        val dimension = props.getProperty("dimension") ?: return null
        val x = props.getProperty("x")?.toDoubleOrNull() ?: return null
        val y = props.getProperty("y")?.toDoubleOrNull() ?: return null
        val z = props.getProperty("z")?.toDoubleOrNull() ?: return null
        val yaw = props.getProperty("yaw")?.toFloatOrNull() ?: return null
        val pitch = props.getProperty("pitch")?.toFloatOrNull() ?: 0.0f
        return SpawnConfig(dimension, Vec3(x, y, z), yaw, pitch)
    }

    private fun spawnTntArea(
        source: CommandSourceStack,
        boss: RabbitBossEntity,
        tntArea: TntAreaPosition,
        explosionDelayTicks: Int
    ): Boolean {
        val dimensionId = ResourceLocation.parse(tntArea.dimension)
        val dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionId)
        val level = source.server.getLevel(dimensionKey) ?: source.level
        val pos = tntArea.position
        val tnt = TntEntity(ModEntities.TNT, level)

        forceChunk(level, pos.x, pos.z)
        tnt.setNoAi(true)
        tnt.setAnimationSpeed(TNT_EXPLOSION_DELAY_TICKS.toFloat() / explosionDelayTicks.toFloat())
        tnt.moveTo(pos.x, pos.y, pos.z, tntArea.yaw, 0.0f)
        tnt.yHeadRot = tntArea.yaw
        tnt.yBodyRot = tntArea.yaw
        level.addFreshEntity(tnt)
        spawnTntIndicator(level, tntArea, explosionDelayTicks)
        spawnTntSpawnEffect(level, pos)
        scheduledTasks.add(
            ScheduledTask(10) {
                playHostileSound(level, pos, SoundEvents.GENERIC_BIG_FALL, 3.0f, 0.55f)
                if (level is ServerLevel) {
                    sendForcedParticles(level, ParticleTypes.POOF, pos.add(0.0, 0.25, 0.0), 30, 0.8f, 0.15f, 0.8f, 0.03f)
                }
            }
        )
        activeTntAreas.add(tntArea)
        skillTasks.add(TntAreaTask(boss, tnt, tntArea, explosionDelayTicks))
        return true
    }

    private fun spawnTntIndicator(level: Level, tntArea: TntAreaPosition, durationTicks: Int) {
        val indicator = TntIndicatorEntity(ModEntities.TNT_INDICATOR, level)
        val pos = tntArea.position

        indicator.setDurationTicks(durationTicks)
        indicator.moveTo(pos.x, pos.y, pos.z, tntArea.yaw, 0.0f)
        indicator.yRot = tntArea.yaw
        indicator.yRotO = tntArea.yaw
        level.addFreshEntity(indicator)
    }

    private fun spawnMissileLaunchers(boss: RabbitBossEntity): List<MissileLauncherEntity> {
        val level = boss.level()
        val yaw = bossFrontYaw(boss)
        val forward = horizontalDirection(yaw)
        val right = Vec3(forward.z, 0.0, -forward.x).normalize()
        val origin = bossSpawnOrigin(boss)
        return listOf(-1.0, 1.0).map { side ->
            val pos = origin
                .add(forward.scale(MISSILE_LAUNCHER_FORWARD_OFFSET))
                .add(right.scale(MISSILE_LAUNCHER_SIDE_OFFSET * side))
            val launcher = MissileLauncherEntity(ModEntities.MISSILE_LAUNCHER, level)
            forceChunk(level, pos.x, pos.z)
            launcher.setNoAi(true)
            launcher.moveTo(pos.x, pos.y, pos.z, yaw, 0.0f)
            launcher.yHeadRot = yaw
            launcher.yBodyRot = yaw
            level.addFreshEntity(launcher)
            spawnLauncherSpawnEffect(level, pos)
            scheduledTasks.add(
                ScheduledTask(10) {
                    if (launcher.isAlive) {
                        playHostileSound(level, pos, SoundEvents.ANVIL_LAND, 2.6f, 0.7f)
                    }
                }
            )
            launcher
        }
    }

    private fun fireMissile(
        source: CommandSourceStack,
        launcher: MissileLauncherEntity,
        missileArea: AreaPosition,
        target: Vec3,
        flightTicks: Int
    ) {
        val dimensionId = ResourceLocation.parse(missileArea.dimension)
        val dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionId)
        val level = source.server.getLevel(dimensionKey) ?: launcher.level()
        val start = launcher.position().add(0.0, 4.0, 0.0)
        val missile = MissileEntity(ModEntities.MISSILE, level)

        forceChunk(level, start.x, start.z)
        forceChunk(level, target.x, target.z)
        spawnMissileIndicator(level, missileArea.yaw, target)
        spawnMissileLaunchEffect(level, start, target.subtract(start))
        missile.launch(start, target.add(0.0, 0.8, 0.0), flightTicks)
        level.addFreshEntity(missile)
    }

    private fun distributedMissileTargets(missileArea: AreaPosition, count: Int): List<Vec3> {
        val candidates = (-MISSILE_AREA_FORWARD_RANGE..MISSILE_AREA_FORWARD_RANGE).flatMap { front ->
            (-MISSILE_AREA_SIDE_RANGE..MISSILE_AREA_SIDE_RANGE).map { side -> front to side }
        }.shuffled().toMutableList()
        val selected = mutableListOf<Pair<Int, Int>>()

        if (candidates.isNotEmpty()) {
            selected.add(candidates.removeAt(Random.nextInt(candidates.size)))
        }

        while (selected.size < count && candidates.isNotEmpty()) {
            var bestIndex = 0
            var bestScore = Int.MIN_VALUE

            candidates.forEachIndexed { index, candidate ->
                val score = selected.minOf { selectedTarget ->
                    kotlin.math.max(
                        kotlin.math.abs(candidate.first - selectedTarget.first),
                        kotlin.math.abs(candidate.second - selectedTarget.second)
                    )
                }

                if (score > bestScore) {
                    bestScore = score
                    bestIndex = index
                }
            }

            selected.add(candidates.removeAt(bestIndex))
        }

        return selected.map { (front, side) ->
            missileTarget(missileArea, front.toDouble(), side.toDouble())
        }
    }

    private fun missileTarget(missileArea: AreaPosition, front: Double, side: Double): Vec3 {
        val forward = horizontalDirection(missileArea.yaw)
        val right = Vec3(forward.z, 0.0, -forward.x).normalize()
        return missileArea.position
            .add(forward.scale(front))
            .add(right.scale(side))
    }

    private fun spawnMissileIndicator(level: Level, yaw: Float, target: Vec3) {
        val indicator = MissileIndicatorEntity(ModEntities.MISSILE_INDICATOR, level)
        indicator.moveTo(target.x, target.y, target.z, yaw, 0.0f)
        indicator.yRot = yaw
        indicator.yRotO = yaw
        level.addFreshEntity(indicator)
    }

    private fun spawnGlobalTwoIndicator(level: Level, area: AreaPosition, fillTicks: Int) {
        val indicator = GlobalTwoIndicatorEntity(ModEntities.GLOBAL_TWO_INDICATOR, level)
        val pos = area.position
        forceChunk(level, pos.x, pos.z)
        indicator.setTiming(fillTicks, fillTicks + GLOBAL_TWO_FREEZE_DURATION_TICKS)
        indicator.moveTo(pos.x, pos.y, pos.z, area.yaw, 0.0f)
        indicator.yRot = area.yaw
        indicator.yRotO = area.yaw
        level.addFreshEntity(indicator)
    }

    private fun spawnCanon(source: CommandSourceStack, canonPosition: CanonPosition): CanonEntity {
        val dimensionId = ResourceLocation.parse(canonPosition.dimension)
        val dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionId)
        val level = source.server.getLevel(dimensionKey) ?: source.level
        val pos = canonPosition.position
        val canon = CanonEntity(ModEntities.CANON, level)

        forceChunk(level, pos.x, pos.z)
        canon.setNoAi(true)
        canon.moveTo(pos.x, pos.y, pos.z, canonPosition.yaw, 0.0f)
        canon.yHeadRot = canonPosition.yaw
        canon.yBodyRot = canonPosition.yaw
        level.addFreshEntity(canon)
        spawnCanonSpawnEffect(level, pos)
        scheduledTasks.add(
            ScheduledTask(20) {
                if (canon.isAlive) {
                    playHostileSound(level, pos, SoundEvents.ANVIL_LAND, 3.0f, 0.6f)
                }
            }
        )
        return canon
    }

    private fun scheduleCanonFireSequence(canons: List<CanonEntity>) {
        val firstFireDelay = CANON_SKILL_DELAY_TICKS +
            (CANON_COUNT - 1) * CANON_SEQUENCE_INTERVAL_TICKS +
            CANON_FIRE_AFTER_ALL_SPAWNED_TICKS
        val shotsPerVolley = cannonShotsPerVolley()
        val boomSpeed = boomLaunchSpeed()

        scheduledTasks.add(
            ScheduledTask(firstFireDelay) {
                repeat(CANON_FIRE_ROUNDS) { round ->
                    val volleys = canons.shuffled().chunked(shotsPerVolley)
                    volleys.forEachIndexed { index, volley ->
                        scheduledTasks.add(
                            ScheduledTask((round * volleys.size + index) * CANON_FIRE_INTERVAL_TICKS) {
                                volley.forEach { canon ->
                                    fireCanon(canon, round == CANON_FIRE_ROUNDS - 1, boomSpeed)
                                }
                            }
                        )
                    }
                }
            }
        )
    }

    private fun fireCanon(canon: CanonEntity, discardAfterFire: Boolean, boomSpeed: Double) {
        if (!canon.isAlive) {
            return
        }

        val yaw = canon.yBodyRot
        val direction = horizontalDirection(yaw)
        val pos = canonExplodePosition(canon)
        val boom = BoomEntity(ModEntities.BOOM, canon.level())

        forceChunk(canon.level(), pos.x, pos.z)
        spawnCanonExplosionEffect(canon, pos)
        playHostileSound(canon.level(), pos, SoundEvents.GENERIC_EXPLODE.value(), 4.0f, 0.75f)
        boom.moveTo(pos.x, pos.y, pos.z, yaw, 0.0f)
        boom.yHeadRot = yaw
        boom.yBodyRot = yaw
        boom.launch(direction, boomSpeed)
        canon.level().addFreshEntity(boom)

        if (discardAfterFire) {
            scheduledTasks.add(
                ScheduledTask(CANON_DISCARD_AFTER_FIRE_TICKS) {
                    if (canon.isAlive) {
                        canon.discard()
                    }
                }
            )
        }
    }

    private fun canonExplodePosition(canon: CanonEntity): Vec3 {
        val direction = horizontalDirection(canon.yBodyRot)
        return canon.position()
            .add(direction.scale(CANON_EXPLODE_ANIMATED_FORWARD * CANON_MODEL_SCALE))
            .add(0.0, CANON_EXPLODE_ANIMATED_Y * CANON_MODEL_SCALE + CANON_EXPLODE_Y_OFFSET, 0.0)
    }

    private fun spawnCanonExplosionEffect(canon: CanonEntity, pos: Vec3) {
        val level = canon.level()
        if (level !is ServerLevel) {
            return
        }

        sendForcedParticles(level, ParticleTypes.EXPLOSION_EMITTER, pos, 4, 0.5f, 0.5f, 0.5f, 0.0f)
        sendForcedParticles(level, ParticleTypes.EXPLOSION, pos, 28, 3.5f, 3.5f, 3.5f, 0.04f)
        sendForcedParticles(level, ParticleTypes.FLAME, pos, 260, 4.0f, 4.0f, 4.0f, 0.1f)
        sendForcedParticles(level, ParticleTypes.LARGE_SMOKE, pos, 180, 3.5f, 3.5f, 3.5f, 0.08f)
    }

    private fun playSkillCue(level: Level, pos: Vec3, skill: Int) {
        if (level !is ServerLevel) {
            return
        }

        sendForcedParticles(level, ParticleTypes.POOF, pos, 60, 1.5f, 0.8f, 1.5f, 0.08f)
        sendForcedParticles(level, ParticleTypes.CRIT, pos.add(0.0, 0.4, 0.0), 40, 1.2f, 0.7f, 1.2f, 0.18f)
    }

    private fun playFreezeDanceMelody(level: Level, pos: Vec3, area: AreaPosition, power: Int) {
        if (level !is ServerLevel) {
            return
        }

        val notes = listOf(
            // Phrase 1
            MusicNote(0, notePitch("G5"), "\uC990"),
            MusicNote(10, notePitch("E5"), "\uC990\uAC81"),
            MusicNote(20, notePitch("C5"), "\uC990\uAC81\uAC8C"),

            // Phrase 2
            MusicNote(40, notePitch("D5"), "\uCD94"),
            MusicNote(45, notePitch("E5"), "\uCD94\uC744"),
            MusicNote(50, notePitch("F5"), "\uCD94\uC744\uCD94"),
            MusicNote(55, notePitch("D5"), "\uCD94\uC744\uCD94\uB2E4"),
            MusicNote(60, notePitch("B4"), "\uCD94\uC744\uCD94\uB2E4\uAC00"),

            // Phrase 3
            MusicNote(80, notePitch("C5"), "\uADF8"),
            MusicNote(90, notePitch("C5"), "\uADF8\uB300"),
            MusicNote(95, notePitch("C5"), "\uADF8\uB300\uB85C"),
            MusicNote(100, notePitch("E5"), "\uBA48"),
            MusicNote(110, notePitch("E5"), "\uBA48\uCCD0"),
            MusicNote(120, notePitch("G5"), "\uBA48\uCCD0\uB77C!")
        )

        notes.forEach { note ->
            scheduledTasks.add(
                ScheduledTask(globalTwoDelayTicks(note.delayTicks, power)) {
                    playHostileSound(level, pos, SoundEvents.NOTE_BLOCK_HARP.value(), 8.0f, note.pitch)
                    sendForcedParticles(level, ParticleTypes.NOTE, pos, 4, 0.35f, 0.25f, 0.35f, 0.0f)
                    sendAreaTitle(level, area, note.title)
                }
            )
        }
    }

    private fun sendAreaTitle(level: ServerLevel, area: AreaPosition, title: String) {
        val forward = horizontalDirection(area.yaw)
        val right = Vec3(forward.z, 0.0, -forward.x).normalize()
        val animation = ClientboundSetTitlesAnimationPacket(0, 14, 0)
        val titlePacket = ClientboundSetTitleTextPacket(Component.literal(title))

        level.players()
            .filter { player -> isInsideArea(player.position(), area, forward, right) }
            .forEach { player ->
                player.connection.send(animation)
                player.connection.send(titlePacket)
            }
    }

    private fun notePitch(noteName: String): Float {
        val midi = when (noteName) {
            "B4" -> 71
            "C5" -> 72
            "D5" -> 74
            "E5" -> 76
            "F5" -> 77
            "G5" -> 79
            else -> 72
        }

        val playableMidi = midi - 12
        return Math.pow(2.0, (playableMidi - 66) / 12.0).toFloat()
    }

    private fun spawnTntSpawnEffect(level: Level, pos: Vec3) {
        if (level !is ServerLevel) {
            return
        }

        sendForcedParticles(level, ParticleTypes.LARGE_SMOKE, pos.add(0.0, 1.0, 0.0), 45, 1.0f, 0.5f, 1.0f, 0.04f)
        sendForcedParticles(level, ParticleTypes.FLAME, pos.add(0.0, 0.45, 0.0), 35, 0.8f, 0.2f, 0.8f, 0.03f)
    }

    private fun spawnCanonSpawnEffect(level: Level, pos: Vec3) {
        if (level !is ServerLevel) {
            return
        }

        sendForcedParticles(level, ParticleTypes.POOF, pos.add(0.0, 0.4, 0.0), 48, 1.3f, 0.4f, 1.3f, 0.05f)
        sendForcedParticles(level, ParticleTypes.CLOUD, pos.add(0.0, 0.2, 0.0), 35, 1.6f, 0.15f, 1.6f, 0.02f)
    }

    private fun spawnLauncherSpawnEffect(level: Level, pos: Vec3) {
        if (level !is ServerLevel) {
            return
        }

        sendForcedParticles(level, ParticleTypes.POOF, pos.add(0.0, 1.0, 0.0), 70, 1.5f, 0.7f, 1.5f, 0.06f)
        sendForcedParticles(level, ParticleTypes.LARGE_SMOKE, pos.add(0.0, 0.5, 0.0), 55, 1.3f, 0.35f, 1.3f, 0.04f)
    }

    private fun spawnMissileLaunchEffect(level: Level, pos: Vec3, direction: Vec3) {
        if (level !is ServerLevel) {
            return
        }

        val back = if (direction.lengthSqr() > 1.0E-6) direction.normalize().scale(-1.0) else Vec3(0.0, 0.0, -1.0)
        val exhaust = pos.add(back.scale(1.4))
        playHostileSound(level, pos, SoundEvents.FIREWORK_ROCKET_LAUNCH, 2.5f, 0.75f)
        sendForcedParticles(level, ParticleTypes.FLAME, exhaust, 45, 0.45f, 0.35f, 0.45f, 0.08f)
        sendForcedParticles(level, ParticleTypes.LARGE_SMOKE, exhaust, 55, 0.55f, 0.45f, 0.55f, 0.07f)
    }

    private fun spawnPipeLaunchEffect(level: Level, pos: Vec3, direction: Vec3) {
        if (level !is ServerLevel) {
            return
        }

        val back = if (direction.lengthSqr() > 1.0E-6) direction.normalize().scale(-1.0) else Vec3(0.0, 0.0, -1.0)
        val exhaust = pos.add(back.scale(2.0)).add(0.0, 1.0, 0.0)
        playHostileSound(level, pos, SoundEvents.TRIDENT_THROW.value(), 3.0f, 0.6f)
        sendForcedParticles(level, ParticleTypes.CLOUD, exhaust, 60, 1.1f, 0.6f, 1.1f, 0.08f)
        sendForcedParticles(level, ParticleTypes.CRIT, pos.add(0.0, 1.0, 0.0), 45, 0.8f, 0.35f, 0.8f, 0.16f)
    }

    private fun playHostileSound(level: Level, pos: Vec3, sound: SoundEvent, volume: Float, pitch: Float) {
        level.playSound(null, pos.x, pos.y, pos.z, sound, SoundSource.HOSTILE, volume.coerceAtLeast(16.0f), pitch)
    }

    private fun sendForcedParticles(
        level: ServerLevel,
        particle: net.minecraft.core.particles.ParticleOptions,
        pos: Vec3,
        count: Int,
        xOffset: Float,
        yOffset: Float,
        zOffset: Float,
        speed: Float
    ) {
        val packet = ClientboundLevelParticlesPacket(
            particle,
            true,
            true,
            pos.x,
            pos.y,
            pos.z,
            xOffset,
            yOffset,
            zOffset,
            speed,
            count
        )
        level.players().forEach { player ->
            level.sendParticles(player, true, pos.x, pos.y, pos.z, packet)
        }
    }

    private fun spawnLaunchedPipe(boss: RabbitBossEntity) {
        val yaw = bossFrontYaw(boss)
        val direction = horizontalDirection(yaw)
        val pos = boss.position().add(direction.scale(6.0)).add(0.0, 1.0, 0.0)
        val pipe = PipeEntity(ModEntities.PIPE, boss.level())

        forceChunk(boss.level(), pos.x, pos.z)
        spawnPipeLaunchEffect(boss.level(), pos, direction)
        pipe.moveTo(pos.x, pos.y, pos.z, yaw, 0.0f)
        pipe.yHeadRot = yaw
        pipe.yBodyRot = yaw
        pipe.launch(direction, pipeLaunchSpeed())
        pipe.refreshPipeBoundingBox()
        boss.level().addFreshEntity(pipe)
        schedulePipeImpactSounds(pipe)
    }

    private fun spawnRazer(boss: RabbitBossEntity, high: Boolean, power: Int) {
        val yaw = bossFrontYaw(boss)
        val direction = horizontalDirection(yaw)
        val yOffset = if (high) RAZER_HIGH_OFFSET else 0.0
        val pos = bossSpawnOrigin(boss)
            .add(direction.scale(RAZER_FORWARD_OFFSET))
            .add(0.0, -0.2 + yOffset, 0.0)
        val razer = RazerEntity(ModEntities.RAZER, boss.level())

        forceChunk(boss.level(), pos.x, pos.z)
        if (boss.level() is ServerLevel) {
            val level = boss.level() as ServerLevel
            sendForcedParticles(level, ParticleTypes.END_ROD, pos, 35, 0.55f, 0.18f, 0.55f, 0.05f)
            sendForcedParticles(level, ParticleTypes.ELECTRIC_SPARK, pos, 45, 0.8f, 0.2f, 0.8f, 0.08f)
        }
        playHostileSound(boss.level(), pos, SoundEvents.BEACON_ACTIVATE, 2.0f, if (high) 1.45f else 1.05f)
        razer.moveTo(pos.x, pos.y, pos.z, yaw, 0.0f)
        razer.yHeadRot = yaw
        razer.yBodyRot = yaw
        razer.launch(direction, razerLaunchSpeed(power))
        razer.refreshRazerBoundingBox()
        boss.level().addFreshEntity(razer)
    }

    private fun schedulePipeImpactSounds(pipe: PipeEntity) {
        listOf(10, 26, 40).forEach { delay ->
            scheduledTasks.add(
                ScheduledTask(delay) {
                    if (pipe.isAlive) {
                        val pos = pipe.position().add(0.0, PipeEntity.HEIGHT * 0.45, 0.0)
                        playHostileSound(pipe.level(), pos, SoundEvents.ANVIL_PLACE, 2.6f, 1.65f)
                        if (pipe.level() is ServerLevel) {
                            sendForcedParticles(pipe.level() as ServerLevel, ParticleTypes.CRIT, pos, 16, 0.5f, 0.2f, 0.5f, 0.08f)
                        }
                    }
                }
            )
        }
    }

    private fun setSpawn(source: CommandSourceStack): Int {
        val props = Properties()
        val pos = source.position
        val rotation = source.rotation

        props["dimension"] = source.level.dimension().location().toString()
        props["x"] = pos.x.toString()
        props["y"] = pos.y.toString()
        props["z"] = pos.z.toString()
        props["yaw"] = rotation.y.toString()
        props["pitch"] = rotation.x.toString()

        Files.createDirectories(spawnFile.parent)
        Files.newOutputStream(spawnFile).use { output ->
            props.store(output, "RabbitBoss spawn point")
        }

        source.sendSuccess({ Component.literal("Rabbit Boss spawn point set") }, false)
        return Command.SINGLE_SUCCESS
    }

    private fun start(source: CommandSourceStack): Int {
        if (autoPatternTask != null || startSequenceRunning) {
            source.sendFailure(Component.literal("Rabbit Boss pattern loop is already running"))
            return 0
        }

        if (!Files.exists(spawnFile)) {
            source.sendFailure(Component.literal("Rabbit Boss spawn point is not set"))
            return 0
        }

        val missingSettings = missingPatternSettings(source)
        if (missingSettings.isNotEmpty()) {
            source.sendFailure(Component.literal("Pattern settings are missing: ${missingSettings.joinToString(", ")}"))
            return 0
        }

        val spawn = loadSpawnConfig(source) ?: run {
            source.sendFailure(Component.literal("Rabbit Boss spawn point is invalid"))
            return 0
        }

        val dimensionId = ResourceLocation.parse(spawn.dimension)
        val dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionId)
        val level = source.server.getLevel(dimensionKey) ?: source.level

        scheduledTasks.clear()
        skillTasks.clear()
        activeTntAreas.clear()
        globalPowerLevel = 0
        startSequenceRunning = true
        initializePlayerLives(source)
        discardRabbitBossEntities(source)
        var boss: RabbitBossEntity? = null
        val fox = spawnStartFox(level, spawn)

        scheduledTasks.add(
            ScheduledTask(START_FOX_TO_BOSS_TICKS) {
                boss = prepareStartBoss(level, spawn, boss)
            }
        )
        scheduledTasks.add(
            ScheduledTask(START_FOX_TO_BOSS_TICKS + START_BOSS_TO_FOX_EXPLODE_TICKS) {
                if (fox.isAlive) {
                    fox.playAnimation("explode")
                    playHostileSound(level, fox.position().add(0.0, 2.0, 0.0), SoundEvents.GENERIC_EXPLODE.value(), 5.0f, 0.65f)
                    if (level is ServerLevel) {
                        sendForcedParticles(level, ParticleTypes.EXPLOSION_EMITTER, fox.position().add(0.0, 2.0, 0.0), 2, 0.4f, 0.4f, 0.4f, 0.0f)
                        sendForcedParticles(level, ParticleTypes.LARGE_SMOKE, fox.position().add(0.0, 1.2, 0.0), 120, 2.5f, 1.2f, 2.5f, 0.08f)
                    }
                }
            }
        )
        scheduledTasks.add(
            ScheduledTask(START_FOX_TO_BOSS_TICKS + START_BOSS_TO_FOX_EXPLODE_TICKS + START_FOX_REMOVE_AFTER_EXPLODE_TICKS) {
                if (fox.isAlive) {
                    fox.discard()
                }
            }
        )
        scheduledTasks.add(
            ScheduledTask(
                START_FOX_TO_BOSS_TICKS +
                    START_BOSS_TO_FOX_EXPLODE_TICKS +
                    START_FOX_REMOVE_AFTER_EXPLODE_TICKS +
                    START_PATTERN_AFTER_FOX_REMOVE_TICKS
            ) {
                val entity = boss
                if (entity != null && entity.isAlive) {
                    autoPatternTask = AutoPatternTask(source, entity)
                    startSequenceRunning = false
                    source.sendSuccess({ Component.literal("Rabbit Boss pattern loop started") }, false)
                } else {
                    startSequenceRunning = false
                    source.sendFailure(Component.literal("Rabbit Boss start sequence failed"))
                }
            }
        )

        source.sendSuccess({ Component.literal("Rabbit Boss start sequence started") }, false)
        return Command.SINGLE_SUCCESS
    }

    private fun stop(source: CommandSourceStack): Int {
        autoPatternTask = null
        startSequenceRunning = false
        scheduledTasks.clear()
        skillTasks.clear()
        activeTntAreas.clear()
        clearLifeHud()
        playerLives.clear()
        globalPowerLevel = 0
        discardPatternEntities(source)
        findActiveBoss(source)?.let { boss ->
            boss.setAnimationSpeed(1.0f)
            boss.playAnimation("idle")
        }
        source.sendSuccess({ Component.literal("Rabbit Boss pattern loop stopped") }, false)
        return Command.SINGLE_SUCCESS
    }

    private fun missingPatternSettings(source: CommandSourceStack): List<String> {
        val missing = mutableListOf<String>()
        if (loadCanonPositions(source) == null) {
            missing.add("canon 1-$CANON_COUNT")
        }
        if (loadTntAreaPositions(source) == null) {
            missing.add("tntarea 1-$TNT_AREA_COUNT")
        }
        if (loadAreaPosition(source) == null) {
            missing.add("area")
        }
        return missing
    }

    private fun discardPatternEntities(source: CommandSourceStack) {
        source.server.allLevels.forEach { level ->
            level.getEntities(ModEntities.PIPE) { true }.forEach { it.discard() }
            level.getEntities(ModEntities.CANON) { true }.forEach { it.discard() }
            level.getEntities(ModEntities.BOOM) { true }.forEach { it.discard() }
            level.getEntities(ModEntities.TNT) { true }.forEach { it.discard() }
            level.getEntities(ModEntities.TNT_INDICATOR) { true }.forEach { it.discard() }
            level.getEntities(ModEntities.MISSILE_LAUNCHER) { true }.forEach { it.discard() }
            level.getEntities(ModEntities.MISSILE) { true }.forEach { it.discard() }
            level.getEntities(ModEntities.MISSILE_INDICATOR) { true }.forEach { it.discard() }
            level.getEntities(ModEntities.GLOBAL_TWO_INDICATOR) { true }.forEach { it.discard() }
            level.getEntities(ModEntities.RAZER) { true }.forEach { it.discard() }
            level.getEntities(ModEntities.FOX_EXPLODE) { true }.forEach { it.discard() }
        }
    }

    private fun discardRabbitBossEntities(source: CommandSourceStack) {
        activeBossUuid = null
        activeBossEntity = null
        source.server.allLevels.forEach { level ->
            level.getEntities(ModEntities.RABBIT_BOSS) { true }.forEach { it.discard() }
        }
    }

    private fun spawnStartFox(level: ServerLevel, spawn: SpawnConfig): FoxExplodeEntity {
        val fox = FoxExplodeEntity(ModEntities.FOX_EXPLODE, level)
        val pos = spawn.position

        forceChunk(level, pos.x, pos.z)
        fox.setNoAi(true)
        fox.moveTo(pos.x, pos.y, pos.z, spawn.yaw, 0.0f)
        fox.yHeadRot = spawn.yaw
        fox.yBodyRot = spawn.yaw
        fox.playAnimation("idle")
        level.addFreshEntity(fox)
        playHostileSound(level, pos.add(0.0, 1.0, 0.0), SoundEvents.FOX_AMBIENT, 3.5f, 0.85f)
        sendForcedParticles(level, ParticleTypes.CLOUD, pos.add(0.0, 0.3, 0.0), 50, 1.2f, 0.2f, 1.2f, 0.03f)
        return fox
    }

    private fun prepareStartBoss(
        level: ServerLevel,
        spawn: SpawnConfig,
        existingBoss: RabbitBossEntity?
    ): RabbitBossEntity {
        val pos = spawn.position
        val boss = if (existingBoss != null && existingBoss.isAlive) {
            existingBoss
        } else {
            RabbitBossEntity(ModEntities.RABBIT_BOSS, level).also { level.addFreshEntity(it) }
        }

        forceChunk(level, pos.x, pos.z)
        boss.setNoAi(true)
        boss.setAnimationSpeed(1.0f)
        boss.moveTo(pos.x, pos.y, pos.z, spawn.yaw, spawn.pitch)
        boss.yHeadRot = spawn.yaw
        boss.yBodyRot = spawn.yaw
        boss.playAnimation("spawn")
        activeBossUuid = boss.uuid
        activeBossEntity = boss
        activeBossSpawnPosition = spawn.position
        activeBossSpawnYaw = spawn.yaw
        playHostileSound(level, pos.add(0.0, 2.0, 0.0), SoundEvents.ILLUSIONER_PREPARE_MIRROR, 5.0f, 0.7f)
        sendForcedParticles(level, ParticleTypes.POOF, pos.add(0.0, 1.0, 0.0), 90, 1.8f, 0.8f, 1.8f, 0.08f)
        return boss
    }

    private fun playAnimation(source: CommandSourceStack, animationName: String): Int {
        if (animationName !in RabbitBossEntity.ANIMATION_NAMES) {
            source.sendFailure(Component.literal("Unknown Rabbit Boss animation: $animationName"))
            return 0
        }

        val boss = findActiveBoss(source)
        if (boss == null) {
            source.sendFailure(Component.literal("No active Rabbit Boss. Run /rabbitboss start first."))
            return 0
        }

        boss.playAnimation(animationName)
        source.sendSuccess({ Component.literal("Playing Rabbit Boss animation: $animationName") }, false)
        return Command.SINGLE_SUCCESS
    }

    private fun findActiveBoss(source: CommandSourceStack): RabbitBossEntity? {
        activeBossEntity?.let { boss ->
            if (boss.isAlive) {
                return boss
            }
        }

        val uuid = activeBossUuid
        if (uuid != null) {
            source.server.allLevels.forEach { level ->
                val entity = level.getEntity(uuid)
                if (entity is RabbitBossEntity && entity.isAlive) {
                    activeBossEntity = entity
                    return entity
                }
            }
        }

        source.server.allLevels.forEach { level ->
            val boss = level.getEntities(ModEntities.RABBIT_BOSS) { it.isAlive }.firstOrNull()
            if (boss != null) {
                activeBossUuid = boss.uuid
                activeBossEntity = boss
                val spawn = loadSpawnConfig(source)
                activeBossSpawnPosition = spawn?.position ?: boss.position()
                activeBossSpawnYaw = spawn?.yaw ?: boss.yBodyRot
                return boss
            }
        }

        return null
    }

    private fun tickScheduledTasks() {
        var index = 0
        while (index < scheduledTasks.size) {
            val task = scheduledTasks[index]
            task.ticks--
            if (task.ticks <= 0) {
                scheduledTasks.removeAt(index)
                task.action()
            } else {
                index++
            }
        }
    }

    private fun tickSkillTasks() {
        var index = 0
        while (index < skillTasks.size) {
            val task = skillTasks[index]
            if (task.tick()) {
                skillTasks.removeAt(index)
            } else {
                index++
            }
        }
    }

    private fun tickAutoPatternTask() {
        val task = autoPatternTask ?: return
        if (task.tick()) {
            autoPatternTask = null
        }
    }

    private fun initializePlayerLives(source: CommandSourceStack) {
        playerLives.clear()
        val area = loadAreaPosition(source) ?: return
        val dimensionId = ResourceLocation.parse(area.dimension)
        val dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionId)
        val level = source.server.getLevel(dimensionKey) ?: return
        val forward = horizontalDirection(area.yaw)
        val right = Vec3(forward.z, 0.0, -forward.x).normalize()

        level.players()
            .filter { player -> isInsideArea(player.position(), area, forward, right) }
            .forEach { player ->
                playerLives[player.uuid] = PLAYER_LIVES
                sendLifeHud(player)
            }
    }

    private fun handlePlayerDamage(entity: LivingEntity, amount: Float): Boolean {
        val player = entity as? ServerPlayer ?: return true
        if (!isGameRunning() || amount <= 0.0f) {
            return true
        }
        val currentLives = playerLives[player.uuid] ?: return true
        if (currentLives <= 0 || !isPlayerInLifeArea(player)) {
            return true
        }

        applyPatternHit(player)
        return false
    }

    private fun tickLifeHud() {
        if (!isGameRunning()) {
            return
        }
        lifeHudTicks++
        if (lifeHudTicks % 10 != 0) {
            return
        }
        val server = activeBossEntity?.server ?: return
        playerLives.keys
            .mapNotNull { uuid -> server.playerList.getPlayer(uuid) }
            .forEach { player -> sendLifeHud(player) }
    }

    private fun clearLifeHud() {
        val server = activeBossEntity?.server ?: return
        playerLives.keys
            .mapNotNull { uuid -> server.playerList.getPlayer(uuid) }
            .forEach { player ->
                if (ServerPlayNetworking.canSend(player, LifeHudPayload.TYPE)) {
                    ServerPlayNetworking.send(player, LifeHudPayload(0, PLAYER_LIVES, false))
                }
            }
    }

    private fun sendLifeHud(player: ServerPlayer) {
        val lives = playerLives[player.uuid] ?: return
        if (ServerPlayNetworking.canSend(player, LifeHudPayload.TYPE)) {
            ServerPlayNetworking.send(player, LifeHudPayload(lives, PLAYER_LIVES, true))
        }
    }

    private fun isPlayerInLifeArea(player: ServerPlayer): Boolean {
        val area = loadAreaPosition(player.createCommandSourceStack()) ?: return false
        if (player.level().dimension().location().toString() != area.dimension) {
            return false
        }
        val forward = horizontalDirection(area.yaw)
        val right = Vec3(forward.z, 0.0, -forward.x).normalize()
        return isInsideArea(player.position(), area, forward, right)
    }

    private fun horizontalDirection(yaw: Float): Vec3 {
        val radians = Math.toRadians(yaw.toDouble())
        return Vec3(-kotlin.math.sin(radians), 0.0, kotlin.math.cos(radians)).normalize()
    }

    private fun bossFrontYaw(boss: RabbitBossEntity): Float =
        if (activeBossEntity == boss || activeBossUuid == boss.uuid) activeBossSpawnYaw else boss.yBodyRot

    private fun bossSpawnOrigin(boss: RabbitBossEntity): Vec3 =
        if (activeBossEntity == boss || activeBossUuid == boss.uuid) activeBossSpawnPosition ?: boss.position() else boss.position()

    private fun faceBossFront(boss: RabbitBossEntity) {
        faceBoss(boss, bossFrontYaw(boss))
    }

    private fun faceBoss(boss: RabbitBossEntity, yaw: Float) {
        boss.yRot = yaw
        boss.yHeadRot = yaw
        boss.yBodyRot = yaw
    }

    private fun isInsideArea(pos: Vec3, area: AreaPosition, forward: Vec3, right: Vec3): Boolean {
        val offset = pos.subtract(area.position)
        val forwardOffset = offset.dot(forward)
        val sideOffset = offset.dot(right)
        val verticalOffset = pos.y - area.position.y
        return kotlin.math.abs(forwardOffset) <= MISSILE_AREA_FORWARD_RANGE + 0.75 &&
            kotlin.math.abs(sideOffset) <= MISSILE_AREA_SIDE_RANGE + 0.75 &&
            verticalOffset >= -2.0 &&
            verticalOffset <= 6.0
    }

    private fun snapYawToRightAngle(yaw: Float): Float {
        val snapped = kotlin.math.round(yaw / 90.0f) * 90.0f
        return when {
            snapped <= -180.0f -> 180.0f
            snapped > 180.0f -> snapped - 360.0f
            else -> snapped
        }
    }

    private fun forceChunk(level: Level, x: Double, z: Double) {
        if (level is ServerLevel) {
            level.setChunkForced(x.toInt() shr 4, z.toInt() shr 4, true)
        }
    }

    private data class ScheduledTask(
        var ticks: Int,
        val action: () -> Unit
    )

    private data class CanonPosition(
        val dimension: String,
        val position: Vec3,
        val yaw: Float
    )

    private data class TntAreaPosition(
        val dimension: String,
        val position: Vec3,
        val yaw: Float
    )

    private data class AreaPosition(
        val dimension: String,
        val position: Vec3,
        val yaw: Float
    )

    private data class SpawnConfig(
        val dimension: String,
        val position: Vec3,
        val yaw: Float,
        val pitch: Float
    )

    private data class MusicNote(
        val delayTicks: Int,
        val pitch: Float,
        val title: String
    )

    private interface SkillTask {
        val boss: RabbitBossEntity

        fun tick(): Boolean
    }

    private class AutoPatternTask(
        private val source: CommandSourceStack,
        private val boss: RabbitBossEntity
    ) {
        private val patternBag = mutableListOf<Int>()
        private val globalPatternBag = mutableListOf<Int>()
        private var ticksUntilNextPattern = 0
        private var normalPatternsSinceGlobal = 0

        fun tick(): Boolean {
            if (!boss.isAlive) {
                return true
            }

            if (ticksUntilNextPattern > 0) {
                ticksUntilNextPattern--
                return false
            }

            val pattern = takePattern()
            val durationTicks = patternDuration(pattern)
            val result = when (pattern) {
                1 -> castSkillOne(source)
                2 -> castSkillTwo(source)
                3 -> castSkillThree(source)
                4 -> castSkillFour(source)
                5 -> castGlobalOne(source)
                6 -> castGlobalTwo(source)
                else -> 0
            }

            if (result == 0) {
                return true
            }

            ticksUntilNextPattern = (durationTicks - startupLeadTicks(peekPattern())).coerceAtLeast(1)
            return false
        }

        private fun takePattern(): Int {
            if (normalPatternsSinceGlobal >= NORMAL_PATTERNS_BEFORE_GLOBAL) {
                normalPatternsSinceGlobal = 0
                return takeGlobalPattern()
            }

            if (patternBag.isEmpty()) {
                patternBag.addAll(listOf(1, 2, 3, 4).shuffled())
            }

            normalPatternsSinceGlobal++
            return patternBag.removeAt(0)
        }

        private fun peekPattern(): Int {
            if (normalPatternsSinceGlobal >= NORMAL_PATTERNS_BEFORE_GLOBAL) {
                return peekGlobalPattern()
            }

            if (patternBag.isEmpty()) {
                patternBag.addAll(listOf(1, 2, 3, 4).shuffled())
            }

            return patternBag.first()
        }

        private fun patternDuration(pattern: Int): Int =
            when (pattern) {
                1 -> SKILL_ONE_PATTERN_TICKS
                2 -> skillTwoPatternTicks()
                3 -> skillThreePatternTicks()
                4 -> skillFourPatternTicks()
                5 -> GLOBAL_ONE_PATTERN_TICKS
                6 -> globalTwoPatternTicks()
                else -> SKILL_FOUR_PATTERN_TICKS
            }

        private fun startupLeadTicks(nextPattern: Int): Int =
            when (nextPattern) {
                1, 3 -> AUTO_PATTERN_LEAD_TICKS
                else -> 0
            }

        private fun takeGlobalPattern(): Int {
            if (globalPatternBag.isEmpty()) {
                globalPatternBag.addAll(listOf(5, 6).shuffled())
            }

            return globalPatternBag.removeAt(0)
        }

        private fun peekGlobalPattern(): Int {
            if (globalPatternBag.isEmpty()) {
                globalPatternBag.addAll(listOf(5, 6).shuffled())
            }

            return globalPatternBag.first()
        }
    }

    private class TntSequenceTask(
        private val source: CommandSourceStack,
        override val boss: RabbitBossEntity,
        private val queue: MutableList<TntAreaPosition>,
        private val explosionDelayTicks: Int,
        initialSpawnDelayTicks: Int,
        private val spawnIntervalTicks: Int,
        private val batchSize: Int
    ) : SkillTask {
        private var ticksUntilNextSpawn = initialSpawnDelayTicks

        override fun tick(): Boolean {
            if (!boss.isAlive) {
                return true
            }

            if (ticksUntilNextSpawn > 0) {
                ticksUntilNextSpawn--
                return false
            }

            var spawned = 0
            repeat(batchSize) {
                val nextIndex = queue.indexOfFirst { it !in activeTntAreas }
                if (nextIndex == -1) {
                    return@repeat
                }

                val nextArea = queue[nextIndex]
                if (spawnTntArea(source, boss, nextArea, explosionDelayTicks)) {
                    queue.removeAt(nextIndex)
                    spawned++
                }
            }

            if (queue.isEmpty()) {
                return true
            }

            if (spawned == 0) {
                ticksUntilNextSpawn = TNT_SPAWN_RETRY_TICKS
            } else {
                ticksUntilNextSpawn = spawnIntervalTicks
            }

            return false
        }
    }

    private class TntAreaTask(
        override val boss: RabbitBossEntity,
        private val tnt: TntEntity,
        private val tntArea: TntAreaPosition,
        private val explosionDelayTicks: Int
    ) : SkillTask {
        private var ticks = 0

        override fun tick(): Boolean {
            if (!tnt.isAlive) {
                activeTntAreas.remove(tntArea)
                return true
            }

            val level = tnt.level()

            ticks++
            if (ticks >= explosionDelayTicks) {
                if (level is ServerLevel) {
                    explodeArea(level)
                }
                tnt.discard()
                activeTntAreas.remove(tntArea)
                return true
            }

            return false
        }

        private fun explodeArea(level: ServerLevel) {
            val forward = horizontalDirection(tntArea.yaw)
            val right = Vec3(forward.z, 0.0, -forward.x).normalize()

            level.playSound(
                null,
                tntArea.position.x,
                tntArea.position.y,
                tntArea.position.z,
                SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.HOSTILE,
                4.0f,
                0.8f
            )

            for (front in -TNT_AREA_FORWARD_RANGE..TNT_AREA_FORWARD_RANGE) {
                for (side in -TNT_AREA_SIDE_RANGE..TNT_AREA_SIDE_RANGE) {
                    val pos = tntArea.position
                        .add(forward.scale(front.toDouble()))
                        .add(right.scale(side.toDouble()))
                        .add(0.0, 0.4, 0.0)
                    if ((front + TNT_AREA_FORWARD_RANGE) % 3 == 0 && side == 0) {
                        sendForcedParticles(level, ParticleTypes.EXPLOSION_EMITTER, pos, 1, 0.2f, 0.2f, 0.2f, 0.0f)
                    }
                    sendForcedParticles(level, ParticleTypes.EXPLOSION, pos, 3, 0.45f, 0.25f, 0.45f, 0.0f)
                    sendForcedParticles(level, ParticleTypes.FLAME, pos, 6, 0.35f, 0.08f, 0.35f, 0.04f)
                }
            }

            damagePlayers(level, forward, right)
        }

        private fun damagePlayers(level: ServerLevel, forward: Vec3, right: Vec3) {
            level.players().forEach { player ->
                val offset = player.position().subtract(tntArea.position)
                val forwardOffset = offset.dot(forward)
                val sideOffset = offset.dot(right)
                val verticalOffset = player.y - tntArea.position.y

                if (
                    kotlin.math.abs(forwardOffset) <= TNT_AREA_FORWARD_RANGE + 0.75 &&
                    kotlin.math.abs(sideOffset) <= TNT_AREA_SIDE_RANGE + 0.75 &&
                    verticalOffset >= -1.0 &&
                    verticalOffset <= 4.0
                ) {
                    player.hurt(level.damageSources().explosion(null, null), TNT_AREA_DAMAGE)
                }
            }
        }
    }

    private class GlobalTwoFreezeTask(
        override val boss: RabbitBossEntity,
        private val area: AreaPosition
    ) : SkillTask {
        private val previousPositions = mutableMapOf<UUID, Vec3>()
        private var ticks = 0

        override fun tick(): Boolean {
            val level = boss.level()
            if (!boss.isAlive || level !is ServerLevel) {
                return true
            }

            ticks++
            val forward = horizontalDirection(area.yaw)
            val right = Vec3(forward.z, 0.0, -forward.x).normalize()
            val playersInArea = level.players().filter { player -> isInsideArea(player.position(), area, forward, right) }
            val currentIds = playersInArea.mapTo(mutableSetOf()) { it.uuid }

            playersInArea.forEach { player ->
                val previous = previousPositions[player.uuid]
                val current = player.position()
                if (previous != null && current.distanceToSqr(previous) > GLOBAL_TWO_MOVE_THRESHOLD_SQR) {
                    player.hurt(level.damageSources().generic(), TNT_AREA_DAMAGE)
                }
                previousPositions[player.uuid] = current
            }

            previousPositions.keys.removeIf { it !in currentIds }
            return ticks >= GLOBAL_TWO_FREEZE_DURATION_TICKS
        }

    }

    private class SkillOneTask(
        override val boss: RabbitBossEntity,
        private val spawnPosition: Vec3,
        private val spawnYaw: Float
    ) : SkillTask {
        private val originalYaw = spawnYaw
        private val right = rightDirection(originalYaw)
        private var phase = Phase.MOVE_TO_SIDE
        private var moveStart = boss.position()
        private var moveTarget = sideTarget()
        private var moveYaw = yawFromDirection(moveTarget.subtract(moveStart), originalYaw)
        private var totalMoveTicks = moveTicks(moveStart, moveTarget)
        private var ticks = 0

        init {
            boss.playAnimation("walk")
            face(moveYaw)
        }

        override fun tick(): Boolean {
            if (!boss.isAlive) {
                return true
            }

            when (phase) {
                Phase.MOVE_TO_SIDE -> {
                    if (tickMove()) {
                        boss.playAnimation("idle")
                        phase = Phase.STOP_AT_SIDE
                        ticks = 0
                    }
                }
                Phase.STOP_AT_SIDE -> {
                    face(moveYaw)
                    ticks++
                    if (ticks >= STOP_DELAY_TICKS) {
                        face(originalYaw)
                        phase = Phase.FACE_FRONT
                        ticks = 0
                    }
                }
                Phase.FACE_FRONT -> {
                    face(originalYaw)
                    ticks++
                    if (ticks >= FACE_DELAY_TICKS) {
                        boss.setAnimationSpeed(2.0f)
                        if (boss.level() is ServerLevel) {
                            val castPos = boss.position().add(0.0, 2.2, 0.0)
                            playHostileSound(boss.level(), castPos, SoundEvents.BLAZE_SHOOT, 1.8f, 0.35f)
                            sendForcedParticles(boss.level() as ServerLevel, ParticleTypes.CRIT, castPos, 55, 1.0f, 0.6f, 1.0f, 0.14f)
                            sendForcedParticles(boss.level() as ServerLevel, ParticleTypes.CLOUD, castPos, 35, 1.2f, 0.35f, 1.2f, 0.04f)
                        }
                        boss.playAnimation("trow")
                        phase = Phase.THROW_WAIT
                        ticks = 0
                    }
                }
                Phase.THROW_WAIT -> {
                    face(originalYaw)
                    ticks++
                    if (ticks >= THROW_DELAY_TICKS) {
                        spawnLaunchedPipe(boss)
                        beginReturnToCenter()
                    }
                }
                Phase.RETURN_CENTER -> {
                    if (tickMove()) {
                        face(originalYaw)
                        boss.playAnimation("idle")
                        return true
                    }
                }
            }

            return false
        }

        private fun tickMove(): Boolean {
            if (ticks >= totalMoveTicks) {
                placeAt(moveTarget, moveYaw)
                return true
            }

            val progress = ((ticks + 1).toDouble() / totalMoveTicks).coerceAtMost(1.0)
            val next = moveStart.lerp(moveTarget, progress)
            placeAt(next, moveYaw)
            ticks++
            return ticks >= totalMoveTicks
        }

        private fun beginReturnToCenter() {
            moveStart = boss.position()
            moveTarget = Vec3(spawnPosition.x, boss.y, spawnPosition.z)
            moveYaw = yawFromDirection(moveTarget.subtract(moveStart), originalYaw)
            totalMoveTicks = moveTicks(moveStart, moveTarget)
            phase = Phase.RETURN_CENTER
            ticks = 0
            boss.setAnimationSpeed(1.0f)
            boss.playAnimation("walk")
            face(moveYaw)
        }

        private fun sideTarget(): Vec3 {
            val currentOffset = boss.position().subtract(spawnPosition).dot(right)
            val targetOffset = randomTargetOffset(currentOffset)
            return Vec3(
                spawnPosition.x + right.x * targetOffset,
                boss.y,
                spawnPosition.z + right.z * targetOffset
            )
        }

        private fun placeAt(pos: Vec3, yaw: Float) {
            boss.moveTo(pos.x, pos.y, pos.z, yaw, 0.0f)
            face(yaw)
        }

        private fun face(yaw: Float) {
            boss.yRot = yaw
            boss.yHeadRot = yaw
            boss.yBodyRot = yaw
        }

        companion object {
            private const val WALK_SPEED = 0.06
            private const val STOP_DELAY_TICKS = 20
            private const val FACE_DELAY_TICKS = 20
            private const val THROW_DELAY_TICKS = 50

            private fun randomTargetOffset(currentOffset: Double): Double {
                repeat(8) {
                    val value = Random.nextDouble(-5.0, 5.0)
                    if (kotlin.math.abs(value - currentOffset) >= 0.75) {
                        return value
                    }
                }

                return if (currentOffset <= 0.0) 5.0 else -5.0
            }

            private fun moveTicks(from: Vec3, to: Vec3): Int =
                kotlin.math.max(1, kotlin.math.ceil(to.subtract(from).horizontalDistance() / WALK_SPEED).toInt())

            private fun rightDirection(yaw: Float): Vec3 {
                val forward = horizontalDirection(yaw)
                return Vec3(forward.z, 0.0, -forward.x).normalize()
            }

            private fun yawFromDirection(direction: Vec3, fallback: Float): Float {
                if (direction.horizontalDistanceSqr() < 1.0E-6) {
                    return fallback
                }

                return Math.toDegrees(kotlin.math.atan2(-direction.x, direction.z)).toFloat()
            }
        }

        private enum class Phase {
            MOVE_TO_SIDE,
            STOP_AT_SIDE,
            FACE_FRONT,
            THROW_WAIT,
            RETURN_CENTER
        }
    }
}
