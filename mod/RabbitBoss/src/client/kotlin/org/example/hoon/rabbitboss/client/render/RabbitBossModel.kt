package org.example.hoon.rabbitboss.client.render

import net.minecraft.resources.ResourceLocation
import org.example.hoon.rabbitboss.Rabbitboss
import org.example.hoon.rabbitboss.entity.RabbitBossEntity
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoRenderer

class RabbitBossModel : GeoModel<RabbitBossEntity>() {
    override fun getModelResource(
        animatable: RabbitBossEntity,
        renderer: GeoRenderer<RabbitBossEntity>?
    ): ResourceLocation =
        Rabbitboss.id("geo/rabbit_boss.geo.json")

    override fun getTextureResource(
        animatable: RabbitBossEntity,
        renderer: GeoRenderer<RabbitBossEntity>?
    ): ResourceLocation =
        Rabbitboss.id("textures/entity/rabbit_boss.png")

    override fun getAnimationResource(animatable: RabbitBossEntity): ResourceLocation =
        Rabbitboss.id("animations/rabbit_boss.animation.json")
}
