package com.necro.asymmetric.battles.common.registry;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.asymmetric.battles.common.api.spawning.BattleSpawnDetail;
import com.necro.asymmetric.battles.common.api.spawning.BattleSpawnPool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SpawnPoolTypeRegistry {
    private static final Map<String, SpawnPoolRegistry> SPAWN_POOL_TYPE_REGISTRY = new HashMap<>();

    public static void sort() {
        SPAWN_POOL_TYPE_REGISTRY.values().forEach(SpawnPoolRegistry::sort);
    }

    public static void register(String type, BattleSpawnPool pool) {
        SPAWN_POOL_TYPE_REGISTRY.computeIfAbsent(type, key -> new SpawnPoolRegistry()).register(pool.species(), pool);
    }

    public static void register(String type, PokemonProperties properties, BattleSpawnDetail detail, Class<? extends BattleSpawnPool> cls) {
        SPAWN_POOL_TYPE_REGISTRY.computeIfAbsent(type, key -> new SpawnPoolRegistry()).register(properties, detail, cls);
    }

    public static @Nullable BattleSpawnPool get(String type, @NotNull PokemonEntity pokemonEntity) {
        SpawnPoolRegistry registry = SPAWN_POOL_TYPE_REGISTRY.get(type);
        if (registry == null) return null;
        return registry.get(pokemonEntity);
    }

    public static @Nullable BattleSpawnPool get(String type, @NotNull Pokemon pokemon) {
        SpawnPoolRegistry registry = SPAWN_POOL_TYPE_REGISTRY.get(type);
        if (registry == null) return null;
        return registry.get(pokemon);
    }

    public static @Nullable BattleSpawnPool get(String type, @NotNull PokemonProperties properties) {
        SpawnPoolRegistry registry = SPAWN_POOL_TYPE_REGISTRY.get(type);
        if (registry == null) return null;
        return registry.get(properties);
    }
}
