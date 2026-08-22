package org.example.hoon.rabbitboss.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import org.example.hoon.rabbitboss.entity.GlobalTwoIndicatorEntity

class GlobalTwoIndicatorRenderer(context: EntityRendererProvider.Context) :
    EntityRenderer<GlobalTwoIndicatorEntity, GlobalTwoIndicatorRenderState>(context) {

    override fun createRenderState(): GlobalTwoIndicatorRenderState = GlobalTwoIndicatorRenderState()

    override fun shouldRender(
        entity: GlobalTwoIndicatorEntity,
        frustum: Frustum,
        x: Double,
        y: Double,
        z: Double
    ): Boolean = true

    override fun extractRenderState(
        entity: GlobalTwoIndicatorEntity,
        state: GlobalTwoIndicatorRenderState,
        partialTick: Float
    ) {
        super.extractRenderState(entity, state, partialTick)
        state.yaw = entity.yRot
        state.progress = ((entity.tickCount + partialTick) / entity.fillTicks().toFloat()).coerceIn(0.0f, 1.0f)
    }

    override fun render(
        state: GlobalTwoIndicatorRenderState,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    ) {
        poseStack.pushPose()
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yaw))
        val pose = poseStack.last().pose()
        val buffer = bufferSource.getBuffer(RenderType.debugQuads())
        val maxDistance = kotlin.math.sqrt(11.0f * 11.0f + 12.0f * 12.0f)
        val fillRadius = state.progress * maxDistance

        for (front in -11..11) {
            for (side in -12..12) {
                val distance = kotlin.math.sqrt((front * front + side * side).toFloat())
                val fill = (1.0f - ((distance - fillRadius) / 1.35f)).coerceIn(0.0f, 1.0f)
                val isBorder = front == -11 || front == 11 || side == -12 || side == 12
                val minX = side - 0.5f
                val maxX = side + 0.5f
                val minZ = front - 0.5f
                val maxZ = front + 0.5f

                addQuad(buffer, pose, minX, 0.055f, minZ, maxX, maxZ, 0, 110, 255, if (isBorder) 108 else 36)

                if (fill > 0.0f) {
                    val alpha = (72 + fill * 170).toInt().coerceIn(72, 242)
                    addQuad(buffer, pose, minX, 0.072f, minZ, maxX, maxZ, 0, 155, 255, alpha)
                }
            }
        }

        poseStack.popPose()
    }

    private fun addQuad(
        buffer: com.mojang.blaze3d.vertex.VertexConsumer,
        pose: org.joml.Matrix4f,
        minX: Float,
        y: Float,
        minZ: Float,
        maxX: Float,
        maxZ: Float,
        red: Int,
        green: Int,
        blue: Int,
        alpha: Int
    ) {
        buffer.addVertex(pose, minX, y, minZ).setColor(red, green, blue, alpha)
        buffer.addVertex(pose, minX, y, maxZ).setColor(red, green, blue, alpha)
        buffer.addVertex(pose, maxX, y, maxZ).setColor(red, green, blue, alpha)
        buffer.addVertex(pose, maxX, y, minZ).setColor(red, green, blue, alpha)
    }
}
