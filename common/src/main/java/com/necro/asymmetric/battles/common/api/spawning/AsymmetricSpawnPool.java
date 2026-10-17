package com.necro.asymmetric.battles.common.api.spawning;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.util.adapters.PokemonPropertiesAdapterKt;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public interface AsymmetricSpawnPool {
    Gson GSON = new GsonBuilder()
        .registerTypeAdapter(PokemonProperties.class, PokemonPropertiesAdapterKt.getPokemonPropertiesShortAdapter())
        .registerTypeAdapter(BattleSpawnDetail.class, new BattleSpawnDetail())
        .registerTypeAdapter(new TypeToken<List<BattleSpawnDetail>>(){}.getType(), new BattleSpawnDetailListAdapter())
        .create();

    PokemonProperties properties();

    default boolean isSatisfiedBy(PokemonProperties check) {
        return this.properties().isSubSetOf(check) && check.getAspects().containsAll(this.properties().getAspects());
    }

    List<BattleSpawnDetail> validSpawns(BattleSpawnablePosition spawnablePosition);
}
