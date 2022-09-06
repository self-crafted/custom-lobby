package com.github.selfcrafted.customlobby.oci;

import com.github.selfcrafted.customlobby.Server;
import net.minestom.server.MinecraftServer;

import java.io.IOException;

public class Preboot {
    public static void main(String[] args) throws IOException {
        MinecraftServer.LOGGER.info("====== VERSIONS ======");
        MinecraftServer.LOGGER.info("Java: " + Runtime.version());
        MinecraftServer.LOGGER.info("&Name: " + Server.VERSION);
        MinecraftServer.LOGGER.info("Minestom: " + Server.MINESTOM_VERSION);
        MinecraftServer.LOGGER.info("Supported protocol: %d (%s)".formatted(MinecraftServer.PROTOCOL_VERSION, MinecraftServer.VERSION_NAME));
        MinecraftServer.LOGGER.info("======================");

        EnvironmentSettings settings = new EnvironmentSettings();
        settings.read();
        System.setProperty("minestom.terminal.disabled", "");

        Server.start(settings);
    }
}
