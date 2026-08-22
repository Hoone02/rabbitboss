package org.example.hoon.rabbitboss.client.render

import net.minecraft.resources.ResourceLocation
import org.example.hoon.rabbitboss.Rabbitboss
import org.example.hoon.rabbitboss.entity.TntEntity
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoRenderer

class TntModel : GeoModel<TntEntity>() {
    override fun getModelResource(
        animatable: TntEntity,
        renderer: GeoRenderer<TntEntity>?
    ): ResourceLocation =
        Rabbitboss.id("geo/tnt.geo.json")

    override fun getTextureResource(
        animatable: TntEntity,
        renderer: GeoRenderer<TntEntity>?
    ): ResourceLocation =
        Rabbitboss.id("textures/entity/tnt.png")

    override fun getAnimationResource(animatable: TntEntity): ResourceLocation =
        Rabbitboss.id("animations/tnt.animation.json")
}
