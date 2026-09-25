package com.necro.asymmetric.battles.common.showdown;

import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

// Implementation of Mega Showdown's Showdown Loader.
public abstract class ShowdownLoader {
    protected abstract boolean isMegaShowdownLoaded();
    protected abstract boolean isGenesisFormsLoaded();

    public void load() {
        AsymmetricBattlesAPI.LOGGER.info("Initiating showdown files");

        Path showdown_sim = Path.of("./showdown/sim");

        String battleJs = "assets/asymmetricbattles/showdown/battle.js";
        String sideJs = "assets/asymmetricbattles/showdown/side.js";
        String teamsJs = "assets/asymmetricbattles/showdown/teams.js";
        String pokemonJs = "assets/asymmetricbattles/showdown/pokemon.js";
        if (this.isMegaShowdownLoaded()) {
            battleJs = "assets/asymmetricbattles/showdown/mega_showdown/battle.js";
            sideJs = "assets/asymmetricbattles/showdown/mega_showdown/side.js";
            pokemonJs = "assets/asymmetricbattles/showdown/mega_showdown/pokemon.js";
        }
        else if (this.isGenesisFormsLoaded()) {
            sideJs = "assets/asymmetricbattles/showdown/genesisforms/side.js";
        }

        try {
            Files.createDirectories(showdown_sim);
            yoink(battleJs, showdown_sim.resolve("battle.js"));
            yoink(sideJs, showdown_sim.resolve("side.js"));
            yoink(teamsJs, showdown_sim.resolve("teams.js"));
            yoink(pokemonJs, showdown_sim.resolve("pokemon.js"));
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
