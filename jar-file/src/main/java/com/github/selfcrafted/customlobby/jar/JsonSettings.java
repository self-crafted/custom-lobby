package com.github.selfcrafted.customlobby.jar;

import com.github.selfcrafted.customlobby.Settings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

public class JsonSettings implements Settings {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();
    private static final File settingsFile = new File("./config/custom-lobby.json");

    private static SettingsState currentSettings = null;

    public void read() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(settingsFile));
            currentSettings = gson.fromJson(reader, SettingsState.class);
        } catch (FileNotFoundException e) {
            currentSettings = new SettingsState();
            try {
                write();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void write() throws IOException {
        String json = gson.toJson(currentSettings);
        settingsFile.getParentFile().mkdirs();
        Writer writer = new FileWriter(settingsFile);
        writer.write(json);
        writer.close();
    }

    private static class SettingsState {
        private final String SERVER_IP;
        private final int SERVER_PORT;

        private final RunMode MODE;
        private final String VELOCITY_SECRET;

        // JVM arguments
        private final boolean TERMINAL_DISABLED;

        private SettingsState() {
            this.SERVER_IP = "localhost";
            this.SERVER_PORT = 25565;

            this.MODE = RunMode.OFFLINE;
            this.VELOCITY_SECRET = "";

            this.TERMINAL_DISABLED = false;
        }

    }

    public RunMode getMode() { return currentSettings.MODE; }

    public String getServerIp() {
        return System.getProperty("server.ip", currentSettings.SERVER_IP);
    }
    public int getServerPort() {
        int port = Integer.getInteger("server.port", currentSettings.SERVER_PORT);
        if (port < 1) return 25565;
        return port;
    }

    public boolean hasVelocitySecret() {
        return !currentSettings.VELOCITY_SECRET.isBlank();
    }

    public String getVelocitySecret() {
        return currentSettings.VELOCITY_SECRET;
    }

    public boolean isTerminalDisabled() { return currentSettings.TERMINAL_DISABLED; }
}
