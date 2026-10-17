package com.necro.asymmetric.battles.common.api.spawning;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.google.gson.annotations.SerializedName;
import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GlobalBattleSpawnPool implements AsymmetricSpawnPool {
    public transient String type = null;
    public String properties = "";
    protected transient PokemonProperties propertiesInner = null;

    @SerializedName(value = "spawns", alternate = { "spawn" })
    public List<BattleSpawnDetail> spawns = new ArrayList<>();

    public boolean replace = false;

    @Override
    public @NotNull PokemonProperties properties() {
        if (this.propertiesInner == null) this.propertiesInner = PokemonProperties.Companion.parse(this.properties);
        return this.propertiesInner;
    }

    @Override
    public boolean isSatisfiedBy(PokemonProperties check) {
        AsymmetricBattlesAPI.LOGGER.info(this.properties().asString(" "));
        AsymmetricBattlesAPI.LOGGER.info("isSubSetOf={},aspects={}", this.properties().isSubSetOf(check), check.getAspects().containsAll(this.properties().getAspects()));
        return this.properties.isEmpty() || (this.properties().isSubSetOf(check) && check.getAspects().containsAll(this.properties().getAspects()));
    }

    @Override
    public List<BattleSpawnDetail> validSpawns(BattleSpawnablePosition spawnablePosition) {
        return this.spawns.stream().filter(spawn -> spawn.isSatisfiedBy(spawnablePosition)).toList();
    }
}
