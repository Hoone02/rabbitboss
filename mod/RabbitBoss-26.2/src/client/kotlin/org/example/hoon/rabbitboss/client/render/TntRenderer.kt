package org.example.hoon.rabbitboss.client.render

import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import org.example.hoon.rabbitboss.entity.TntEntity
import com.geckolib.renderer.GeoEntityRenderer

class TntRenderer(context: EntityRendererProvider.Context) :
    GeoEntityRenderer<TntEntity, EntityRenderState>(context, TntModel()) {

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
