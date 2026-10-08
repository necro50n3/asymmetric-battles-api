package com.necro.asymmetric.battles.common.registry;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import com.necro.asymmetric.battles.common.api.spawning.BattleSpawnDetail;
import com.necro.asymmetric.battles.common.api.spawning.BattleSpawnPool;
import com.necro.asymmetric.battles.common.util.PropertyExtractors;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SpawnPoolRegistry {
    private final Map<String, List<BattleSpawnPool>> registry = new HashMap<>();

    public void register(String species, BattleSpawnPool pool) {
        this.registry.computeIfAbsent(species, key -> new ArrayList<>()).add(pool);
    }

    public void register(PokemonProperties properties, BattleSpawnDetail detail, Class<? extends BattleSpawnPool> cls) {
        BattleSpawnPool result = this.get(properties);
        if (result != null) result.spawns.add(detail);
        else {
            try {
                BattleSpawnPool pool = cls.getDeclaredConstructor().newInstance();
                pool.pokemon = properties;
                pool.spawns.add(detail);
                this.register(detail.species(), pool);
            }
            catch (Exception e) {
                AsymmetricBattlesAPI.LOGGER.error("Unable to create a spawn pool for: {}", detail.pokemon.getOriginalString(), e);
            }
        }
    }

    public @Nullable BattleSpawnPool get(PokemonEntity pokemonEntity) {
        return this.get(pokemonEntity.getPokemon());
    }

    public @Nullable BattleSpawnPool get(Pokemon pokemon) {
        return this.get(pokemon.createPokemonProperties(PropertyExtractors.SHORT_EXTRACTOR));
    }

    public @Nullable BattleSpawnPool get(PokemonProperties properties) {
        return this.registry.computeIfAbsent(properties.getSpecies(), key -> new ArrayList<>())
            .stream()
            .filter(pool -> pool.pokemon.getOriginalString().equals(properties.getOriginalString()))
            .findFirst().orElse(null);
    }
}
