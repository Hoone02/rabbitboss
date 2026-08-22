package org.example.hoon.rabbitboss.client.render

import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.resources.Identifier
import org.example.hoon.rabbitboss.entity.RazerEntity
import com.geckolib.renderer.GeoEntityRenderer

class RazerRenderer(context: EntityRendererProvider.Context) :
    GeoEntityRenderer<RazerEntity, EntityRenderState>(context, RazerModel()) {

    init {
        withScale(25.0f)
        shadowRadius = 0.0f
    }

    override fun getRenderType(
        state: EntityRenderState,
        texture: Identifier
    ): RenderType = RenderTypes.entityTranslucent(texture)

    override fun shouldRender(
        animatable: RazerEntity,
        frustum: Frustum,
        x: Double,
        y: Double,
        z: Double
    ): Boolean = true
}
