package org.example.hoon.rabbitboss.client.render

import net.minecraft.resources.Identifier
import org.example.hoon.rabbitboss.Rabbitboss
import org.example.hoon.rabbitboss.entity.TntEntity
import com.geckolib.model.GeoModel
import com.geckolib.renderer.base.GeoRenderState

class TntModel : GeoModel<TntEntity>() {
    override fun getModelResource(renderState: GeoRenderState): Identifier =
        Rabbitboss.id("geo/tnt.geo.json")

    override fun getTextureResource(renderState: GeoRenderState): Identifier =
        Rabbitboss.id("textures/entity/tnt.png")

    override fun getAnimationResource(animatable: TntEntity): Identifier =
        Rabbitboss.id("animations/tnt.animation.json")
}
