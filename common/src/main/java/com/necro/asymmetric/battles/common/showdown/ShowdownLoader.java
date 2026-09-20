package com.necro.asymmetric.battles.common.showdown;

import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

// Implementation of Mega Showdown's Showdown Loader.
public class ShowdownLoader {
    public void load() {
        AsymmetricBattlesAPI.LOGGER.info("Initiating showdown files");

        Path showdown_sim = Path.of("./showdown/sim");

        try {
            Files.createDirectories(showdown_sim);
            yoink("assets/asymmetricbattles/showdown/battle.js", showdown_sim.resolve("battle.js"));
            yoink("assets/asymmetricbattles/showdown/battle-actions.js", showdown_sim.resolve("battle-actions.js"));
        } catch (IOException e) {
            AsymmetricBattlesAPI.LOGGER.error("Failed to load showdown files: {}", e.getMessage());
        }
    }

    private void yoink(String resourcePath, Path targetPath) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) return;
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
