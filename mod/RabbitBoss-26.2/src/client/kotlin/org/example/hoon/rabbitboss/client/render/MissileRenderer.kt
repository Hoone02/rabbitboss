package org.example.hoon.rabbitboss.client.render

import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import org.example.hoon.rabbitboss.entity.MissileEntity
import com.geckolib.renderer.GeoEntityRenderer

class MissileRenderer(context: EntityRendererProvider.Context) :
    GeoEntityRenderer<MissileEntity, EntityRenderState>(context, MissileModel()) {

    init {
        withScale(5.0f)
        shadowRadius = 1.5f
    }

    override fun shouldRender(
        animatable: MissileEntity,
        frustum: Frustum,
        x: Double,
        y: Double,
        z: Double
    ): Boolean = true
}
