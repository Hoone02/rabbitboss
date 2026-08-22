package org.example.hoon.rabbitboss.client.render

import net.minecraft.client.renderer.entity.state.EntityRenderState
import com.geckolib.constant.dataticket.DataTicket

class TntIndicatorRenderState : EntityRenderState() {
    private val geckolibData = mutableMapOf<DataTicket<*>, Any>()

    override fun getDataMap(): MutableMap<DataTicket<*>, Any> = geckolibData
    var yaw: Float = 0.0f
    var progress: Float = 0.0f
}
