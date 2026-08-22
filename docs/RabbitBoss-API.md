# RabbitBoss API

RabbitBoss exposes a Bukkit plugin API and a Fabric server-mod bridge. Both APIs call the game system directly; they do not execute RCON or console commands.

## Bukkit/Cardboard plugin API

Add `RabbitBossMiniGame-1.0-SNAPSHOT.jar` as a compile-only dependency and declare RabbitBossMiniGame as a dependency in `plugin.yml`:

```yaml
depend: [RabbitBossMiniGame]
```

Main API class:

```java
import org.example.hoon.rabbitboss.minigame.api.GameState;
import org.example.hoon.rabbitboss.minigame.api.RabbitBossMiniGameApi;

GameState state = RabbitBossMiniGameApi.getState();
int remaining = RabbitBossMiniGameApi.getRemainingPlayerCount();
List<Player> alivePlayers = RabbitBossMiniGameApi.getAlivePlayers();
List<UUID> aliveIds = RabbitBossMiniGameApi.getAlivePlayerIds();
int lives = RabbitBossMiniGameApi.getLives(player);

boolean started = RabbitBossMiniGameApi.startGame();
boolean stopped = RabbitBossMiniGameApi.stopGame();
```

`getAlivePlayers()`, `getAlivePlayerIds()`, and `getRemainingPlayerCount()` use the same rules as the minigame's completion check: the player must be online, alive, inside the configured area, and have at least one life. They return an empty result while the game is not `STARTING` or `RUNNING`.

The last completed game's result remains available separately:

```java
List<Player> survivors = RabbitBossMiniGameApi.getLastSurvivors();
List<UUID> survivorIds = RabbitBossMiniGameApi.getLastSurvivorIds();
```

### Bukkit events

```java
@EventHandler
public void onRabbitBossStart(RabbitBossGameStartEvent event) {
    // Game entered its start sequence.
}

@EventHandler
public void onRabbitBossEnd(RabbitBossGameEndEvent event) {
    List<Player> survivors = event.getSurvivors();
    List<UUID> survivorIds = event.getSurvivorIds();
}
```

Event classes are in `org.example.hoon.rabbitboss.minigame.event`. Register the listener through Bukkit's normal `PluginManager.registerEvents` API. A callback can also be registered with `RabbitBossMiniGameApi.addGameEndListener(listener)`.

### Game states

- `UNAVAILABLE`: RabbitBoss server bridge is unavailable.
- `WAITING`: no game is active.
- `STARTING`: boss start presentation is active.
- `RUNNING`: skill patterns are active.

Call the Bukkit API from the server main thread. Returned lists are snapshots and modifying them does not alter the game.

## Fabric mod API

Compile against the RabbitBoss mod JAR matching the Minecraft server version and declare a RabbitBoss dependency in `fabric.mod.json`:

```json
{
  "depends": {
    "rabbitboss": "*"
  }
}
```

Kotlin example:

```kotlin
import org.example.hoon.rabbitboss.api.RabbitBossServerBridge

val running = RabbitBossServerBridge.isRunning()
val remaining = RabbitBossServerBridge.getRemainingPlayerCount()
val aliveIds = RabbitBossServerBridge.getAlivePlayerIds()
val lives = RabbitBossServerBridge.getPlayerLives(player.uuid.toString())

RabbitBossServerBridge.requestStart()
RabbitBossServerBridge.requestStop()
```

Java example:

```java
boolean running = RabbitBossServerBridge.isRunning();
int remaining = RabbitBossServerBridge.getRemainingPlayerCount();
List<UUID> aliveIds = RabbitBossServerBridge.getAlivePlayerIds();
int lives = RabbitBossServerBridge.getPlayerLives(player.getUUID().toString());
```

The Fabric remaining-player methods report players tracked by the active game whose lives are greater than zero. They return zero/an empty list when the game is not running. `requestStart()` and `requestStop()` enqueue direct internal requests and do not dispatch commands.

## Distribution artifacts

- Minecraft 1.21.4 mod API: `mod/RabbitBoss/build/libs/RabbitBoss-1.0-SNAPSHOT.jar`
- Minecraft 26.2 mod API: `mod/RabbitBoss-26.2/build/libs/RabbitBoss-26.2-1.0-26.2.jar`
- Bukkit/Cardboard plugin API: `plugin/RabbitBossMiniGame/build/libs/RabbitBossMiniGame-1.0-SNAPSHOT.jar`
