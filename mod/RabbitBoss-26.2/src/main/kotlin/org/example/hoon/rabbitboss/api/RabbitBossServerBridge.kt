package org.example.hoon.rabbitboss.api

import org.example.hoon.rabbitboss.command.RabbitBossCommands
import java.util.UUID

object RabbitBossServerBridge {
    @JvmStatic
    fun requestStart(): Boolean {
        RabbitBossCommands.requestExternalStart()
        return true
    }

    @JvmStatic
    fun requestStop(): Boolean {
        RabbitBossCommands.requestExternalStop()
        return true
    }

    @JvmStatic
    fun isRunning(): Boolean = RabbitBossCommands.isGameRunning()

    @JvmStatic
    fun getPlayerLives(uuid: String): Int =
        runCatching { RabbitBossCommands.getPlayerLives(UUID.fromString(uuid)) }.getOrDefault(0)

    @JvmStatic
    fun getAlivePlayerIds(): List<UUID> = RabbitBossCommands.getAlivePlayerIds()

    @JvmStatic
    fun getRemainingPlayerCount(): Int = RabbitBossCommands.getRemainingPlayerCount()
}
