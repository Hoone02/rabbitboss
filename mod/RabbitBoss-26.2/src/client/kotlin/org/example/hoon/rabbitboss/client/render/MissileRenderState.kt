package org.example.hoon.rabbitboss.client.render

import com.geckolib.constant.dataticket.DataTicket
import net.minecraft.client.renderer.entity.state.EntityRenderState

class MissileRenderState : EntityRenderState() {
    private val geckolibData = mutableMapOf<DataTicket<*>, Any>()

    var missileYaw = 0.0f
    var missilePitch = 0.0f

    override fun getDataMap(): MutableMap<DataTicket<*>, Any> = geckolibData
}
