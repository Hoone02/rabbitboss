package org.example.hoon.rabbitboss.client.render

import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRendererProvider
import org.example.hoon.rabbitboss.entity.TntEntity
import software.bernie.geckolib.renderer.GeoEntityRenderer

class TntRenderer(context: EntityRendererProvider.Context) :
    GeoEntityRenderer<TntEntity>(context, TntModel()) {

    init {
        withScale(3.0f)
        shadowRadius = 3.0f
    }

    override fun shouldRender(
        animatable: TntEntity,
        frustum: Frustum,
        x: Double,
        y: Double,
        z: Double
    ): Boolean = true
}
