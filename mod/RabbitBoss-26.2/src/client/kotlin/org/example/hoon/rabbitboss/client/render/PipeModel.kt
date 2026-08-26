package org.example.hoon.rabbitboss.client.render

import net.minecraft.resources.Identifier
import org.example.hoon.rabbitboss.Rabbitboss
import org.example.hoon.rabbitboss.entity.PipeEntity
import com.geckolib.model.GeoModel
import com.geckolib.renderer.base.GeoRenderState

class PipeModel : GeoModel<PipeEntity>() {
    override fun getModelResource(renderState: GeoRenderState): Identifier =
        Rabbitboss.id("pipe")

    override fun getTextureResource(renderState: GeoRenderState): Identifier =
        Rabbitboss.id("textures/entity/rabbit_boss.png")

    override fun getAnimationResource(animatable: PipeEntity): Identifier =
        Rabbitboss.id("pipe")
}
