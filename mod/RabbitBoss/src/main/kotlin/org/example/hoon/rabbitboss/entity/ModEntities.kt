package org.example.hoon.rabbitboss.entity

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.item.Item
import net.minecraft.world.item.SpawnEggItem
import net.minecraft.core.Registry
import org.example.hoon.rabbitboss.Rabbitboss

object ModEntities {
    private val RABBIT_BOSS_KEY: ResourceKey<EntityType<*>> =
        ResourceKey.create(Registries.ENTITY_TYPE, Rabbitboss.id("rabbit_boss"))
    private val RABBIT_BOSS_EGG_KEY: ResourceKey<Item> =
        ResourceKey.create(Registries.ITEM, Rabbitboss.id("rabbit_boss_spawn_egg"))
    private val PIPE_KEY: ResourceKey<EntityType<*>> =
        ResourceKey.create(Registries.ENTITY_TYPE, Rabbitboss.id("pipe"))
    private val CANON_KEY: ResourceKey<EntityType<*>> =
        ResourceKey.create(Registries.ENTITY_TYPE, Rabbitboss.id("canon"))
    private val BOOM_KEY: ResourceKey<EntityType<*>> =
        ResourceKey.create(Registries.ENTITY_TYPE, Rabbitboss.id("boom"))
    private val TNT_KEY: ResourceKey<EntityType<*>> =
        ResourceKey.create(Registries.ENTITY_TYPE, Rabbitboss.id("tnt"))
    private val TNT_INDICATOR_KEY: ResourceKey<EntityType<*>> =
        ResourceKey.create(Registries.ENTITY_TYPE, Rabbitboss.id("tnt_indicator"))
    private val MISSILE_LAUNCHER_KEY: ResourceKey<EntityType<*>> =
        ResourceKey.create(Registries.ENTITY_TYPE, Rabbitboss.id("missile_launcher"))
    private val MISSILE_KEY: ResourceKey<EntityType<*>> =
        ResourceKey.create(Registries.ENTITY_TYPE, Rabbitboss.id("missile"))
    private val MISSILE_INDICATOR_KEY: ResourceKey<EntityType<*>> =
        ResourceKey.create(Registries.ENTITY_TYPE, Rabbitboss.id("missile_indicator"))
    private val GLOBAL_TWO_INDICATOR_KEY: ResourceKey<EntityType<*>> =
        ResourceKey.create(Registries.ENTITY_TYPE, Rabbitboss.id("global_two_indicator"))
    private val RAZER_KEY: ResourceKey<EntityType<*>> =
        ResourceKey.create(Registries.ENTITY_TYPE, Rabbitboss.id("razer"))
    private val FOX_EXPLODE_KEY: ResourceKey<EntityType<*>> =
        ResourceKey.create(Registries.ENTITY_TYPE, Rabbitboss.id("foxexplode"))

    val RABBIT_BOSS: EntityType<RabbitBossEntity> =
        Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Rabbitboss.id("rabbit_boss"),
            EntityType.Builder.of(::RabbitBossEntity, MobCategory.MONSTER)
                .sized(7.0f, 12.0f)
                .clientTrackingRange(256)
                .build(RABBIT_BOSS_KEY)
        )

    val RABBIT_BOSS_SPAWN_EGG: Item =
        Registry.register(
            BuiltInRegistries.ITEM,
            Rabbitboss.id("rabbit_boss_spawn_egg"),
            SpawnEggItem(
                RABBIT_BOSS,
                Item.Properties().setId(RABBIT_BOSS_EGG_KEY)
            )
        )

    val PIPE: EntityType<PipeEntity> =
        Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Rabbitboss.id("pipe"),
            EntityType.Builder.of(::PipeEntity, MobCategory.MISC)
                .sized(PipeEntity.LENGTH, PipeEntity.HEIGHT)
                .clientTrackingRange(256)
                .build(PIPE_KEY)
        )

    val CANON: EntityType<CanonEntity> =
        Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Rabbitboss.id("canon"),
            EntityType.Builder.of(::CanonEntity, MobCategory.MISC)
                .sized(3.0f, 3.5f)
                .clientTrackingRange(256)
                .build(CANON_KEY)
        )

    val BOOM: EntityType<BoomEntity> =
        Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Rabbitboss.id("boom"),
            EntityType.Builder.of(::BoomEntity, MobCategory.MISC)
                .sized(2.0f, 2.5f)
                .clientTrackingRange(256)
                .build(BOOM_KEY)
        )

    val TNT: EntityType<TntEntity> =
        Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Rabbitboss.id("tnt"),
            EntityType.Builder.of(::TntEntity, MobCategory.MISC)
                .sized(3.0f, 3.0f)
                .clientTrackingRange(256)
                .build(TNT_KEY)
        )

    val TNT_INDICATOR: EntityType<TntIndicatorEntity> =
        Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Rabbitboss.id("tnt_indicator"),
            EntityType.Builder.of(::TntIndicatorEntity, MobCategory.MISC)
                .sized(5.0f, 0.1f)
                .clientTrackingRange(256)
                .build(TNT_INDICATOR_KEY)
        )

    val MISSILE_LAUNCHER: EntityType<MissileLauncherEntity> =
        Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Rabbitboss.id("missile_launcher"),
            EntityType.Builder.of(::MissileLauncherEntity, MobCategory.MISC)
                .sized(5.0f, 5.0f)
                .clientTrackingRange(256)
                .build(MISSILE_LAUNCHER_KEY)
        )

    val MISSILE: EntityType<MissileEntity> =
        Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Rabbitboss.id("missile"),
            EntityType.Builder.of(::MissileEntity, MobCategory.MISC)
                .sized(2.0f, 2.0f)
                .clientTrackingRange(256)
                .build(MISSILE_KEY)
        )

    val MISSILE_INDICATOR: EntityType<MissileIndicatorEntity> =
        Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Rabbitboss.id("missile_indicator"),
            EntityType.Builder.of(::MissileIndicatorEntity, MobCategory.MISC)
                .sized(5.0f, 0.1f)
                .clientTrackingRange(256)
                .build(MISSILE_INDICATOR_KEY)
        )

    val GLOBAL_TWO_INDICATOR: EntityType<GlobalTwoIndicatorEntity> =
        Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Rabbitboss.id("global_two_indicator"),
            EntityType.Builder.of(::GlobalTwoIndicatorEntity, MobCategory.MISC)
                .sized(25.0f, 0.1f)
                .clientTrackingRange(256)
                .build(GLOBAL_TWO_INDICATOR_KEY)
        )

    val RAZER: EntityType<RazerEntity> =
        Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Rabbitboss.id("razer"),
            EntityType.Builder.of(::RazerEntity, MobCategory.MISC)
                .sized(RazerEntity.LENGTH, RazerEntity.HEIGHT)
                .clientTrackingRange(256)
                .build(RAZER_KEY)
        )

    val FOX_EXPLODE: EntityType<FoxExplodeEntity> =
        Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Rabbitboss.id("foxexplode"),
            EntityType.Builder.of(::FoxExplodeEntity, MobCategory.MISC)
                .sized(5.0f, 5.0f)
                .clientTrackingRange(256)
                .build(FOX_EXPLODE_KEY)
        )

    fun register() {
    }
}
