package com.github.selfcrafted.customlobby.preboot;

import com.github.selfcrafted.customlobby.Server;

import java.io.IOException;

public class OciPreboot {
    public static void main(String[] args) throws IOException {
        // TODO: 06.09.22 execute before starting the server

        System.setProperty("minestom.terminal.disabled", "");

        Server.main(args);
    }
}
