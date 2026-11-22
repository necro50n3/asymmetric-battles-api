package com.necro.asymmetric.battles.common.api.actor.multi;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.actor.MultiPokemonBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.asymmetric.battles.common.api.actor.MultiAttackBattleActor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class MultiAttackMultiPokemonBattleActor extends MultiPokemonBattleActor implements MultiAttackBattleActor {
    public MultiAttackMultiPokemonBattleActor(List<BattlePokemon> pokemonList, @NotNull BattleAI battleAI, @NotNull UUID uuid) {
        super(pokemonList, battleAI, uuid);
    }

    @Override
    public void onChoiceRequested() {
        this.onMultiChoiceRequested();
    }
}