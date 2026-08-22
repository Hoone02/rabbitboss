package org.example.hoon.rabbitboss.client.render

import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRendererProvider
import org.example.hoon.rabbitboss.entity.PipeEntity
import software.bernie.geckolib.renderer.GeoEntityRenderer

class PipeRenderer(context: EntityRendererProvider.Context) :
    GeoEntityRenderer<PipeEntity>(context, PipeModel()) {

    init {
        withScale(5.0f)
        shadowRadius = 5.0f
    }

    override fun shouldRender(
        animatable: PipeEntity,
        frustum: Frustum,
        x: Double,
        y: Double,
        z: Double
    ): Boolean = true
}
