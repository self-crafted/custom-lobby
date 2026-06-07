package com.github.selfcrafted.customlobby;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

public class Settings {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();
    private static final File settingsFile = new File("./config.json");

    private static SettingsState currentSettings = null;

    public static void read() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(settingsFile));
            currentSettings = gson.fromJson(reader, SettingsState.class);
        } catch (FileNotFoundException e) {
            currentSettings = new SettingsState();
            try {
                write();
            } catch (IOException ex) {
                Server.logger().error("Could not create settings file.", ex);
            }
        }
        if (currentSettings.MODE == RunMode.VELOCITY && currentSettings.SECRET.isBlank())
            throw new IllegalArgumentException("The velocity secret is mandatory.");
    }

    public static void write() throws IOException {
        String json = gson.toJson(currentSettings);
        Writer writer = new FileWriter(settingsFile);
        writer.write(json);
        writer.close();
    }

    private static class SettingsState {
        private final String SERVER_IP;
        private final int SERVER_PORT;

        private final RunMode MODE;
        private final String SECRET;

        private SettingsState() {
            this.SERVER_IP = "localhost";
            this.SERVER_PORT = 25565;

            this.MODE = RunMode.OFFLINE;
            this.SECRET = "";
        }

    }

    public enum RunMode {
        OFFLINE("offline"),
        ONLINE("online"),
        BUNGEECORD("BungeeCord"),
        VELOCITY("Velocity");

        private final String name;

        RunMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public static RunMode getMode() { return currentSettings.MODE; }

    public static String getServerIp() {
        return System.getProperty("server.ip", currentSettings.SERVER_IP);
    }
    public static int getServerPort() {
        int port = Integer.getInteger("server.port", currentSettings.SERVER_PORT);
        if (port < 1) return 25565;
        return port;
    }

    public static boolean hasSecret() {
        return !currentSettings.SECRET.isBlank();
    }

    public static String getSecret() {
        return currentSettings.SECRET;
    }
}
