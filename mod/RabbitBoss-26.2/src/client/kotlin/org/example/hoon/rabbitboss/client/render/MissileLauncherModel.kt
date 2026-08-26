package org.example.hoon.rabbitboss.client.render

import net.minecraft.resources.Identifier
import org.example.hoon.rabbitboss.Rabbitboss
import org.example.hoon.rabbitboss.entity.MissileLauncherEntity
import com.geckolib.model.GeoModel
import com.geckolib.renderer.base.GeoRenderState

class MissileLauncherModel : GeoModel<MissileLauncherEntity>() {
    override fun getModelResource(renderState: GeoRenderState): Identifier = Rabbitboss.id("missilelauncher")

    override fun getTextureResource(renderState: GeoRenderState): Identifier = Rabbitboss.id("textures/entity/missilelauncher.png")

    override fun getAnimationResource(animatable: MissileLauncherEntity): Identifier =
        Rabbitboss.id("missilelauncher")
}
