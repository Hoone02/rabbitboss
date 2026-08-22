package org.example.hoon.rabbitboss

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.resources.ResourceLocation
import org.example.hoon.rabbitboss.command.RabbitBossCommands
import org.example.hoon.rabbitboss.entity.BoomEntity
import org.example.hoon.rabbitboss.entity.CanonEntity
import org.example.hoon.rabbitboss.entity.FoxExplodeEntity
import org.example.hoon.rabbitboss.entity.MissileEntity
import org.example.hoon.rabbitboss.entity.MissileLauncherEntity
import org.example.hoon.rabbitboss.entity.ModEntities
import org.example.hoon.rabbitboss.entity.PipeEntity
import org.example.hoon.rabbitboss.entity.RabbitBossEntity
import org.example.hoon.rabbitboss.entity.RazerEntity
import org.example.hoon.rabbitboss.entity.TntEntity
import org.example.hoon.rabbitboss.network.LifeHudPayload

class Rabbitboss : ModInitializer {

    override fun onInitialize() {
        PayloadTypeRegistry.playS2C().register(LifeHudPayload.TYPE, LifeHudPayload.CODEC)
        ModEntities.register()
        FabricDefaultAttributeRegistry.register(ModEntities.RABBIT_BOSS, RabbitBossEntity.createAttributes())
        FabricDefaultAttributeRegistry.register(ModEntities.PIPE, PipeEntity.createAttributes())
        FabricDefaultAttributeRegistry.register(ModEntities.CANON, CanonEntity.createAttributes())
        FabricDefaultAttributeRegistry.register(ModEntities.BOOM, BoomEntity.createAttributes())
        FabricDefaultAttributeRegistry.register(ModEntities.TNT, TntEntity.createAttributes())
        FabricDefaultAttributeRegistry.register(ModEntities.MISSILE_LAUNCHER, MissileLauncherEntity.createAttributes())
        FabricDefaultAttributeRegistry.register(ModEntities.MISSILE, MissileEntity.createAttributes())
        FabricDefaultAttributeRegistry.register(ModEntities.RAZER, RazerEntity.createAttributes())
        FabricDefaultAttributeRegistry.register(ModEntities.FOX_EXPLODE, FoxExplodeEntity.createAttributes())
        RabbitBossCommands.register()
    }

    companion object {
        const val MOD_ID = "rabbitboss"

        fun id(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
    }
}
