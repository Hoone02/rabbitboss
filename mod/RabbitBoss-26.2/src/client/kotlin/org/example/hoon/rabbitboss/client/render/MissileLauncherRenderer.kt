package org.example.hoon.rabbitboss.client.render

import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import org.example.hoon.rabbitboss.entity.MissileLauncherEntity
import com.geckolib.renderer.GeoEntityRenderer

class MissileLauncherRenderer(context: EntityRendererProvider.Context) :
    GeoEntityRenderer<MissileLauncherEntity, EntityRenderState>(context, MissileLauncherModel()) {

    init {
        withScale(5.0f)
        shadowRadius = 4.5f
    }

    override fun shouldRender(
        animatable: MissileLauncherEntity,
        frustum: Frustum,
        x: Double,
        y: Double,
        z: Double
    ): Boolean = true
}
