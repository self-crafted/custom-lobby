package com.github.selfcrafted.customlobby.oci;

import com.github.selfcrafted.customlobby.Server;
import com.github.selfcrafted.customlobby.commands.Commands;
import net.minestom.server.MinecraftServer;

public class Preboot {
    public static void main(String[] args) {
        System.setProperty("minestom.chunk-view-distance", "2");
        System.setProperty("minestom.entity-view-distance", "2");
        System.setProperty("minestom.terminal.disabled", "");

        MinecraftServer.LOGGER.info("====== VERSIONS ======");
        MinecraftServer.LOGGER.info("Java: " + Runtime.version());
        MinecraftServer.LOGGER.info("&Name: " + Server.VERSION);
        MinecraftServer.LOGGER.info("Minestom: " + Server.MINESTOM_VERSION);
        MinecraftServer.LOGGER.info("Supported protocol: %d (%s)".formatted(MinecraftServer.PROTOCOL_VERSION, MinecraftServer.VERSION_NAME));
        MinecraftServer.LOGGER.info("======================");

        EnvironmentSettings settings = new EnvironmentSettings();
        settings.read();

        // Initialise server
        MinecraftServer server = MinecraftServer.init();

        MinecraftServer.getCommandManager().register(Commands.SHUTDOWN);

        Server.start(settings, server);
    }
}
