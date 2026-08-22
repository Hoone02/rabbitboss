package org.example.hoon.rabbitboss.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import org.example.hoon.rabbitboss.client.hud.RabbitBossLifeHud
import org.example.hoon.rabbitboss.client.render.BoomRenderer
import org.example.hoon.rabbitboss.client.render.CanonRenderer
import org.example.hoon.rabbitboss.client.render.FoxExplodeRenderer
import org.example.hoon.rabbitboss.client.render.GlobalTwoIndicatorRenderer
import org.example.hoon.rabbitboss.client.render.MissileIndicatorRenderer
import org.example.hoon.rabbitboss.client.render.MissileLauncherRenderer
import org.example.hoon.rabbitboss.client.render.MissileRenderer
import org.example.hoon.rabbitboss.client.render.PipeRenderer
import org.example.hoon.rabbitboss.client.render.RabbitBossRenderer
import org.example.hoon.rabbitboss.client.render.RazerRenderer
import org.example.hoon.rabbitboss.client.render.TntIndicatorRenderer
import org.example.hoon.rabbitboss.client.render.TntRenderer
import org.example.hoon.rabbitboss.entity.ModEntities
import org.example.hoon.rabbitboss.network.LifeHudPayload

class RabbitbossClient : ClientModInitializer {

    override fun onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.RABBIT_BOSS, ::RabbitBossRenderer)
        EntityRendererRegistry.register(ModEntities.PIPE, ::PipeRenderer)
        EntityRendererRegistry.register(ModEntities.CANON, ::CanonRenderer)
        EntityRendererRegistry.register(ModEntities.BOOM, ::BoomRenderer)
        EntityRendererRegistry.register(ModEntities.TNT, ::TntRenderer)
        EntityRendererRegistry.register(ModEntities.TNT_INDICATOR, ::TntIndicatorRenderer)
        EntityRendererRegistry.register(ModEntities.MISSILE_LAUNCHER, ::MissileLauncherRenderer)
        EntityRendererRegistry.register(ModEntities.MISSILE, ::MissileRenderer)
        EntityRendererRegistry.register(ModEntities.MISSILE_INDICATOR, ::MissileIndicatorRenderer)
        EntityRendererRegistry.register(ModEntities.GLOBAL_TWO_INDICATOR, ::GlobalTwoIndicatorRenderer)
        EntityRendererRegistry.register(ModEntities.RAZER, ::RazerRenderer)
        EntityRendererRegistry.register(ModEntities.FOX_EXPLODE, ::FoxExplodeRenderer)
        RabbitBossLifeHud.register()
        ClientPlayNetworking.registerGlobalReceiver(LifeHudPayload.TYPE) { payload, context ->
            context.client().execute {
                RabbitBossLifeHud.update(payload.lives, payload.maxLives, payload.visible)
            }
        }
    }
}
