package com.necro.asymmetric.battles.common.registry;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import com.necro.asymmetric.battles.common.api.spawning.BattleSpawnDetail;
import com.necro.asymmetric.battles.common.api.spawning.BattleSpawnPool;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SpawnRegistry {
    private static final Map<String, Map<String, BattleSpawnPool>> SPAWN_POOL_REGISTRY = new HashMap<>();

    public static void register(String type, BattleSpawnPool pool) {
        SPAWN_POOL_REGISTRY.computeIfAbsent(type, key -> new HashMap<>()).put(pool.species(), pool);
    }

    public static void register(String type, BattleSpawnDetail detail, Class<? extends BattleSpawnPool> cls) {
        BattleSpawnPool result = SPAWN_POOL_REGISTRY.computeIfAbsent(type, key -> new HashMap<>()).computeIfAbsent(detail.species(), key -> {
            try {
                BattleSpawnPool pool = cls.getDeclaredConstructor().newInstance();
                pool.pokemon.setSpecies(detail.species());
                return pool;
            }
            catch (Exception e) {
                AsymmetricBattlesAPI.LOGGER.error("Unable to create a spawn pool for: {}", detail.pokemon.getOriginalString(), e);
                return null;
            }
        });
        if (result != null) result.spawns.add(detail);
    }

    public static @Nullable BattleSpawnPool get(String type, Pokemon pokemon) {
        return get(type, pokemon.getSpecies().getResourceIdentifier().getPath());
    }

    public static @Nullable BattleSpawnPool get(String type, PokemonEntity pokemonEntity) {
        return get(type, pokemonEntity.getPokemon());
    }

    public static @Nullable BattleSpawnPool get(String type, PokemonProperties properties) {
        return get(type, properties.getSpecies());
    }

    public static @Nullable BattleSpawnPool get(String type, String species) {
        Map<String, BattleSpawnPool> typeMap = SPAWN_POOL_REGISTRY.get(type);
        if (typeMap == null || species == null) return null;
        return typeMap.get(species);
    }
}
