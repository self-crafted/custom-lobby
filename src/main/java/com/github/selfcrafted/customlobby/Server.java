package com.github.selfcrafted.customlobby;

import net.hollowcube.polar.PolarLoader;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PlayerBeginItemUseEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.attribute.EnvironmentAttribute;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

public class Server {
    private static InstanceContainer LOBBY;
    private static Pos SPAWN;
    private static final Logger serverLogger = LoggerFactory.getLogger(Server.class);

    public static Logger logger() {
        return serverLogger;
    }

    static void main(String[] args) throws IOException {
        System.setProperty("minestom.chunk-view-distance", "2");
        System.setProperty("minestom.entity-view-distance", "2");

        serverLogger.info("====== VERSIONS ======");
        serverLogger.info("Java: {}", Runtime.version());
        serverLogger.info("{}: {}", Versions.implementation(), Versions.version());
        serverLogger.info("Minestom: {}", Versions.minestom());
        serverLogger.info("Supported protocol: {} ({})", MinecraftServer.PROTOCOL_VERSION, MinecraftServer.VERSION_NAME);
        serverLogger.info("======================");

        if (args.length > 0 && args[0].equalsIgnoreCase("-v")) System.exit(0);

        Settings.read();

        // Initialise server
        Auth auth = null;
        switch (Settings.getMode()) {
            case OFFLINE -> auth = new Auth.Offline();
            case ONLINE -> auth = new Auth.Online();
            case BUNGEECORD -> {
                if (!Settings.hasSecret()) auth = new Auth.Bungee();
                else auth = new Auth.Bungee(Set.of(Settings.getSecret()));
            }
            case VELOCITY -> auth = new Auth.Velocity(Settings.getSecret());
        }

        MinecraftServer server = MinecraftServer.init(auth);
        serverLogger.info("Running in {} mode.", Settings.getMode());

        // Create lobby instance
        var fullBrightDimensionType = MinecraftServer.getDimensionTypeRegistry().register("self_crafted:lobby",
                DimensionType.builder()
                        .fixedTime(true).skybox(DimensionType.Skybox.OVERWORLD)
                        .setAttribute(EnvironmentAttribute.AMBIENT_LIGHT_COLOR, NamedTextColor.WHITE)
                        .setAttribute(EnvironmentAttribute.SUN_ANGLE, 180f)
                        .setAttribute(EnvironmentAttribute.STAR_BRIGHTNESS, 0.85f)
                        .build());
        LOBBY = MinecraftServer.getInstanceManager().createInstanceContainer(fullBrightDimensionType);
        LOBBY.setChunkLoader(new PolarLoader(Objects.requireNonNull(
                Server.class.getResourceAsStream("/lobby.polar"), "Polar world missing!")));

        SPAWN = new Pos(84.8, 61.0, 84.0, -30.3F, 0.0F);
        LOBBY.setTime(-18000L);

        var eventNode = MinecraftServer.getGlobalEventHandler();
        EventListener<AsyncPlayerPreLoginEvent> firstLoginListener = new EventListener<>() {
            @Override
            public @NonNull Class<AsyncPlayerPreLoginEvent> eventType() {
                return AsyncPlayerPreLoginEvent.class;
            }

            @Override
            public @NonNull Result run(@NonNull AsyncPlayerPreLoginEvent event) {
                eventNode.removeListener(this);

                // Fishing Steve
                LivingEntity mannequin = new LivingEntity(EntityType.MANNEQUIN);
                mannequin.setInstance(LOBBY, new Pos(91.0, 60.45, 89.1, 164.0F, 40.0F));
                mannequin.setView(164.0F, 40.0F);
                mannequin.setItemInMainHand(ItemStack.of(Material.FISHING_ROD));
                mannequin.setInvisible(false);
                mannequin.setNoGravity(true);
                mannequin.setAutoViewable(true);

                // Seat
                Entity seat = new Entity(EntityType.BAT);
                seat.setInstance(LOBBY, new Pos(91.0, 60.15, 89.1, 164.0F, 40.0F));
                seat.setInvisible(true);
                seat.setNoGravity(true);
                seat.setAutoViewable(true);
                seat.addPassenger(mannequin);

                // Fishing hook
                Entity hook = new Entity(EntityType.FISHING_BOBBER);
                hook.editEntityMeta(FishingHookMeta.class, meta -> meta.setOwnerEntity(mannequin));
                hook.setInstance(LOBBY, new Pos(90.5, 60.875, 87.5));
                hook.setInvisible(false);
                hook.setAutoViewable(true);
                hook.spawn();

                // Hook holder
                Entity bobberHolder = new Entity(EntityType.BAT);
                bobberHolder.setInstance(LOBBY, new Pos(90.5, 60.875, 87.5));
                bobberHolder.setInvisible(true);
                bobberHolder.setNoGravity(true);
                bobberHolder.setAutoViewable(true);
                bobberHolder.addPassenger(hook);

                return Result.SUCCESS;
            }
        };

        eventNode.addListener(firstLoginListener);
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

        // Start server
        server.start(Settings.getServerIp(), Settings.getServerPort());
        serverLogger.info("Listening on {}:{}", Settings.getServerIp(), Settings.getServerPort());
    }
}
