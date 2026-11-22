package com.necro.asymmetric.battles.common.api.actor.multi;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.asymmetric.battles.common.api.actor.MultiAttackBattleActor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MultiAttackPokemonBattleActor extends PokemonBattleActor implements MultiAttackBattleActor {
    public MultiAttackPokemonBattleActor(@NotNull UUID uuid, BattlePokemon pokemon, float fleeDistance, @NotNull BattleAI battleAI) {
        super(uuid, pokemon, fleeDistance, battleAI);
    }

    @Override
    public void onChoiceRequested() {
        this.onMultiChoiceRequested();
    }
}
