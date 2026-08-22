package org.example.hoon.rabbitboss.client.render

import net.minecraft.resources.Identifier
import org.example.hoon.rabbitboss.Rabbitboss
import org.example.hoon.rabbitboss.entity.RabbitBossEntity
import com.geckolib.model.GeoModel
import com.geckolib.renderer.base.GeoRenderState

class RabbitBossModel : GeoModel<RabbitBossEntity>() {
    override fun getModelResource(renderState: GeoRenderState): Identifier =
        Rabbitboss.id("geo/rabbit_boss.geo.json")

    override fun getTextureResource(renderState: GeoRenderState): Identifier =
        Rabbitboss.id("textures/entity/rabbit_boss.png")

    override fun getAnimationResource(animatable: RabbitBossEntity): Identifier =
        Rabbitboss.id("animations/rabbit_boss.animation.json")
}
