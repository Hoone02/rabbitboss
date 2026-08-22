package org.example.hoon.rabbitboss.minigame.api;

import org.bukkit.entity.Player;
import org.example.hoon.rabbitboss.minigame.RabbitBossMiniGamePlugin;

import java.util.List;
import java.util.UUID;

public final class RabbitBossMiniGameApi {
    private RabbitBossMiniGameApi() {
    }

    public static GameState getState() {
        RabbitBossMiniGamePlugin plugin = RabbitBossMiniGamePlugin.getInstance();
        return plugin == null ? GameState.UNAVAILABLE : plugin.getGameState();
    }

    public static List<Player> getLastSurvivors() {
        RabbitBossMiniGamePlugin plugin = RabbitBossMiniGamePlugin.getInstance();
        return plugin == null ? List.of() : plugin.getLastSurvivorPlayers();
    }

    public static List<UUID> getLastSurvivorIds() {
        RabbitBossMiniGamePlugin plugin = RabbitBossMiniGamePlugin.getInstance();
        return plugin == null ? List.of() : plugin.getLastSurvivorIds();
    }

    public static int getLives(Player player) {
        RabbitBossMiniGamePlugin plugin = RabbitBossMiniGamePlugin.getInstance();
        return plugin == null ? 0 : plugin.getLives(player);
    }

    public static List<Player> getAlivePlayers() {
        RabbitBossMiniGamePlugin plugin = RabbitBossMiniGamePlugin.getInstance();
        return plugin == null ? List.of() : plugin.getAlivePlayers();
    }

    public static List<UUID> getAlivePlayerIds() {
        RabbitBossMiniGamePlugin plugin = RabbitBossMiniGamePlugin.getInstance();
        return plugin == null ? List.of() : plugin.getAlivePlayerIds();
    }

    public static int getRemainingPlayerCount() {
        RabbitBossMiniGamePlugin plugin = RabbitBossMiniGamePlugin.getInstance();
        return plugin == null ? 0 : plugin.getRemainingPlayerCount();
    }

    public static void addGameEndListener(RabbitBossGameEndListener listener) {
        RabbitBossMiniGamePlugin plugin = RabbitBossMiniGamePlugin.getInstance();
        if (plugin != null) {
            plugin.addGameEndListener(listener);
        }
    }

    public static boolean startGame() {
        RabbitBossMiniGamePlugin plugin = RabbitBossMiniGamePlugin.getInstance();
        return plugin != null && plugin.startGame();
    }

    public static boolean stopGame() {
        RabbitBossMiniGamePlugin plugin = RabbitBossMiniGamePlugin.getInstance();
        return plugin != null && plugin.stopGame(false);
    }
}
