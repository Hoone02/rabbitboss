package org.example.hoon.rabbitboss.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import org.example.hoon.rabbitboss.entity.MissileIndicatorEntity

class MissileIndicatorRenderer(context: EntityRendererProvider.Context) :
    EntityRenderer<MissileIndicatorEntity, MissileIndicatorRenderState>(context) {

    override fun createRenderState(): MissileIndicatorRenderState = MissileIndicatorRenderState()

    override fun shouldRender(
        entity: MissileIndicatorEntity,
        frustum: Frustum,
        x: Double,
        y: Double,
        z: Double
    ): Boolean = true

    override fun extractRenderState(
        entity: MissileIndicatorEntity,
        state: MissileIndicatorRenderState,
        partialTick: Float
    ) {
        super.extractRenderState(entity, state, partialTick)
        state.yaw = entity.yRot
        state.progress = ((entity.tickCount + partialTick) / MissileIndicatorEntity.DURATION_TICKS).coerceIn(0.0f, 1.0f)
    }

    override fun submit(
        state: MissileIndicatorRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        cameraState: CameraRenderState
    ) {
        poseStack.pushPose()
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yaw))
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.debugQuads()) { renderedPose, buffer ->
            val pose = renderedPose.pose()
        val fillRadius = state.progress * 2.84f

        for (front in -2..2) {
            for (side in -2..2) {
                val distance = kotlin.math.sqrt((front * front + side * side).toFloat())
                val fill = (1.0f - ((distance - fillRadius) / 0.75f)).coerceIn(0.0f, 1.0f)
                val isBorder = front == -2 || front == 2 || side == -2 || side == 2
                val minX = side - 0.5f
                val maxX = side + 0.5f
                val minZ = front - 0.5f
                val maxZ = front + 0.5f

                addQuad(buffer, pose, minX, 0.05f, minZ, maxX, maxZ, 255, 40, 0, if (isBorder) 120 else 52)
                if (fill > 0.0f) {
                    val alpha = (80 + fill * 175).toInt().coerceIn(80, 255)
                    addQuad(buffer, pose, minX, 0.065f, minZ, maxX, maxZ, 255, 0, 0, alpha)
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
