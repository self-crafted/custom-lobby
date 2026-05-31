package com.github.selfcrafted.customlobby;

import com.github.selfcrafted.customlobby.commands.Commands;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PlayerBeginItemUseEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.clock.WorldClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class Server {
    private static final String START_SCRIPT_FILENAME = "start.sh";

    private static InstanceContainer LOBBY;
    private static Pos SPAWN;
    private static final Logger serverLogger = LoggerFactory.getLogger(Server.class);

    public static Logger logger() {
        return serverLogger;
    }

    static void main(String[] args) throws IOException {
        System.setProperty("minestom.chunk-view-distance", "2");
        System.setProperty("minestom.entity-view-distance", "2");

        Settings.read();
        if (Settings.isTerminalDisabled()) {
            System.setProperty("minestom.terminal.disabled", "");
        }

        serverLogger.info("====== VERSIONS ======");
        serverLogger.info("Java: {}", Runtime.version());
        serverLogger.info("{}: {}", Versions.implementation(), Versions.version());
        serverLogger.info("Minestom: {}", Versions.minestom());
        serverLogger.info("Supported protocol: {} ({})", MinecraftServer.PROTOCOL_VERSION, MinecraftServer.VERSION_NAME);
        serverLogger.info("======================");

        if (args.length > 0 && args[0].equalsIgnoreCase("-v")) System.exit(0);

        File startScriptFile = new File(START_SCRIPT_FILENAME);
        if (!startScriptFile.exists()) {
            serverLogger.info("Create startup script.");
            Files.copy(
                    Objects.requireNonNull(Server.class.getClassLoader().getResourceAsStream(START_SCRIPT_FILENAME)),
                    startScriptFile.toPath());
            new ProcessBuilder("chmod u+x start.sh").start();
            serverLogger.info("Use './start.sh' to start the server.");
            System.exit(0);
        }

        // Initialise server
        Auth auth;
        switch (Settings.getMode()) {
            case OFFLINE -> auth = new Auth.Offline();
            case ONLINE -> auth = new Auth.Online();
            case BUNGEECORD -> auth = new Auth.Bungee();
            case VELOCITY -> {
                if (!Settings.hasVelocitySecret())
                    throw new IllegalArgumentException("The velocity secret is mandatory.");
                auth = new Auth.Velocity(Settings.getVelocitySecret());
            }
            default -> throw new IllegalArgumentException("Invalid authentication mode value."); // TODO: check in Settings instead of here
        }

        MinecraftServer server = MinecraftServer.init(auth);
        serverLogger.info("Running in {} mode.", Settings.getMode());

        // Create lobby instance
        var fullBrightDimensionType = MinecraftServer.getDimensionTypeRegistry().register("self_crafted:lobby",
                DimensionType.builder().ambientLight(2.0f).defaultClock(WorldClock.THE_END).build());
        LOBBY = MinecraftServer.getInstanceManager().createInstanceContainer(fullBrightDimensionType);
        LOBBY.setChunkLoader(new AnvilLoader("no")); // TODO: implement polar as slime is no longer working

        SPAWN = new Pos(84.8, 61.0, 84.0, -30.3F, 0.0F);
        LOBBY.setTime(-18000L);

        // Fishing Steve
        LivingEntity mannequin = new LivingEntity(EntityType.PLAYER);
        mannequin.setInstance(LOBBY);
        mannequin.setView(164.0F, 40.0F);
        mannequin.setItemInMainHand(ItemStack.of(Material.FISHING_ROD));
        mannequin.setInvisible(false);
        mannequin.setNoGravity(true);

        // Seat
        Entity seat = new Entity(EntityType.BAT);
        seat.setInvisible(true);
        seat.setNoGravity(true);
        seat.setInstance(LOBBY, new Pos(91.0, 60.45, 89.1, 164.0F, 40.0F));
        seat.addPassenger(mannequin);

        // Fishing hook
        Entity hook = new Entity(EntityType.FISHING_BOBBER);
        ((FishingHookMeta) hook.getEntityMeta()).setOwnerEntity(mannequin);
        hook.setNoGravity(true);
        hook.setInstance(LOBBY, new Pos(90.5, 60.875, 87.5));

        var eventNode = MinecraftServer.getGlobalEventHandler();
        eventNode.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(LOBBY);
            event.getPlayer().setRespawnPoint(SPAWN);
        });
        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            var player = event.getPlayer();
            if (event.getInstance() != LOBBY) return;
            player.setGameMode(GameMode.ADVENTURE);
            player.setNoGravity(false);
        });
        eventNode.addListener(PlayerMoveEvent.class, event -> {
            var player = event.getPlayer();
            if (player.getPosition().y() > 0) return;
            player.teleport(SPAWN);
        });

        eventNode.addListener(ItemDropEvent.class, event -> event.setCancelled(true));
        eventNode.addListener(PlayerSwapItemEvent.class, event -> event.setCancelled(true));
        eventNode.addListener(PlayerBeginItemUseEvent.class, event -> event.setCancelled(true));
        eventNode.addListener(PlayerStartDiggingEvent.class, event -> event.setCancelled(true));

        MinecraftServer.getCommandManager().register(Commands.SHUTDOWN);
        MinecraftServer.getCommandManager().register(Commands.RESTART);

        // Start server
        server.start(Settings.getServerIp(), Settings.getServerPort());
        serverLogger.info("Listening on {}:{}", Settings.getServerIp(), Settings.getServerPort());
    }
}
