package com.necro.asymmetric.battles.common.api.spawning;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.StringExtensionsKt;
import com.cobblemon.mod.common.util.adapters.PokemonPropertiesAdapterKt;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.necro.asymmetric.battles.common.util.DoubleWeightedRandomMap;
import kotlin.Pair;
import kotlin.collections.CollectionsKt;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public abstract class BattleSpawnPool {
    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(PokemonProperties.class, PokemonPropertiesAdapterKt.getPokemonPropertiesShortAdapter())
        .registerTypeAdapter(BattleSpawnDetail.class, new BattleSpawnDetail())
        .registerTypeAdapter(new TypeToken<List<BattleSpawnDetail>>(){}.getType(), new BattleSpawnDetailListAdapter())
        .create();

    public PokemonProperties pokemon = new PokemonProperties();
    private transient String species = null;
    public List<BattleSpawnDetail> spawns = new ArrayList<>();

    public @NotNull String species() {
        if (this.species != null) return this.species;

        if (this.pokemon.getSpecies() != null) this.species = this.pokemon.getSpecies();
        else {
            List<Pair<String, String>> keyPairs = StringExtensionsKt.splitMap(this.pokemon.getOriginalString(), " ", "=");
            Pair<String, String> matched = CollectionsKt.last(keyPairs, pair -> "species".equalsIgnoreCase(pair.getFirst()));
            if (matched == null) this.species = "random";
            else {
                String species = matched.getSecond().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_:]", "");
                if (species.contains(":")) this.species = species.split(":")[1];
                else this.species = species;
            }
        }
        return this.species;
    }

    public List<BattleSpawnDetail> validSpawns(BattleSpawnablePosition spawnablePosition) {
        PokemonProperties check = spawnablePosition.rootProperties();
        check.setAspects(spawnablePosition.rootProperties().getAspects());

        return this.spawns.stream().filter(spawn -> spawn.isSatisfiedBy(spawnablePosition, check)).toList();
    }

    public @Nullable Pokemon getRandom(BattleSpawnablePosition spawnablePosition, ServerPlayer player) {
        List<BattleSpawnDetail> validSpawns = this.validSpawns(spawnablePosition);
        if (validSpawns.isEmpty()) return null;
        DoubleWeightedRandomMap<BattleSpawnDetail> spawnMap = DoubleWeightedRandomMap.fromMap(validSpawns.stream().collect(Collectors.toMap(detail -> detail, detail -> detail.getWeight(spawnablePosition))));
        return spawnMap.getRandom(player.getRandom()).map(detail -> detail.create(spawnablePosition, player)).orElse(null);
    }
}
