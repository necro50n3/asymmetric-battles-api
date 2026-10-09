package com.necro.asymmetric.battles.common.registry;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import com.necro.asymmetric.battles.common.api.spawning.BattleSpawnDetail;
import com.necro.asymmetric.battles.common.api.spawning.BattleSpawnPool;
import com.necro.asymmetric.battles.common.util.PropertyExtractors;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SpawnPoolRegistry {
    public final Map<String, List<BattleSpawnPool>> registry = new HashMap<>();

    public void sort() {
        this.registry.values().forEach(list -> list.sort(
            Comparator.comparingInt((BattleSpawnPool p) -> p.pokemon.trim().split(" ").length).reversed()
        ));
    }

    public void register(String species, BattleSpawnPool pool) {
        this.registry.computeIfAbsent(species, key -> new ArrayList<>()).add(pool);
    }

    public void register(PokemonProperties properties, BattleSpawnDetail detail, Class<? extends BattleSpawnPool> cls) {
        String species = properties.getSpecies() != null && properties.getSpecies().contains(":") ? properties.getSpecies().split(":")[1] : properties.getSpecies();
        BattleSpawnPool result = species == null ? null : this.registry.computeIfAbsent(species, key -> new ArrayList<>())
            .stream()
            .filter(pool -> pool.pokemon.equals(properties.getOriginalString()))
            .findFirst().orElse(null);
        if (result != null) result.spawns.add(detail);
        else {
            try {
                BattleSpawnPool pool = cls.getDeclaredConstructor().newInstance();
                pool.pokemon = properties.getOriginalString();
                pool.spawns.add(detail);
                this.register(pool.species(), pool);
            }
            catch (Exception e) {
                AsymmetricBattlesAPI.LOGGER.error("Unable to create a spawn pool for: {}", detail.pokemon, e);
            }
        }
    }

    public @Nullable BattleSpawnPool get(PokemonEntity pokemonEntity) {
        return this.get(pokemonEntity.getPokemon());
    }

    public @Nullable BattleSpawnPool get(Pokemon pokemon) {
        PokemonProperties properties = pokemon.createPokemonProperties(PropertyExtractors.LONG_EXTRACTOR);
        properties.setAspects(pokemon.getAspects());
        return this.get(properties);
    }

    public @Nullable BattleSpawnPool get(PokemonProperties properties) {
        String species = properties.getSpecies() != null && properties.getSpecies().contains(":") ? properties.getSpecies().split(":")[1] : properties.getSpecies();
        if (species == null) return null;
        List<BattleSpawnPool> pools = this.registry.get(species);
        if (pools == null) return null;
        return pools.stream().filter(pool -> pool.isSatisfiedBy(properties)).findFirst().orElse(null);
    }
}
