package com.github.selfcrafted.customlobby.oci;

import com.github.selfcrafted.customlobby.Settings;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentSettings implements Settings {
    private Map<String, String> env = new HashMap<>();

    public void read() {
        env = System.getenv();
    }

    public void write() { }

    public RunMode getMode() { return RunMode.valueOf(env.getOrDefault("RUN_MODE", "OFFLINE")); }

    public String getServerIp() {
        return System.getProperty("server.ip", env.getOrDefault("SERVER_IP", "localhost"));
    }
    public int getServerPort() {
        return Integer.parseInt(env.getOrDefault("SERVER_PORT", "25565"));
    }

    public boolean hasVelocitySecret() {
        return !env.getOrDefault("VELOCITY_SECRET", "").isBlank();
    }

    public String getVelocitySecret() {
        return env.get("VELOCITY_SECRET");
    }

    public boolean isTerminalDisabled() { return true; }
}
