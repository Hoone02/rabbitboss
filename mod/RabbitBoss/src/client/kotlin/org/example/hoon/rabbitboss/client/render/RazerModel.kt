package org.example.hoon.rabbitboss.client.render

import net.minecraft.resources.ResourceLocation
import org.example.hoon.rabbitboss.Rabbitboss
import org.example.hoon.rabbitboss.entity.RazerEntity
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoRenderer

class RazerModel : GeoModel<RazerEntity>() {
    override fun getModelResource(
        animatable: RazerEntity,
        renderer: GeoRenderer<RazerEntity>?
    ): ResourceLocation = Rabbitboss.id("geo/razer.geo.json")

    override fun getTextureResource(
        animatable: RazerEntity,
        renderer: GeoRenderer<RazerEntity>?
    ): ResourceLocation = Rabbitboss.id("textures/entity/razer.png")

    override fun getAnimationResource(animatable: RazerEntity): ResourceLocation =
        Rabbitboss.id("animations/razer.animation.json")
}
