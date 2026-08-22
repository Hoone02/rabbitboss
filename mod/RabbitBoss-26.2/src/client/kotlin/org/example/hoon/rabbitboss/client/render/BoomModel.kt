package org.example.hoon.rabbitboss.client.render

import net.minecraft.resources.Identifier
import org.example.hoon.rabbitboss.Rabbitboss
import org.example.hoon.rabbitboss.entity.BoomEntity
import com.geckolib.model.GeoModel
import com.geckolib.renderer.base.GeoRenderState

class BoomModel : GeoModel<BoomEntity>() {
    override fun getModelResource(renderState: GeoRenderState): Identifier =
        Rabbitboss.id("geo/boom.geo.json")

    override fun getTextureResource(renderState: GeoRenderState): Identifier =
        Rabbitboss.id("textures/entity/boom.png")

    override fun getAnimationResource(animatable: BoomEntity): Identifier =
        Rabbitboss.id("animations/boom.animation.json")
}
