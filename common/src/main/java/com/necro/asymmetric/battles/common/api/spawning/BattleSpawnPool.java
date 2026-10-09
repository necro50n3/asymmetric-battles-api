package com.necro.asymmetric.battles.common.api.spawning;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.evolution.PreEvolution;
import com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.adapters.PokemonPropertiesAdapterKt;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.necro.asymmetric.battles.common.util.DoubleWeightedRandomMap;
import com.necro.asymmetric.battles.common.util.PropertyExtractors;
import kotlin.ranges.IntRange;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BattleSpawnPool extends BattleSpawnTarget {
    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(PokemonProperties.class, PokemonPropertiesAdapterKt.getPokemonPropertiesShortAdapter())
        .registerTypeAdapter(BattleSpawnDetail.class, new BattleSpawnDetail())
        .registerTypeAdapter(new TypeToken<List<BattleSpawnDetail>>(){}.getType(), new BattleSpawnDetailListAdapter())
        .create();

    public List<BattleSpawnDetail> spawns = new ArrayList<>();

    public boolean isSatisfiedBy(PokemonProperties check) {
        return this.properties().isSubSetOf(check) && check.getAspects().containsAll(this.properties().getAspects());
    }

    public List<BattleSpawnDetail> validSpawns(BattleSpawnablePosition spawnablePosition) {
        return this.spawns.stream().filter(spawn -> spawn.isSatisfiedBy(spawnablePosition)).toList();
    }

    public @Nullable Pokemon getRandom(BattleSpawnablePosition spawnablePosition, ServerPlayer player) {
        List<BattleSpawnDetail> validSpawns = this.validSpawns(spawnablePosition);
        if (validSpawns.isEmpty()) return null;
        DoubleWeightedRandomMap<BattleSpawnDetail> spawnMap = DoubleWeightedRandomMap.fromMap(validSpawns.stream().collect(Collectors.toMap(detail -> detail, detail -> detail.getWeight(spawnablePosition))));
        return spawnMap.getRandom(player.getRandom()).map(detail -> detail.create(spawnablePosition.baseLevel(), player)).orElse(null);
    }

    public static BattleSpawnDetail defaultSpawn(Pokemon pokemon, IntRange levelRangeOffset) {
        PreEvolution preEvolution = null;
        for (
            PreEvolution current = pokemon.getPreEvolution();
            current != null && !current.getForm().getLabels().contains(CobblemonPokemonLabels.BABY);
            current = current.getForm().getPreEvolution()
        ) {
            preEvolution = current;
        }

        PokemonProperties spawnProperties = pokemon.createPokemonProperties(PropertyExtractors.SHORT_EXTRACTOR);
        FormData targetForm = pokemon.getForm();
        if (preEvolution != null) {
            targetForm = preEvolution.getForm();
            spawnProperties.setSpecies(preEvolution.getSpecies().getResourceIdentifier().getPath());
        }

        spawnProperties.setForm(targetForm.formOnlyShowdownId());
        spawnProperties.setAspects(new HashSet<>(targetForm.getAspects()));
        return BattleSpawnDetail.basic(spawnProperties, levelRangeOffset, 1.0);
    }
}
