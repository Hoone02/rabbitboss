package org.example.hoon.rabbitboss.client.render

import net.minecraft.resources.ResourceLocation
import org.example.hoon.rabbitboss.Rabbitboss
import org.example.hoon.rabbitboss.entity.MissileEntity
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoRenderer

class MissileModel : GeoModel<MissileEntity>() {
    override fun getModelResource(
        animatable: MissileEntity,
        renderer: GeoRenderer<MissileEntity>?
    ): ResourceLocation = Rabbitboss.id("geo/missile.geo.json")

    override fun getTextureResource(
        animatable: MissileEntity,
        renderer: GeoRenderer<MissileEntity>?
    ): ResourceLocation = Rabbitboss.id("textures/entity/missile.png")

    override fun getAnimationResource(animatable: MissileEntity): ResourceLocation =
        Rabbitboss.id("animations/missile.animation.json")
}
