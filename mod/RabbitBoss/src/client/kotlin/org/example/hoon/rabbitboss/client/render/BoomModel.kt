package org.example.hoon.rabbitboss.client.render

import net.minecraft.resources.ResourceLocation
import org.example.hoon.rabbitboss.Rabbitboss
import org.example.hoon.rabbitboss.entity.BoomEntity
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoRenderer

class BoomModel : GeoModel<BoomEntity>() {
    override fun getModelResource(
        animatable: BoomEntity,
        renderer: GeoRenderer<BoomEntity>?
    ): ResourceLocation =
        Rabbitboss.id("geo/boom.geo.json")

    override fun getTextureResource(
        animatable: BoomEntity,
        renderer: GeoRenderer<BoomEntity>?
    ): ResourceLocation =
        Rabbitboss.id("textures/entity/boom.png")

    override fun getAnimationResource(animatable: BoomEntity): ResourceLocation =
        Rabbitboss.id("animations/boom.animation.json")
}
