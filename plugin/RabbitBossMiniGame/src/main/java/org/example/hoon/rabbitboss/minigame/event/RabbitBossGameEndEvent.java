package org.example.hoon.rabbitboss.minigame.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.UUID;

public class RabbitBossGameEndEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final List<UUID> survivorIds;
    private final List<Player> survivors;

    public RabbitBossGameEndEvent(List<UUID> survivorIds, List<Player> survivors) {
        this.survivorIds = List.copyOf(survivorIds);
        this.survivors = List.copyOf(survivors);
    }

    public List<UUID> getSurvivorIds() {
        return survivorIds;
    }

    public List<Player> getSurvivors() {
        return survivors;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
