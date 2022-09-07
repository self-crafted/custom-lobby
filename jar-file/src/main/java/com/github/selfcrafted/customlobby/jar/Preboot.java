package com.github.selfcrafted.customlobby.jar;

import com.github.selfcrafted.customlobby.Server;
import com.github.selfcrafted.customlobby.Settings;
import net.minestom.server.MinecraftServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class Preboot {
    private static final String START_SCRIPT_FILENAME = "start.sh";

    public static void main(String[] args) throws IOException {
        Settings settings = new JsonSettings();
        settings.read();
        if (settings.isTerminalDisabled())
            System.setProperty("minestom.terminal.disabled", "");

        System.setProperty("minestom.chunk-view-distance", "2");
        System.setProperty("minestom.entity-view-distance", "2");

        MinecraftServer.LOGGER.info("====== VERSIONS ======");
        MinecraftServer.LOGGER.info("Java: " + Runtime.version());
        MinecraftServer.LOGGER.info("&Name: " + Server.VERSION);
        MinecraftServer.LOGGER.info("Minestom: " + Server.MINESTOM_VERSION);
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

        Server.start(settings);
    }
}
