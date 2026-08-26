package org.example.hoon.rabbitboss.client.render

import net.minecraft.resources.Identifier
import org.example.hoon.rabbitboss.Rabbitboss
import org.example.hoon.rabbitboss.entity.RazerEntity
import com.geckolib.model.GeoModel
import com.geckolib.renderer.base.GeoRenderState

class RazerModel : GeoModel<RazerEntity>() {
    override fun getModelResource(renderState: GeoRenderState): Identifier = Rabbitboss.id("razer")

    override fun getTextureResource(renderState: GeoRenderState): Identifier = Rabbitboss.id("textures/entity/razer.png")

    override fun getAnimationResource(animatable: RazerEntity): Identifier =
        Rabbitboss.id("razer")
}
