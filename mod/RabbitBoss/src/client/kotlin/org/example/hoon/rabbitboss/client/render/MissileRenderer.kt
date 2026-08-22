package org.example.hoon.rabbitboss.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRendererProvider
import org.example.hoon.rabbitboss.entity.MissileEntity
import software.bernie.geckolib.renderer.GeoEntityRenderer

class MissileRenderer(context: EntityRendererProvider.Context) :
    GeoEntityRenderer<MissileEntity>(context, MissileModel()) {

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

    override fun applyRotations(
        animatable: MissileEntity,
        poseStack: PoseStack,
        ageInTicks: Float,
        rotationYaw: Float,
        partialTick: Float,
        nativeScale: Float
    ) {
        super.applyRotations(animatable, poseStack, ageInTicks, animatable.renderYaw(), partialTick, nativeScale)
        poseStack.mulPose(Axis.XP.rotationDegrees(-animatable.renderPitch()))
    }
}
