package com.github.selfcrafted.customlobby;

import com.github.selfcrafted.customlobby.commands.Commands;
import com.github.selfcrafted.customlobby.instance.ReadOnlyAnvilLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.TagSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Server {
    public static final String VERSION = "&version";
    public static final String MINESTOM_VERSION = "&minestomVersion";
    private static final String START_SCRIPT_FILENAME = "start.sh";

    private static Instance INSTANCE;
    private static Pos SPAWN;

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
        INSTANCE = MinecraftServer.getInstanceManager().createInstanceContainer(new ReadOnlyAnvilLoader());

        TagHandler data = TagHandler.fromCompound(INSTANCE.getTag(Tag.Structure("Data", TagSerializer.COMPOUND)));
        MinecraftServer.LOGGER.info(data.asCompound().toSNBT());
        SPAWN = new Pos(data.getTag(Tag.Integer("SpawnX")),
                data.getTag(Tag.Integer("SpawnY")) + 1,
                data.getTag(Tag.Integer("SpawnZ")));

        TagHandler gameRules = TagHandler.fromCompound(data.getTag(Tag.Structure("GameRules", TagSerializer.COMPOUND)));
        if ("false".equals(gameRules.getTag(Tag.String("doDaylightCycle")))) {
            INSTANCE.setTimeRate(0);
            INSTANCE.setTime(-data.getTag(Tag.Long("Time")));
        } else INSTANCE.setTime(data.getTag(Tag.Long("Time")));

        var eventNode = MinecraftServer.getGlobalEventHandler();
        eventNode.addListener(PlayerLoginEvent.class, event -> {
            event.setSpawningInstance(INSTANCE);
            event.getPlayer().setRespawnPoint(SPAWN);
        });

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