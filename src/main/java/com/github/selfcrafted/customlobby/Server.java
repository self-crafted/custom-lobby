package com.github.selfcrafted.customlobby;

import com.github.selfcrafted.customlobby.commands.Commands;
import gg.astromc.slimeloader.loader.SlimeLoader;
import gg.astromc.slimeloader.source.SlimeSource;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.fakeplayer.FakePlayer;
import net.minestom.server.entity.fakeplayer.FakePlayerOption;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.scoreboard.TeamBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

public class Server {
    public static final String VERSION = "&version";
    public static final String MINESTOM_VERSION = "&minestomVersion";
    private static final String START_SCRIPT_FILENAME = "start.sh";

    private static InstanceContainer INSTANCE;
    private static Pos SPAWN;

    private static Team NO_NAMES_TEAM;

    public static void main(String[] args) throws IOException {
        System.setProperty("minestom.tps", "5");
        System.setProperty("minestom.chunk-view-distance", "2");
        System.setProperty("minestom.entity-view-distance", "2");

        Settings.read();
        if (Settings.isTerminalDisabled())
            System.setProperty("minestom.terminal.disabled", "");

        MinecraftServer.LOGGER.info("====== VERSIONS ======");
        MinecraftServer.LOGGER.info("Java: " + Runtime.version());
        MinecraftServer.LOGGER.info("&Name: " + VERSION);
        MinecraftServer.LOGGER.info("Minestom: " + MINESTOM_VERSION);
        MinecraftServer.LOGGER.info("Supported protocol: %d (%s)".formatted(MinecraftServer.PROTOCOL_VERSION, MinecraftServer.VERSION_NAME));
        MinecraftServer.LOGGER.info("======================");

        if (args.length > 0 && args[0].equalsIgnoreCase("-v")) System.exit(0);

        File startScriptFile = new File(START_SCRIPT_FILENAME);
        if (!startScriptFile.exists()) {
            MinecraftServer.LOGGER.info("Create startup script.");
            Files.copy(
                    Objects.requireNonNull(Server.class.getClassLoader().getResourceAsStream(START_SCRIPT_FILENAME)),
                    startScriptFile.toPath());
            Runtime.getRuntime().exec("chmod u+x start.sh");
            MinecraftServer.LOGGER.info("Use './start.sh' to start the server.");
            System.exit(0);
        }

        // Initialise server
        MinecraftServer server = MinecraftServer.init();

        // Create lobby instance
        INSTANCE = MinecraftServer.getInstanceManager().createInstanceContainer();
        INSTANCE.setChunkLoader(new SlimeLoader(INSTANCE, new SlimeSource() {
            @NotNull
            @Override
            public InputStream load() {
                return Objects.requireNonNull(getClass().getResourceAsStream("/lobby.slime"),
                        "Slime world missing!");
            }

            @NotNull
            @Override
            public OutputStream save() {
                return null;
            }
        }, true));

        SPAWN = new Pos(86, 61, 84);
        INSTANCE.setTimeRate(0);
        INSTANCE.setTime(-6000L);

        NO_NAMES_TEAM = new TeamBuilder("noNames", MinecraftServer.getTeamManager())
                .nameTagVisibility(TeamsPacket.NameTagVisibility.NEVER).build();

        FakePlayer.initPlayer(UUID.fromString("00000000-0000-4000-8000-000000000000"), "Fishing Steve",
                new FakePlayerOption().setInTabList(false).setRegistered(false),
                player -> {
                    var playerPos = new Pos(91.0, 61.0, 89.1, 164.0F, 40.0F);
                    player.setTeam(NO_NAMES_TEAM);
                    player.setView(playerPos.yaw(), playerPos.pitch());
                    player.getInventory().setItemInMainHand(ItemStack.of(Material.FISHING_ROD));

                    // Seat
                    Entity seat = new Entity(EntityType.BAT);
                    seat.getEntityMeta().setInvisible(true);
                    seat.setInstance(INSTANCE, playerPos.sub(0, 0.55, 0));
                    seat.addPassenger(player);

                    // Fishing hook
                    Entity hook = new Entity(EntityType.FISHING_BOBBER);
                    ((FishingHookMeta) hook.getEntityMeta()).setOwnerEntity(player);
                    hook.setNoGravity(true);
                    hook.setInstance(INSTANCE, player.getTargetBlockPosition(5)
                            .add(new Pos(0.5, 0.0, 0.5)));
                });

        var eventNode = MinecraftServer.getGlobalEventHandler();
        eventNode.addListener(PlayerLoginEvent.class, event -> {
            event.setSpawningInstance(INSTANCE);
            event.getPlayer().setRespawnPoint(SPAWN);
        });
        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            if (event.getSpawnInstance() != INSTANCE) return;
            if (event.getPlayer() instanceof FakePlayer) return;
            event.getPlayer().setGameMode(GameMode.ADVENTURE);
        });
        eventNode.addListener(EntityDamageEvent.class, event -> {
            if (event.getDamageType() != DamageType.VOID) return;
            if (event.getEntity() instanceof Player player) {
                player.teleport(SPAWN);
                event.setCancelled(true);
            }
        });

        eventNode.addListener(ItemDropEvent.class, event -> event.setCancelled(true));
        eventNode.addListener(PlayerSwapItemEvent.class, event -> event.setCancelled(true));
        eventNode.addListener(PlayerPreEatEvent.class, event -> event.setCancelled(true));
        eventNode.addListener(PlayerStartDiggingEvent.class, event -> event.setCancelled(true));

        MinecraftServer.getCommandManager().register(Commands.SHUTDOWN);
        MinecraftServer.getCommandManager().register(Commands.RESTART);
        MinecraftServer.getExtensionManager().setExtensionDataRoot(Path.of("config"));

        switch (Settings.getMode()) {
            case OFFLINE -> {}
            case ONLINE -> MojangAuth.init();
            case BUNGEECORD -> BungeeCordProxy.enable();
            case VELOCITY -> {
                if (!Settings.hasVelocitySecret())
                    throw new IllegalArgumentException("The velocity secret is mandatory.");
                VelocityProxy.enable(Settings.getVelocitySecret());
            }
        }

        MinecraftServer.LOGGER.info("Running in " + Settings.getMode() + " mode.");
        MinecraftServer.LOGGER.info("Listening on " + Settings.getServerIp() + ":" + Settings.getServerPort());

        // Start server
        server.start(Settings.getServerIp(), Settings.getServerPort());
    }
}