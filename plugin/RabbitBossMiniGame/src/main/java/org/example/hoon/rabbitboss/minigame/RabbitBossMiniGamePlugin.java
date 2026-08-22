package org.example.hoon.rabbitboss.minigame;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.example.hoon.rabbitboss.minigame.api.GameState;
import org.example.hoon.rabbitboss.minigame.api.RabbitBossGameEndListener;
import org.example.hoon.rabbitboss.minigame.event.RabbitBossGameEndEvent;
import org.example.hoon.rabbitboss.minigame.event.RabbitBossGameStartEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class RabbitBossMiniGamePlugin extends JavaPlugin implements TabExecutor {
    private static RabbitBossMiniGamePlugin instance;
    private final List<RabbitBossGameEndListener> endListeners = new ArrayList<>();
    private final List<UUID> lastSurvivorIds = new ArrayList<>();
    private final Map<UUID, Integer> lives = new HashMap<>();
    private GameState state = GameState.WAITING;
    private BukkitTask monitorTask;

    public static RabbitBossMiniGamePlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        migrateLegacyPropertiesIfMissing();
        syncLegacyProperties();
        getCommand("rabbitboss").setExecutor(this);
        getCommand("rabbitboss").setTabCompleter(this);
        startMonitorTask();
        getLogger().info("RabbitBossMiniGame enabled");
    }

    @Override
    public void onDisable() {
        if (monitorTask != null) {
            monitorTask.cancel();
        }
        instance = null;
    }

    public GameState getGameState() {
        return state == GameState.WAITING && !isRabbitBossBridgeAvailable() ? GameState.UNAVAILABLE : state;
    }

    public List<UUID> getLastSurvivorIds() {
        return List.copyOf(lastSurvivorIds);
    }

    public List<Player> getLastSurvivorPlayers() {
        return lastSurvivorIds.stream()
            .map(Bukkit::getPlayer)
            .filter(player -> player != null && player.isOnline())
            .toList();
    }

    public int getLives(Player player) {
        return getBridgeLives(player);
    }

    public List<Player> getAlivePlayers() {
        if (state != GameState.STARTING && state != GameState.RUNNING) {
            return List.of();
        }
        return List.copyOf(alivePlayersInArea());
    }

    public List<UUID> getAlivePlayerIds() {
        return getAlivePlayers().stream()
            .map(Player::getUniqueId)
            .toList();
    }

    public int getRemainingPlayerCount() {
        return getAlivePlayers().size();
    }

    public void addGameEndListener(RabbitBossGameEndListener listener) {
        endListeners.add(listener);
    }

    public boolean startGame() {
        if (state == GameState.STARTING || state == GameState.RUNNING) {
            return false;
        }
        if (!hasRequiredSettings()) {
            getLogger().warning("RabbitBoss settings are incomplete. Use /rabbitboss status.");
            return false;
        }
        syncLegacyProperties();
        resetLives(alivePlayersInArea());
        state = GameState.STARTING;
        lastSurvivorIds.clear();
        Bukkit.getPluginManager().callEvent(new RabbitBossGameStartEvent());
        if (!requestRabbitBossStart()) {
            state = GameState.WAITING;
            getLogger().warning("Failed to request RabbitBoss start.");
            return false;
        }
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (state == GameState.STARTING) {
                state = GameState.RUNNING;
            }
        }, 300L);
        return true;
    }

    public boolean stopGame(boolean completed) {
        if (state == GameState.WAITING) {
            return false;
        }
        List<Player> survivors = alivePlayersInArea();
        lastSurvivorIds.clear();
        survivors.forEach(player -> lastSurvivorIds.add(player.getUniqueId()));
        requestRabbitBossStop();
        state = GameState.WAITING;
        clearHud();
        lives.clear();
        if (completed) {
            RabbitBossGameEndEvent event = new RabbitBossGameEndEvent(lastSurvivorIds, survivors);
            Bukkit.getPluginManager().callEvent(event);
            endListeners.forEach(listener -> listener.onGameEnd(survivors));
        }
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || "status".equalsIgnoreCase(args[0])) {
            sendStatus(sender);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[0])) {
            reloadConfig();
            syncLegacyProperties();
            restartMonitorTask();
            sender.sendMessage("RabbitBossMiniGame config reloaded");
            return true;
        }
        if ("start".equalsIgnoreCase(args[0])) {
            sender.sendMessage(startGame() ? "RabbitBoss game starting" : "RabbitBoss game could not start");
            return true;
        }
        if ("stop".equalsIgnoreCase(args[0])) {
            sender.sendMessage(stopGame(false) ? "RabbitBoss game stopped" : "RabbitBoss game is not running");
            return true;
        }
        if ("setting".equalsIgnoreCase(args[0])) {
            return handleSetting(sender, args);
        }
        sender.sendMessage("Usage: /rabbitboss <start|stop|status|reload|setting>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return partial(args[0], List.of("start", "stop", "status", "reload", "setting"));
        }
        if (args.length == 2 && "setting".equalsIgnoreCase(args[0])) {
            return partial(args[1], List.of("spawn", "canon", "tntarea", "area"));
        }
        if (args.length == 3 && "setting".equalsIgnoreCase(args[0])) {
            return partial(args[2], List.of("setpos"));
        }
        if (args.length == 4 && "setting".equalsIgnoreCase(args[0]) &&
            ("canon".equalsIgnoreCase(args[1]) || "tntarea".equalsIgnoreCase(args[1]))) {
            return partial(args[3], List.of("1", "2", "3", "4", "5"));
        }
        return List.of();
    }

    private boolean handleSetting(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can set positions");
            return true;
        }
        if (args.length < 3 || !"setpos".equalsIgnoreCase(args[2])) {
            sender.sendMessage("Usage: /rabbitboss setting <spawn|canon|tntarea|area> setpos [1-5]");
            return true;
        }
        String type = args[1].toLowerCase(Locale.ROOT);
        if ("spawn".equals(type)) {
            savePosition("positions.boss-spawn", player.getLocation(), true);
            sender.sendMessage("RabbitBoss spawn position saved");
        } else if ("area".equals(type)) {
            savePosition("positions.area", player.getLocation(), false);
            sender.sendMessage("RabbitBoss area position saved");
        } else if ("canon".equals(type) || "tntarea".equals(type)) {
            if (args.length < 4) {
                sender.sendMessage("Usage: /rabbitboss setting " + type + " setpos <1-5>");
                return true;
            }
            int number = parseNumber(args[3]);
            if (number < 1 || number > 5) {
                sender.sendMessage("Number must be 1-5");
                return true;
            }
            savePosition("positions." + (type.equals("canon") ? "canons" : "tntareas") + "." + number, player.getLocation(), false);
            sender.sendMessage("RabbitBoss " + type + " " + number + " position saved");
        } else {
            sender.sendMessage("Usage: /rabbitboss setting <spawn|canon|tntarea|area> setpos [1-5]");
            return true;
        }
        saveConfig();
        syncLegacyProperties();
        return true;
    }

    private void startMonitorTask() {
        long interval = Math.max(1L, getConfig().getLong("auto-start-check-ticks", 20L));
        monitorTask = Bukkit.getScheduler().runTaskTimer(this, this::tickMiniGame, interval, interval);
    }

    private void restartMonitorTask() {
        if (monitorTask != null) {
            monitorTask.cancel();
        }
        startMonitorTask();
    }

    private void tickMiniGame() {
        if (!hasRequiredSettings()) {
            return;
        }
        List<Player> alive = alivePlayersInArea();
        if (state == GameState.WAITING && alive.size() >= getConfig().getInt("min-players", 30)) {
            startGame();
            return;
        }
        if (state == GameState.RUNNING && alive.size() <= getConfig().getInt("finish-survivors", 3)) {
            stopGame(true);
            return;
        }
        updateHud();
    }

    private List<Player> alivePlayersInArea() {
        Position area = readPosition("positions.area", false);
        if (area == null) {
            return List.of();
        }
        World world = Bukkit.getWorld(worldName(area.worldKey));
        if (world == null) {
            return List.of();
        }
        double forwardRange = getConfig().getDouble("trigger-area.forward-range", 11.0);
        double sideRange = getConfig().getDouble("trigger-area.side-range", 12.0);
        double verticalMin = getConfig().getDouble("trigger-area.vertical-min", -2.0);
        double verticalMax = getConfig().getDouble("trigger-area.vertical-max", 6.0);
        Vector2 forward = forward(area.yaw);
        Vector2 right = new Vector2(forward.z, -forward.x);

        return world.getPlayers().stream()
            .filter(player -> !player.isDead())
            .filter(player -> state == GameState.WAITING || getBridgeLives(player) > 0)
            .filter(player -> {
                Location loc = player.getLocation();
                double dx = loc.getX() - area.x;
                double dz = loc.getZ() - area.z;
                double dy = loc.getY() - area.y;
                double forwardOffset = dx * forward.x + dz * forward.z;
                double sideOffset = dx * right.x + dz * right.z;
                return Math.abs(forwardOffset) <= forwardRange + 0.75 &&
                    Math.abs(sideOffset) <= sideRange + 0.75 &&
                    dy >= verticalMin &&
                    dy <= verticalMax;
            })
            .toList();
    }

    private void resetLives(List<Player> players) {
        lives.clear();
        int maxLives = getConfig().getInt("player-lives", 5);
        players.forEach(player -> {
            lives.put(player.getUniqueId(), maxLives);
        });
    }

    private void updateHud() {
    }

    private void sendLivesHud(Player player) {
    }

    private void clearHud() {
    }

    private boolean isInsideConfiguredArea(Player player) {
        Position area = readPosition("positions.area", false);
        if (area == null || player.getWorld() == null || !player.getWorld().getName().equals(worldName(area.worldKey))) {
            return false;
        }
        double forwardRange = getConfig().getDouble("trigger-area.forward-range", 11.0);
        double sideRange = getConfig().getDouble("trigger-area.side-range", 12.0);
        double verticalMin = getConfig().getDouble("trigger-area.vertical-min", -2.0);
        double verticalMax = getConfig().getDouble("trigger-area.vertical-max", 6.0);
        Vector2 forward = forward(area.yaw);
        Vector2 right = new Vector2(forward.z, -forward.x);
        Location loc = player.getLocation();
        double dx = loc.getX() - area.x;
        double dz = loc.getZ() - area.z;
        double dy = loc.getY() - area.y;
        double forwardOffset = dx * forward.x + dz * forward.z;
        double sideOffset = dx * right.x + dz * right.z;
        return Math.abs(forwardOffset) <= forwardRange + 0.75 &&
            Math.abs(sideOffset) <= sideRange + 0.75 &&
            dy >= verticalMin &&
            dy <= verticalMax;
    }

    private boolean hasRequiredSettings() {
        if (readPosition("positions.boss-spawn", true) == null || readPosition("positions.area", false) == null) {
            return false;
        }
        for (int i = 1; i <= 5; i++) {
            if (readPosition("positions.canons." + i, false) == null ||
                readPosition("positions.tntareas." + i, false) == null) {
                return false;
            }
        }
        return true;
    }

    private void sendStatus(CommandSender sender) {
        sender.sendMessage("RabbitBossMiniGame state=" + state);
        sender.sendMessage("settings=" + (hasRequiredSettings() ? "complete" : "missing"));
        sender.sendMessage("alive-in-area=" + alivePlayersInArea().size());
        sender.sendMessage("min-players=" + getConfig().getInt("min-players", 30));
        sender.sendMessage("finish-survivors=" + getConfig().getInt("finish-survivors", 3));
    }

    private void savePosition(String path, Location loc, boolean includePitch) {
        getConfig().set(path + ".world", dimensionName(loc.getWorld()));
        getConfig().set(path + ".x", loc.getX());
        getConfig().set(path + ".y", loc.getY());
        getConfig().set(path + ".z", loc.getZ());
        getConfig().set(path + ".yaw", snapYaw(loc.getYaw()));
        if (includePitch) {
            getConfig().set(path + ".pitch", loc.getPitch());
        }
    }

    private Position readPosition(String path, boolean includePitch) {
        ConfigurationSection section = getConfig().getConfigurationSection(path);
        if (section == null || !section.contains("world") || !section.contains("x") ||
            !section.contains("y") || !section.contains("z") || !section.contains("yaw")) {
            return null;
        }
        return new Position(
            section.getString("world"),
            section.getDouble("x"),
            section.getDouble("y"),
            section.getDouble("z"),
            (float) section.getDouble("yaw"),
            includePitch ? (float) section.getDouble("pitch", 0.0) : 0.0f
        );
    }

    private void syncLegacyProperties() {
        File configDir = new File(getServer().getWorldContainer(), "config");
        configDir.mkdirs();
        writeSpawn(new File(configDir, "rabbitboss-spawn.properties"), readPosition("positions.boss-spawn", true));
        writeIndexed(new File(configDir, "rabbitboss-canons.properties"), "canon", "positions.canons");
        writeIndexed(new File(configDir, "rabbitboss-tntareas.properties"), "tntarea", "positions.tntareas");
        writeArea(new File(configDir, "rabbitboss-area.properties"), readPosition("positions.area", false));
    }

    private boolean isRabbitBossBridgeAvailable() {
        try {
            Class.forName("org.example.hoon.rabbitboss.api.RabbitBossServerBridge");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean requestRabbitBossStart() {
        try {
            Class<?> bridge = Class.forName("org.example.hoon.rabbitboss.api.RabbitBossServerBridge");
            Object result = bridge.getMethod("requestStart").invoke(null);
            if (Boolean.TRUE.equals(result)) {
                return true;
            }
        } catch (ReflectiveOperationException e) {
            getLogger().fine("RabbitBoss bridge reflection unavailable, using control file");
        }
        return writeControlRequest("start");
    }

    private boolean requestRabbitBossStop() {
        try {
            Class<?> bridge = Class.forName("org.example.hoon.rabbitboss.api.RabbitBossServerBridge");
            Object result = bridge.getMethod("requestStop").invoke(null);
            if (Boolean.TRUE.equals(result)) {
                return true;
            }
        } catch (ReflectiveOperationException e) {
            getLogger().fine("RabbitBoss bridge reflection unavailable, using control file");
        }
        return writeControlRequest("stop");
    }

    private int getBridgeLives(Player player) {
        try {
            Class<?> bridge = Class.forName("org.example.hoon.rabbitboss.api.RabbitBossServerBridge");
            Object result = bridge.getMethod("getPlayerLives", String.class).invoke(null, player.getUniqueId().toString());
            if (result instanceof Number number) {
                return number.intValue();
            }
        } catch (ReflectiveOperationException e) {
            getLogger().fine("RabbitBoss bridge lives unavailable");
        }
        return lives.getOrDefault(player.getUniqueId(), 0);
    }

    private boolean writeControlRequest(String command) {
        File configDir = new File(getServer().getWorldContainer(), "config");
        configDir.mkdirs();
        File file = new File(configDir, "rabbitboss-control.properties");
        Properties props = new Properties();
        props.setProperty("command", command);
        props.setProperty("nonce", Long.toString(System.nanoTime()));
        try (FileOutputStream output = new FileOutputStream(file)) {
            props.store(output, "RabbitBoss plugin control request");
            return true;
        } catch (IOException e) {
            getLogger().warning("Failed to write RabbitBoss control request: " + e.getMessage());
            return false;
        }
    }

    private void migrateLegacyPropertiesIfMissing() {
        if (hasRequiredSettings()) {
            return;
        }
        File configDir = new File(getServer().getWorldContainer(), "config");
        readLegacySpawn(new File(configDir, "rabbitboss-spawn.properties"));
        readLegacyIndexed(new File(configDir, "rabbitboss-canons.properties"), "canon", "positions.canons");
        readLegacyIndexed(new File(configDir, "rabbitboss-tntareas.properties"), "tntarea", "positions.tntareas");
        File areaFile = new File(configDir, "rabbitboss-area.properties");
        if (!areaFile.exists()) {
            areaFile = new File(configDir, "rabbitboss-missilearea.properties");
        }
        readLegacyArea(areaFile, "positions.area");
        saveConfig();
    }

    private void writeSpawn(File file, Position pos) {
        if (pos == null) {
            return;
        }
        Properties props = baseProps(pos);
        props.setProperty("pitch", Float.toString(pos.pitch));
        store(file, props, "RabbitBoss spawn point");
    }

    private void writeArea(File file, Position pos) {
        if (pos == null) {
            return;
        }
        store(file, baseProps(pos), "RabbitBoss area position");
    }

    private void writeIndexed(File file, String prefix, String configPath) {
        Properties props = new Properties();
        for (int i = 1; i <= 5; i++) {
            Position pos = readPosition(configPath + "." + i, false);
            if (pos == null) {
                continue;
            }
            String key = prefix + "." + i;
            props.setProperty(key + ".dimension", pos.worldKey);
            props.setProperty(key + ".x", Double.toString(pos.x));
            props.setProperty(key + ".y", Double.toString(pos.y));
            props.setProperty(key + ".z", Double.toString(pos.z));
            props.setProperty(key + ".yaw", Float.toString(pos.yaw));
        }
        store(file, props, "RabbitBoss " + prefix + " positions");
    }

    private Properties baseProps(Position pos) {
        Properties props = new Properties();
        props.setProperty("dimension", pos.worldKey);
        props.setProperty("x", Double.toString(pos.x));
        props.setProperty("y", Double.toString(pos.y));
        props.setProperty("z", Double.toString(pos.z));
        props.setProperty("yaw", Float.toString(pos.yaw));
        return props;
    }

    private void store(File file, Properties props, String comment) {
        try (FileOutputStream output = new FileOutputStream(file)) {
            props.store(output, comment);
        } catch (IOException e) {
            getLogger().warning("Failed to write " + file.getName() + ": " + e.getMessage());
        }
    }

    private void readLegacySpawn(File file) {
        Properties props = load(file);
        if (props == null) {
            return;
        }
        writeConfigPosition("positions.boss-spawn", props, "", true);
    }

    private void readLegacyArea(File file, String path) {
        Properties props = load(file);
        if (props == null) {
            return;
        }
        writeConfigPosition(path, props, "", false);
    }

    private void readLegacyIndexed(File file, String prefix, String path) {
        Properties props = load(file);
        if (props == null) {
            return;
        }
        for (int i = 1; i <= 5; i++) {
            writeConfigPosition(path + "." + i, props, prefix + "." + i + ".", false);
        }
    }

    private Properties load(File file) {
        if (!file.exists()) {
            return null;
        }
        Properties props = new Properties();
        try (var input = java.nio.file.Files.newInputStream(file.toPath())) {
            props.load(input);
            return props;
        } catch (IOException e) {
            getLogger().warning("Failed to read " + file.getName() + ": " + e.getMessage());
            return null;
        }
    }

    private void writeConfigPosition(String path, Properties props, String prefix, boolean includePitch) {
        String dimension = props.getProperty(prefix + "dimension");
        String x = props.getProperty(prefix + "x");
        String y = props.getProperty(prefix + "y");
        String z = props.getProperty(prefix + "z");
        String yaw = props.getProperty(prefix + "yaw");
        if (dimension == null || x == null || y == null || z == null || yaw == null) {
            return;
        }
        getConfig().set(path + ".world", dimension);
        getConfig().set(path + ".x", Double.parseDouble(x));
        getConfig().set(path + ".y", Double.parseDouble(y));
        getConfig().set(path + ".z", Double.parseDouble(z));
        getConfig().set(path + ".yaw", Double.parseDouble(yaw));
        if (includePitch) {
            getConfig().set(path + ".pitch", Double.parseDouble(props.getProperty(prefix + "pitch", "0.0")));
        }
    }

    private static float snapYaw(float yaw) {
        float snapped = Math.round(yaw / 90.0f) * 90.0f;
        if (snapped <= -180.0f) {
            return 180.0f;
        }
        if (snapped > 180.0f) {
            return snapped - 360.0f;
        }
        return snapped;
    }

    private static Vector2 forward(float yaw) {
        double radians = Math.toRadians(yaw);
        return new Vector2(-Math.sin(radians), Math.cos(radians));
    }

    private static String dimensionName(World world) {
        String name = world.getName();
        if ("world_nether".equals(name)) {
            return "minecraft:the_nether";
        }
        if ("world_the_end".equals(name)) {
            return "minecraft:the_end";
        }
        return "minecraft:overworld";
    }

    private static String worldName(String dimension) {
        if ("minecraft:the_nether".equals(dimension)) {
            return "world_nether";
        }
        if ("minecraft:the_end".equals(dimension)) {
            return "world_the_end";
        }
        return "world";
    }

    private static int parseNumber(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static List<String> partial(String prefix, List<String> values) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return values.stream().filter(value -> value.startsWith(lower)).toList();
    }

    private record Position(String worldKey, double x, double y, double z, float yaw, float pitch) {
    }

    private record Vector2(double x, double z) {
    }
}
