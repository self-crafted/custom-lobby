package com.github.selfcrafted.customlobby;

import java.io.*;

public interface Settings {
    enum RunMode {
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

    void read();

    void write() throws IOException;

    RunMode getMode();

    String getServerIp();
    int getServerPort();

    boolean hasVelocitySecret();

    String getVelocitySecret();

    boolean isTerminalDisabled();
}
