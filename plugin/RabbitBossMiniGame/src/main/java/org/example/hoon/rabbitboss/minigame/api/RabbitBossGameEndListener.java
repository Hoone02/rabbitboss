package org.example.hoon.rabbitboss.minigame.api;

import org.bukkit.entity.Player;

import java.util.List;

@FunctionalInterface
public interface RabbitBossGameEndListener {
    void onGameEnd(List<Player> survivors);
}
