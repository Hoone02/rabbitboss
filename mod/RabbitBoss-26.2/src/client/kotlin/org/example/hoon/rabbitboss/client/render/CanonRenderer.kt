package org.example.hoon.rabbitboss.client.render

import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import org.example.hoon.rabbitboss.entity.CanonEntity
import com.geckolib.renderer.GeoEntityRenderer

class CanonRenderer(context: EntityRendererProvider.Context) :
    GeoEntityRenderer<CanonEntity, EntityRenderState>(context, CanonModel()) {

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
