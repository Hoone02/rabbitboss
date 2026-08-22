package org.example.hoon.rabbitboss.client.render

import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRendererProvider
import org.example.hoon.rabbitboss.entity.CanonEntity
import software.bernie.geckolib.renderer.GeoEntityRenderer

class CanonRenderer(context: EntityRendererProvider.Context) :
    GeoEntityRenderer<CanonEntity>(context, CanonModel()) {

    init {
        withScale(5.0f)
        shadowRadius = 5.0f
    }

    override fun shouldRender(
        animatable: CanonEntity,
        frustum: Frustum,
        x: Double,
        y: Double,
        z: Double
    ): Boolean = true
}
