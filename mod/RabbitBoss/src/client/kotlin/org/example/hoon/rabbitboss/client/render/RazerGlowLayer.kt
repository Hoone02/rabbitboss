package org.example.hoon.rabbitboss.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import org.example.hoon.rabbitboss.entity.RazerEntity
import org.joml.Matrix4f
import software.bernie.geckolib.cache.`object`.BakedGeoModel
import software.bernie.geckolib.renderer.GeoRenderer
import software.bernie.geckolib.renderer.layer.GeoRenderLayer

class RazerGlowLayer(renderer: GeoRenderer<RazerEntity>) : GeoRenderLayer<RazerEntity>(renderer) {
    override fun render(
        poseStack: PoseStack,
        animatable: RazerEntity,
        bakedModel: BakedGeoModel,
        renderType: RenderType?,
        bufferSource: MultiBufferSource,
        buffer: VertexConsumer?,
        partialTick: Float,
        packedLight: Int,
        packedOverlay: Int,
        renderColor: Int
    ) {
        val glowBuffer = bufferSource.getBuffer(RenderType.lightning())
        val pose = poseStack.last().pose()
        addBeamPlanes(glowBuffer, pose)
    }

    private fun addBeamPlanes(buffer: VertexConsumer, pose: Matrix4f) {
        addCross(buffer, pose, 9.3f, 1.55f, 255, 24, 0, 150)
        addCross(buffer, pose, 9.1f, 1.05f, 255, 58, 8, 205)
        addCross(buffer, pose, 8.85f, 0.62f, 255, 130, 32, 245)
        addCross(buffer, pose, 8.55f, 0.32f, 255, 230, 96, 255)
        addCross(buffer, pose, 8.25f, 0.15f, 255, 255, 235, 255)
    }

    private fun addCross(
        buffer: VertexConsumer,
        pose: Matrix4f,
        halfLength: Float,
        halfWidth: Float,
        red: Int,
        green: Int,
        blue: Int,
        alpha: Int
    ) {
        addQuad(buffer, pose, -halfLength, -halfWidth, 0.0f, halfLength, halfWidth, 0.0f, red, green, blue, alpha)
        addQuad(buffer, pose, -halfLength, 0.0f, -halfWidth, halfLength, 0.0f, halfWidth, red, green, blue, alpha)
        addQuad(buffer, pose, -halfLength, -halfWidth, -halfWidth, halfLength, halfWidth, halfWidth, red, green, blue, alpha)
        addQuad(buffer, pose, -halfLength, -halfWidth, halfWidth, halfLength, halfWidth, -halfWidth, red, green, blue, alpha)
    }

    private fun addQuad(
        buffer: VertexConsumer,
        pose: Matrix4f,
        x1: Float,
        y1: Float,
        z1: Float,
        x2: Float,
        y2: Float,
        z2: Float,
        red: Int,
        green: Int,
        blue: Int,
        alpha: Int
    ) {
        buffer.addVertex(pose, x1, y1, z1).setColor(red, green, blue, alpha).setLight(LightTexture.FULL_BRIGHT)
        buffer.addVertex(pose, x1, y2, z2).setColor(red, green, blue, alpha).setLight(LightTexture.FULL_BRIGHT)
        buffer.addVertex(pose, x2, y2, z2).setColor(red, green, blue, alpha).setLight(LightTexture.FULL_BRIGHT)
        buffer.addVertex(pose, x2, y1, z1).setColor(red, green, blue, alpha).setLight(LightTexture.FULL_BRIGHT)
    }
}
