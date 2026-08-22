package org.example.hoon.rabbitboss.client.render

import net.minecraft.resources.ResourceLocation
import org.example.hoon.rabbitboss.Rabbitboss
import org.example.hoon.rabbitboss.entity.MissileLauncherEntity
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoRenderer

class MissileLauncherModel : GeoModel<MissileLauncherEntity>() {
    override fun getModelResource(
        animatable: MissileLauncherEntity,
        renderer: GeoRenderer<MissileLauncherEntity>?
    ): ResourceLocation = Rabbitboss.id("geo/missilelauncher.geo.json")

    override fun getTextureResource(
        animatable: MissileLauncherEntity,
        renderer: GeoRenderer<MissileLauncherEntity>?
    ): ResourceLocation = Rabbitboss.id("textures/entity/missilelauncher.png")

    override fun getAnimationResource(animatable: MissileLauncherEntity): ResourceLocation =
        Rabbitboss.id("animations/missilelauncher.animation.json")
}
