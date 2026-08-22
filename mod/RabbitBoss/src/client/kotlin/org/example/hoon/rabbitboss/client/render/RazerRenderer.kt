package org.example.hoon.rabbitboss.client.render

import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation
import org.example.hoon.rabbitboss.entity.RazerEntity
import software.bernie.geckolib.renderer.GeoEntityRenderer

class RazerRenderer(context: EntityRendererProvider.Context) :
    GeoEntityRenderer<RazerEntity>(context, RazerModel()) {

    init {
        withScale(25.0f)
        shadowRadius = 0.0f
    }

    override fun getRenderType(
        animatable: RazerEntity,
        texture: ResourceLocation,
        bufferSource: MultiBufferSource?,
        partialTick: Float
    ): RenderType = RenderType.entityTranslucent(texture)

    override fun shouldRender(
        animatable: RazerEntity,
        frustum: Frustum,
        x: Double,
        y: Double,
        z: Double
    ): Boolean = true
}
