package org.example.hoon.rabbitboss.client.render

import net.minecraft.resources.Identifier
import org.example.hoon.rabbitboss.Rabbitboss
import org.example.hoon.rabbitboss.entity.FoxExplodeEntity
import com.geckolib.model.GeoModel
import com.geckolib.renderer.base.GeoRenderState

class FoxExplodeModel : GeoModel<FoxExplodeEntity>() {
    override fun getModelResource(renderState: GeoRenderState): Identifier =
        Rabbitboss.id("geo/foxexplode.geo.json")

    override fun getTextureResource(renderState: GeoRenderState): Identifier =
        Rabbitboss.id("textures/entity/anj_fox_full.png")

    override fun getAnimationResource(animatable: FoxExplodeEntity): Identifier =
        Rabbitboss.id("animations/foxexplode.animation.json")
}
