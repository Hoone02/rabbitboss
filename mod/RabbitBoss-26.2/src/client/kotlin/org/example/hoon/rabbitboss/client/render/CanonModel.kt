package org.example.hoon.rabbitboss.client.render

import net.minecraft.resources.Identifier
import org.example.hoon.rabbitboss.Rabbitboss
import org.example.hoon.rabbitboss.entity.CanonEntity
import com.geckolib.model.GeoModel
import com.geckolib.renderer.base.GeoRenderState

class CanonModel : GeoModel<CanonEntity>() {
    override fun getModelResource(renderState: GeoRenderState): Identifier =
        Rabbitboss.id("canon")

    override fun getTextureResource(renderState: GeoRenderState): Identifier =
        Rabbitboss.id("textures/entity/canon.png")

    override fun getAnimationResource(animatable: CanonEntity): Identifier =
        Rabbitboss.id("canon")
}
