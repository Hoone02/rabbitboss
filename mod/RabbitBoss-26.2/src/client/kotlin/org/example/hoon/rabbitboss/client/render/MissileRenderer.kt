package org.example.hoon.rabbitboss.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRendererProvider
import org.example.hoon.rabbitboss.entity.MissileEntity
import com.geckolib.renderer.GeoEntityRenderer
import com.geckolib.renderer.base.RenderPassInfo

class MissileRenderer(context: EntityRendererProvider.Context) :
    GeoEntityRenderer<MissileEntity, MissileRenderState>(context, MissileModel()) {

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

    override fun createRenderState(animatable: MissileEntity, relatedObject: Void?): MissileRenderState = MissileRenderState()

    override fun extractRenderState(entity: MissileEntity, state: MissileRenderState, partialTick: Float) {
        super.extractRenderState(entity, state, partialTick)
        state.missileYaw = entity.renderYaw()
        state.missilePitch = entity.renderPitch()
    }

    override fun applyRotations(renderPassInfo: RenderPassInfo<MissileRenderState>, poseStack: PoseStack, nativeScale: Float) {
        super.applyRotations(renderPassInfo, poseStack, nativeScale)
        poseStack.mulPose(Axis.XP.rotationDegrees(-renderPassInfo.renderState().missilePitch))
    }
}
