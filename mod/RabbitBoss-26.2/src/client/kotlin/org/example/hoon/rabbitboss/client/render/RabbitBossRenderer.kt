package org.example.hoon.rabbitboss.client.render

import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.resources.Identifier
import org.example.hoon.rabbitboss.entity.RabbitBossEntity
import com.geckolib.renderer.GeoEntityRenderer

class RabbitBossRenderer(context: EntityRendererProvider.Context) :
    GeoEntityRenderer<RabbitBossEntity, EntityRenderState>(context, RabbitBossModel()) {

    init {
        withScale(5.0f)
        shadowRadius = 3.5f
    }

    override fun getRenderType(
        state: EntityRenderState,
        texture: Identifier
    ): RenderType = RenderTypes.entityTranslucent(texture)

    override fun shouldRender(
        animatable: RabbitBossEntity,
        frustum: Frustum,
        x: Double,
        y: Double,
        z: Double
    ): Boolean = true
}
