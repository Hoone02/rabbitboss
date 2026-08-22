package org.example.hoon.rabbitboss.client.render

import net.minecraft.resources.ResourceLocation
import org.example.hoon.rabbitboss.Rabbitboss
import org.example.hoon.rabbitboss.entity.FoxExplodeEntity
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoRenderer

class FoxExplodeModel : GeoModel<FoxExplodeEntity>() {
    override fun getModelResource(
        animatable: FoxExplodeEntity,
        renderer: GeoRenderer<FoxExplodeEntity>?
    ): ResourceLocation =
        Rabbitboss.id("geo/foxexplode.geo.json")

    override fun getTextureResource(
        animatable: FoxExplodeEntity,
        renderer: GeoRenderer<FoxExplodeEntity>?
    ): ResourceLocation =
        Rabbitboss.id("textures/entity/anj_fox_full.png")

    override fun getAnimationResource(animatable: FoxExplodeEntity): ResourceLocation =
        Rabbitboss.id("animations/foxexplode.animation.json")
}
