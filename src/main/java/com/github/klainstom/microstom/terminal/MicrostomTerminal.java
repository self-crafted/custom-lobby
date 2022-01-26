package com.github.klainstom.microstom.terminal;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.CommandResult;
import org.jetbrains.annotations.ApiStatus;

import org.jline.reader.*;
import org.jline.reader.impl.DefaultExpander;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

public class MicrostomTerminal {
    private static CommandManager COMMAND_MANAGER;
    private static final String PROMPT = "> ";

    private static volatile Terminal terminal;
    private static volatile LineReader lineReader;
    private static final Highlighter highlighter = new MicrostomHighlighter();
    private static final Completer completer = new MicrostomCompleter();
    private static final Expander expander = new DefaultExpander();
    private static final History history = new DefaultHistory();
    private static volatile boolean running = false;

    @ApiStatus.Internal
    public static void start() {
        COMMAND_MANAGER = MinecraftServer.getCommandManager();
        final Thread thread = new Thread(null, () -> {
            try {
                terminal = TerminalBuilder.terminal();
            } catch (IOException e) {
                e.printStackTrace();
            }
            lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .highlighter(highlighter)
                    .completer(completer)
                    .expander(expander)
                    .history(history)
                    .build();
            running = true;

            while (running) {
                String command;
                try {
                    command = lineReader.readLine(PROMPT);
                    if (command.isBlank()) continue;
                    CommandResult result = COMMAND_MANAGER.execute(COMMAND_MANAGER.getConsoleSender(), command);
                    switch (result.getType()) {
                        case UNKNOWN -> print("Unknown command.");
                        case INVALID_SYNTAX -> print("Invalid syntax: " + result.getInput());
                        case CANCELLED -> print("Execution got cancelled.");
                        case SUCCESS -> print("Execution succeeded.");
                    }
                } catch (UserInterruptException ignore) {
                } catch (EndOfFileException e) {
                    return;
                }
            }
        }, "Jline");
        thread.setDaemon(true);
        thread.start();
    }

    @ApiStatus.Internal
    public static void stop() {
        running = false;
        if (terminal != null) {
            try {
                terminal.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void print(String line) {
        lineReader.printAbove(line);
    }

    public static boolean isRunning() { return running; }
}
