package org.example.hoon.rabbitboss.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import org.example.hoon.rabbitboss.entity.TntIndicatorEntity

class TntIndicatorRenderer(context: EntityRendererProvider.Context) :
    EntityRenderer<TntIndicatorEntity, TntIndicatorRenderState>(context) {

    override fun createRenderState(): TntIndicatorRenderState = TntIndicatorRenderState()

    override fun shouldRender(
        entity: TntIndicatorEntity,
        frustum: Frustum,
        x: Double,
        y: Double,
        z: Double
    ): Boolean = true

    override fun extractRenderState(
        entity: TntIndicatorEntity,
        state: TntIndicatorRenderState,
        partialTick: Float
    ) {
        super.extractRenderState(entity, state, partialTick)
        state.yaw = entity.yRot
        state.progress = ((entity.tickCount + partialTick) / entity.durationTicks().toFloat()).coerceIn(0.0f, 1.0f)
    }

    override fun submit(
        state: TntIndicatorRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        cameraState: CameraRenderState
    ) {
        val maxDistance = kotlin.math.sqrt(11.0f * 11.0f + 2.0f * 2.0f)
        val y = 0.045f
        val fillRadius = state.progress * maxDistance
        poseStack.pushPose()
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yaw))
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lightning()) { renderedPose, buffer ->
            val pose = renderedPose.pose()

            for (front in -11..11) {
                for (side in -2..2) {
                    val distance = kotlin.math.sqrt((front * front + side * side).toFloat())
                    val fill = (1.0f - ((distance - fillRadius) / 1.35f)).coerceIn(0.0f, 1.0f)
                    val isBorder = front == -11 || front == 11 || side == -2 || side == 2
                    val minX = side - 0.5f
                    val maxX = side + 0.5f
                    val minZ = front - 0.5f
                    val maxZ = front + 0.5f

                    val baseAlpha = if (isBorder) 92 else 34
                    addQuad(buffer, pose, minX, y, minZ, maxX, maxZ, 255, 32, 0, baseAlpha)

                    if (fill > 0.0f) {
                        val pulse = if (state.progress > 0.82f) {
                            (kotlin.math.sin((state.progress - 0.82f) * 80.0f) * 0.5f + 0.5f).coerceIn(0.0f, 1.0f)
                        } else {
                            0.0f
                        }
                        val alpha = (70 + fill * 165 + pulse * 25).toInt().coerceIn(70, 255)
                        val green = (24 * (1.0f - fill)).toInt().coerceIn(0, 24)
                        addQuad(buffer, pose, minX, y + 0.012f, minZ, maxX, maxZ, 255, green, 0, alpha)
                    }
                }
            }
        }
        poseStack.popPose()
        super.submit(state, poseStack, submitNodeCollector, cameraState)
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
