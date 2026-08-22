package org.example.hoon.rabbitboss.client.render

import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRendererProvider
import org.example.hoon.rabbitboss.entity.FoxExplodeEntity
import software.bernie.geckolib.renderer.GeoEntityRenderer

class FoxExplodeRenderer(context: EntityRendererProvider.Context) :
    GeoEntityRenderer<FoxExplodeEntity>(context, FoxExplodeModel()) {

    init {
        withScale(5.0f)
        shadowRadius = 3.0f
    }

    override fun shouldRender(
        animatable: FoxExplodeEntity,
        frustum: Frustum,
        x: Double,
        y: Double,
        z: Double
    ): Boolean = true
}
