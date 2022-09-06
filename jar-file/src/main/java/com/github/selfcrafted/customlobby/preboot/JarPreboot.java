package com.github.selfcrafted.customlobby.preboot;

import com.github.selfcrafted.customlobby.Server;
import com.github.selfcrafted.customlobby.Settings;
import net.minestom.server.MinecraftServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class JarPreboot {
    private static final String START_SCRIPT_FILENAME = "start.sh";

    public static void main(String[] args) throws IOException {
        // TODO: 06.09.22 execute before starting the server

        Settings.read();
        if (Settings.isTerminalDisabled())
            System.setProperty("minestom.terminal.disabled", "");

        File startScriptFile = new File(START_SCRIPT_FILENAME);
        if (!startScriptFile.exists() && !System.getenv("IS_CONTAINER").equals("true")) {
            MinecraftServer.LOGGER.info("Create startup script.");
            Files.copy(
                    Objects.requireNonNull(Server.class.getClassLoader().getResourceAsStream(START_SCRIPT_FILENAME)),
                    startScriptFile.toPath());
            Runtime.getRuntime().exec("chmod u+x start.sh");
            MinecraftServer.LOGGER.info("Use './start.sh' to start the server.");
            System.exit(0);
        }

        Server.main(args);
    }
}
