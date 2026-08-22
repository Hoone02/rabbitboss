package org.example.hoon.rabbitboss.client.render

import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRendererProvider
import org.example.hoon.rabbitboss.entity.BoomEntity
import software.bernie.geckolib.renderer.GeoEntityRenderer

class BoomRenderer(context: EntityRendererProvider.Context) :
    GeoEntityRenderer<BoomEntity>(context, BoomModel()) {

    init {
        withScale(5.0f)
        shadowRadius = 2.5f
    }

    override fun shouldRender(
        animatable: BoomEntity,
        frustum: Frustum,
        x: Double,
        y: Double,
        z: Double
    ): Boolean = true
}
