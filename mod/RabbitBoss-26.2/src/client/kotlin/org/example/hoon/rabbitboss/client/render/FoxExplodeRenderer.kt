package org.example.hoon.rabbitboss.client.render

import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import org.example.hoon.rabbitboss.entity.FoxExplodeEntity
import com.geckolib.renderer.GeoEntityRenderer

class FoxExplodeRenderer(context: EntityRendererProvider.Context) :
    GeoEntityRenderer<FoxExplodeEntity, EntityRenderState>(context, FoxExplodeModel()) {

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
