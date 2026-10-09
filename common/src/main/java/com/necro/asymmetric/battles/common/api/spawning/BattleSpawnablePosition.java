package com.necro.asymmetric.battles.common.api.spawning;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.spawning.SpawnCause;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.api.spawning.position.BasicSpawnablePosition;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.asymmetric.battles.common.util.PropertyExtractors;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;

public class BattleSpawnablePosition extends BasicSpawnablePosition {
    private final PokemonBattle battle;
    private final PokemonProperties rootProperties;
    private final int baseLevel;

    public BattleSpawnablePosition(@NotNull SpawnCause cause, @NotNull ServerLevel level, @NotNull BlockPos blockPos, @NotNull List<SpawningInfluence> influences, PokemonBattle battle, PokemonEntity rootEntity) {
        this(cause, level, blockPos, influences, battle, rootEntity.getPokemon());
    }

    public BattleSpawnablePosition(@NotNull SpawnCause cause, @NotNull ServerLevel level, @NotNull BlockPos blockPos, @NotNull List<SpawningInfluence> influences, PokemonBattle battle, Pokemon rootPokemon) {
        this(cause, level, blockPos, influences, battle, rootPokemon.createPokemonProperties(PropertyExtractors.LONG_EXTRACTOR), rootPokemon.getLevel());
        this.rootProperties.setAspects(new HashSet<>(rootPokemon.getAspects()));
    }

    public BattleSpawnablePosition(@NotNull SpawnCause cause, @NotNull ServerLevel level, @NotNull BlockPos blockPos, @NotNull List<SpawningInfluence> influences, PokemonBattle battle, PokemonProperties rootProperties, int baseLevel) {
        super(cause, level, blockPos, level.getMaxLocalRawBrightness(blockPos), level.getMaxLocalRawBrightness(blockPos.above()), level.canSeeSkyFromBelowWater(blockPos), influences);
        this.battle = battle;
        this.rootProperties = rootProperties;
        this.baseLevel = baseLevel;
    }

    public PokemonBattle battle() {
        return this.battle;
    }

    public PokemonProperties rootProperties() {
        return this.rootProperties;
    }

    public int baseLevel() {
        return this.baseLevel;
    }
}
