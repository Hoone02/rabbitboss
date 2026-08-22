package org.example.hoon.rabbitboss.network

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import org.example.hoon.rabbitboss.Rabbitboss

data class LifeHudPayload(
    val lives: Int,
    val maxLives: Int,
    val visible: Boolean
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<LifeHudPayload> =
            CustomPacketPayload.Type(Rabbitboss.id("life_hud"))

        val CODEC: StreamCodec<RegistryFriendlyByteBuf, LifeHudPayload> =
            StreamCodec.of(
                { buffer, payload ->
                    buffer.writeVarInt(payload.lives)
                    buffer.writeVarInt(payload.maxLives)
                    buffer.writeBoolean(payload.visible)
                },
                { buffer ->
                    LifeHudPayload(
                        buffer.readVarInt(),
                        buffer.readVarInt(),
                        buffer.readBoolean()
                    )
                }
            )
    }
}
