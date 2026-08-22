package org.example.hoon.rabbitboss.client.render

import net.minecraft.resources.ResourceLocation
import org.example.hoon.rabbitboss.Rabbitboss
import org.example.hoon.rabbitboss.entity.CanonEntity
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoRenderer

class CanonModel : GeoModel<CanonEntity>() {
    override fun getModelResource(
        animatable: CanonEntity,
        renderer: GeoRenderer<CanonEntity>?
    ): ResourceLocation =
        Rabbitboss.id("geo/canon.geo.json")

    override fun getTextureResource(
        animatable: CanonEntity,
        renderer: GeoRenderer<CanonEntity>?
    ): ResourceLocation =
        Rabbitboss.id("textures/entity/canon.png")

    override fun getAnimationResource(animatable: CanonEntity): ResourceLocation =
        Rabbitboss.id("animations/canon.animation.json")
}
