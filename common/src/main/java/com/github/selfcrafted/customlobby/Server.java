package com.github.selfcrafted.customlobby;

import dev.emortal.tnt.TNTLoader;
import dev.emortal.tnt.source.TNTSource;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.fakeplayer.FakePlayer;
import net.minestom.server.entity.fakeplayer.FakePlayerOption;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.TeamBuilder;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

public class Server {
    public static final String VERSION = "&version";
    public static final String MINESTOM_VERSION = "&minestomVersion";

    private static InstanceContainer LOBBY;
    private static Pos SPAWN;

    public static void start(Settings settings, MinecraftServer server) {
        var fullBrightDimensionType = DimensionType.builder(NamespaceID.from("selfcrafted:lobby"))
                .ambientLight(2.0f)
                .build();
        MinecraftServer.getDimensionTypeManager().addDimension(fullBrightDimensionType);

        // Create lobby instance
        LOBBY = MinecraftServer.getInstanceManager().createInstanceContainer(fullBrightDimensionType);
        LOBBY.setChunkLoader(new TNTLoader(LOBBY, new TNTSource() {
            @Override
            public @NotNull InputStream load() {
                return Objects.requireNonNull(getClass().getResourceAsStream("/lobby.tnt"),
                        "TNT world missing!");
            }

            @Override
            public void save(byte[] bytes) { }
        }, Pos.ZERO));

        SPAWN = new Pos(84.8, 61.0, 84.0, -30.3F, 0.0F);
        LOBBY.setTimeRate(0);
        LOBBY.setTime(-18000L);

        FakePlayer.initPlayer(UUID.fromString("00000000-0000-4000-8000-000000000000"), "Fishing Steve",
                new FakePlayerOption().setInTabList(false).setRegistered(false),
                player -> {
                    // Fishing Steve
                    var team = new TeamBuilder("noNames", MinecraftServer.getTeamManager())
                            .nameTagVisibility(TeamsPacket.NameTagVisibility.NEVER).build();
                    player.setTeam(team);
                    player.setView(164.0F, 40.0F);
                    player.getInventory().setItemInMainHand(ItemStack.of(Material.FISHING_ROD));

                    // Seat
                    Entity seat = new Entity(EntityType.BAT);
                    seat.getEntityMeta().setInvisible(true);
                    seat.getEntityMeta().setHasNoGravity(true);
                    seat.setInstance(LOBBY, new Pos(91.0, 60.45, 89.1, 164.0F, 40.0F));
                    seat.addPassenger(player);

                    // Fishing hook
                    Entity hook = new Entity(EntityType.FISHING_BOBBER);
                    ((FishingHookMeta) hook.getEntityMeta()).setOwnerEntity(player);
                    hook.setNoGravity(true);
                    hook.setInstance(LOBBY, new Pos(90.5, 60.875, 87.5));
                });

        var eventNode = MinecraftServer.getGlobalEventHandler();
        eventNode.addListener(PlayerLoginEvent.class, event -> {
            event.setSpawningInstance(LOBBY);
            event.getPlayer().setRespawnPoint(SPAWN);
        });
        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            var player = event.getPlayer();
            if (event.getSpawnInstance() != LOBBY) return;
            if (player instanceof FakePlayer) return;
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
        eventNode.addListener(PlayerPreEatEvent.class, event -> event.setCancelled(true));
        eventNode.addListener(PlayerStartDiggingEvent.class, event -> event.setCancelled(true));

        MinecraftServer.getExtensionManager().setExtensionDataRoot(Path.of("config"));
        MinecraftServer.setBrandName("custom-lobby");

        switch (settings.getMode()) {
            case OFFLINE -> {}
            case ONLINE -> MojangAuth.init();
            case BUNGEECORD -> BungeeCordProxy.enable();
            case VELOCITY -> {
                if (!settings.hasVelocitySecret())
                    throw new IllegalArgumentException("The velocity secret is mandatory.");
                VelocityProxy.enable(settings.getVelocitySecret());
            }
        }

        MinecraftServer.LOGGER.info("Running in " + settings.getMode() + " mode.");
        MinecraftServer.LOGGER.info("Listening on " + settings.getServerIp() + ":" + settings.getServerPort());

        // Start server
        server.start(settings.getServerIp(), settings.getServerPort());
    }

    /**
     * @return The InstanceContainer of the lobby instance
     */
    public static InstanceContainer getLobby() {
        return LOBBY;
    }

    /**
     * @return The spawn point for all players in the lobby
     */
    public static Pos getSpawn() {
        return SPAWN;
    }
}