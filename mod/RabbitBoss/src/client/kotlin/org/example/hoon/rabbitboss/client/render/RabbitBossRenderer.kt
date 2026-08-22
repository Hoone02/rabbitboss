package org.example.hoon.rabbitboss.client.render

import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation
import org.example.hoon.rabbitboss.entity.RabbitBossEntity
import software.bernie.geckolib.renderer.GeoEntityRenderer

class RabbitBossRenderer(context: EntityRendererProvider.Context) :
    GeoEntityRenderer<RabbitBossEntity>(context, RabbitBossModel()) {

    init {
        withScale(5.0f)
        shadowRadius = 3.5f
    }

    override fun getRenderType(
        animatable: RabbitBossEntity,
        texture: ResourceLocation,
        bufferSource: MultiBufferSource?,
        partialTick: Float
    ): RenderType = RenderType.entityTranslucent(texture)

    override fun shouldRender(
        animatable: RabbitBossEntity,
        frustum: Frustum,
        x: Double,
        y: Double,
        z: Double
    ): Boolean = true
}
