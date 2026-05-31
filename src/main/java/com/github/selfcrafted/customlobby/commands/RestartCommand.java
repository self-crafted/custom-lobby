package com.github.selfcrafted.customlobby.commands;

import com.github.selfcrafted.customlobby.Server;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.ServerSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

import java.io.IOException;

public class RestartCommand extends Command {
    RestartCommand() {
        super("restart");
        setCondition(((sender, commandString) -> (sender instanceof ServerSender)
                || (sender instanceof ConsoleSender)
                || ((sender instanceof Player) && ((Player) sender).getPermissionLevel() == 4)
        ));
        addSyntax((sender, context) -> {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    new ProcessBuilder("./start.sh").start();
                    Server.logger().info("Start new server.");
                } catch (IOException e) {
                    if (!(sender instanceof ConsoleSender)) sender.sendMessage("Could not restart server.");
                    Server.logger().error("Could not restart server.", e);
                }
            }, "RestartHook"));
            MinecraftServer.stopCleanly();
        });
    }
}
