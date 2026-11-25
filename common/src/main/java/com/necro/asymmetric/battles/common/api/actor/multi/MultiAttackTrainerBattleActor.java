package com.necro.asymmetric.battles.common.api.actor.multi;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.battles.actor.TrainerBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.asymmetric.battles.common.api.actor.MultiAttackBattleActor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MultiAttackTrainerBattleActor extends TrainerBattleActor implements MultiAttackBattleActor {
    private final List<List<ShowdownActionResponse>> multiAttackResponses;

    public MultiAttackTrainerBattleActor(String trainerName, UUID uuid, List<BattlePokemon> pokemonList, @NotNull BattleAI battleAI) {
        super(trainerName, uuid, pokemonList, battleAI);
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
