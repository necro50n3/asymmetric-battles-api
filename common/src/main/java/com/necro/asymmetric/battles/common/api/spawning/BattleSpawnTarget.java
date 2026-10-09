package com.necro.asymmetric.battles.common.api.spawning;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import org.jetbrains.annotations.NotNull;

public class BattleSpawnTarget {
    public String pokemon = "";
    protected transient PokemonProperties properties = null;
    protected transient String species = null;

    public @NotNull String species() {
        if (this.species != null) return this.species;
        this.species = this.pokemon.split(" ")[0];
        return this.species;
    }

    public @NotNull PokemonProperties properties() {
        if (this.properties == null) this.properties = PokemonProperties.Companion.parse(this.pokemon);
        return this.properties;
    }
}
