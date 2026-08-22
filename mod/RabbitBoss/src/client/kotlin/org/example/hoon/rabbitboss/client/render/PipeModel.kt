package org.example.hoon.rabbitboss.client.render

import net.minecraft.resources.ResourceLocation
import org.example.hoon.rabbitboss.Rabbitboss
import org.example.hoon.rabbitboss.entity.PipeEntity
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoRenderer

class PipeModel : GeoModel<PipeEntity>() {
    override fun getModelResource(
        animatable: PipeEntity,
        renderer: GeoRenderer<PipeEntity>?
    ): ResourceLocation =
        Rabbitboss.id("geo/pipe.geo.json")

    override fun getTextureResource(
        animatable: PipeEntity,
        renderer: GeoRenderer<PipeEntity>?
    ): ResourceLocation =
        Rabbitboss.id("textures/entity/rabbit_boss.png")

    override fun getAnimationResource(animatable: PipeEntity): ResourceLocation =
        Rabbitboss.id("animations/pipe.animation.json")
}
