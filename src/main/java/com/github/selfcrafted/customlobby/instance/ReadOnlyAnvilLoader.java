package com.github.selfcrafted.customlobby.instance;

import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ReadOnlyAnvilLoader extends AnvilLoader {
    private static final Path MAP_PATH = Path.of("/tmp", "custom-lobby");

    public ReadOnlyAnvilLoader() {
        super(MAP_PATH);
    }

    @Override
    public void loadInstance(@NotNull Instance instance) {
        if (!MAP_PATH.toFile().exists())
            try {
                MAP_PATH.resolve("region").toFile().mkdirs();
                Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/map/level.dat")),
                        MAP_PATH.resolve("level.dat"), StandardCopyOption.REPLACE_EXISTING);
                Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/map/region/r.0.0.mca")),
                        MAP_PATH.resolve("region/r.0.0.mca"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        super.loadInstance(instance);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveInstance(@NotNull Instance instance) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunk(@NotNull Chunk chunk) {
        return CompletableFuture.completedFuture(null);
    }
}
