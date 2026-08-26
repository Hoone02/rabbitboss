package org.example.hoon.rabbitboss.client.render

import net.minecraft.resources.Identifier
import org.example.hoon.rabbitboss.Rabbitboss
import org.example.hoon.rabbitboss.entity.MissileEntity
import com.geckolib.model.GeoModel
import com.geckolib.renderer.base.GeoRenderState

class MissileModel : GeoModel<MissileEntity>() {
    override fun getModelResource(renderState: GeoRenderState): Identifier = Rabbitboss.id("missile")

    override fun getTextureResource(renderState: GeoRenderState): Identifier = Rabbitboss.id("textures/entity/missile.png")

    override fun getAnimationResource(animatable: MissileEntity): Identifier =
        Rabbitboss.id("missile")
}
