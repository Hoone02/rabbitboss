package org.example.hoon.rabbitboss.client.hud

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import org.example.hoon.rabbitboss.Rabbitboss
import net.minecraft.sounds.SoundEvents
import kotlin.random.Random

object RabbitBossLifeHud {
    private const val HEART_SIZE = 18
    private const val GAP = 6
    private const val PIXEL = 2
    private const val ACTIVE = 0xFFE83A5A.toInt()
    private const val ACTIVE_DARK = 0xFF79182B.toInt()
    private const val EMPTY = 0xFF2C3038.toInt()
    private const val EMPTY_EDGE = 0xFF6A7080.toInt()
    private const val CYAN_GLITCH = 0xCC34F7FF.toInt()
    private const val RED_GLITCH = 0xCCE83A5A.toInt()

    private var lives = 0
    private var maxLives = 0
    private var visible = false
    private var glitchTicks = 0

    fun register() {
        HudElementRegistry.addLast(Rabbitboss.id("life_hud")) { graphics, _ -> render(graphics) }
    }

    fun update(newLives: Int, newMaxLives: Int, newVisible: Boolean) {
        if (newLives < lives) {
            glitchTicks = 12
            playHitFeedback()
        }
        lives = newLives.coerceAtLeast(0)
        maxLives = newMaxLives.coerceAtLeast(0)
        visible = newVisible && maxLives > 0
    }

    private fun render(graphics: GuiGraphicsExtractor) {
        if (!visible) {
            return
        }

        val width = graphics.guiWidth()
        val height = graphics.guiHeight()
        val totalWidth = maxLives * HEART_SIZE + (maxLives - 1).coerceAtLeast(0) * GAP
        val startX = (width - totalWidth) / 2
        val y = height - 66

        drawBackplate(graphics, startX - 8, y - 6, totalWidth + 16, HEART_SIZE + 12)
        for (index in 0 until maxLives) {
            drawPixelHeart(graphics, startX + index * (HEART_SIZE + GAP), y, index < lives)
        }

        if (glitchTicks > 0) {
            drawGlitch(graphics, startX - 6, y - 4, totalWidth + 12, HEART_SIZE + 8)
            glitchTicks--
        }
    }

    private fun drawBackplate(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int) {
        graphics.fill(x + 2, y, x + width - 2, y + height, 0x66000000)
        graphics.fill(x, y + 2, x + width, y + height - 2, 0x66000000)
        graphics.fill(x + 3, y + height - 2, x + width - 3, y + height, 0xAA080A10.toInt())
    }

    private fun drawPixelHeart(graphics: GuiGraphicsExtractor, x: Int, y: Int, filled: Boolean) {
        val main = if (filled) ACTIVE else EMPTY
        val shade = if (filled) ACTIVE_DARK else EMPTY_EDGE
        val rows = intArrayOf(
            0b0110110,
            0b1111111,
            0b1111111,
            0b1111111,
            0b0111110,
            0b0011100,
            0b0001000
        )

        rows.forEachIndexed { row, bits ->
            for (col in 0 until 7) {
                if ((bits and (1 shl (6 - col))) != 0) {
                    val color = if (row >= 4 || col >= 4) shade else main
                    graphics.fill(x + col * PIXEL, y + row * PIXEL, x + col * PIXEL + PIXEL, y + row * PIXEL + PIXEL, color)
                }
            }
        }

        if (filled) {
            graphics.fill(x + 4, y + 3, x + 8, y + 5, 0xFFFF8CA0.toInt())
        }
    }

    private fun drawGlitch(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int) {
        repeat(5) {
            val gx = x + Random.nextInt(width.coerceAtLeast(1))
            val gy = y + Random.nextInt(height.coerceAtLeast(1))
            val gw = Random.nextInt(4, 14)
            val color = if (it % 2 == 0) CYAN_GLITCH else RED_GLITCH
            graphics.fill(gx - 2, gy, gx + gw, gy + 2, color)
        }
    }

    private fun playHitFeedback() {
        val player = Minecraft.getInstance().player ?: return
        player.playSound(SoundEvents.PLAYER_HURT, 0.65f, 1.25f)
        player.playSound(SoundEvents.NOTE_BLOCK_BIT.value(), 0.75f, 0.55f)
        player.playSound(SoundEvents.NOTE_BLOCK_BIT.value(), 0.45f, 1.85f)
    }
}
