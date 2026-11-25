package com.necro.asymmetric.battles.common.api.actor.multi;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.asymmetric.battles.common.api.actor.MultiAttackBattleActor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MultiAttackPokemonBattleActor extends PokemonBattleActor implements MultiAttackBattleActor {
    private final List<List<ShowdownActionResponse>> multiAttackResponses;

    public MultiAttackPokemonBattleActor(@NotNull UUID uuid, BattlePokemon pokemon, float fleeDistance, @NotNull BattleAI battleAI) {
        super(uuid, pokemon, fleeDistance, battleAI);
        this.multiAttackResponses = new ArrayList<>();
    }

    @Override
    public void onChoiceRequested() {
        this.onMultiChoiceRequested();
    }

    @Override
    public List<List<ShowdownActionResponse>> getMultiAttackResponses() {
        return this.multiAttackResponses;
    }
}
