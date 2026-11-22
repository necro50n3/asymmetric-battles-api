package com.necro.asymmetric.battles.common.api.actor;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.api.pokemon.feature.IntSpeciesFeature;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.exception.IllegalActionChoiceException;
import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import com.necro.asymmetric.battles.common.util.MultiAttackHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility for an AI Battle Actor which can handle multiple attacks per turn, based on a Global Species Feature.
 * @see MultiAttackHelper
 */
public interface MultiAttackBattleActor {
    /**
     * Overrides the default choice selector from AI Battle Actors to utilise multiple attacks per turn.
     */
    default void onMultiChoiceRequested() {
        try {
            List<ShowdownActionResponse> response = new ArrayList<>();

            if (this.getRequest() != null) {
                response.addAll(MultiAttackHelper.iterate(this.getRequest(), this.getActivePokemon(), (pokemon, moveset, forceSwitch) ->
                    this.getBattleAI().choose(pokemon, this.getBattle(), this.getSide(), moveset, forceSwitch)
                ));
                this.setActionResponses(response);
                this.getPokemonList().forEach(pokemon -> pokemon.setWillBeSwitchedIn(false));
            }
            else {
                for (int i = 0; i < this.getActivePokemon().size(); i++) {
                    ActiveBattlePokemon pokemon = this.getActivePokemon().get(i);
                    IntSpeciesFeature feature = pokemon.getBattlePokemon() != null ? pokemon.getBattlePokemon().getEffectedPokemon().getFeature("attacks_per_turn") : null;
                    int moves = feature != null ? feature.getValue() : 1;
                    for (int j = 0; j < moves; j++) response.add(PassActionResponse.INSTANCE);
                }
                this.setActionResponses(response);
                AsymmetricBattlesAPI.LOGGER.warn("AI requested choice, but no request was set. Returning PassActionResponses.");
            }
        }
        catch (IllegalActionChoiceException exception) {
            AsymmetricBattlesAPI.LOGGER.error("AI was unable to choose an action, we're going to need to pass!");

            if (this.getRequest() != null) {
                this.setActionResponses(MultiAttackHelper.iterate(this.getRequest(), this.getActivePokemon(), (battleMon, moveset, forceSwitch) -> PassActionResponse.INSTANCE));
            }
            else {
                List<ShowdownActionResponse> response = new ArrayList<>();
                for (int i = 0; i < this.getActivePokemon().size(); i++) {
                    ActiveBattlePokemon pokemon = this.getActivePokemon().get(i);
                    IntSpeciesFeature feature = pokemon.getBattlePokemon() != null ? pokemon.getBattlePokemon().getEffectedPokemon().getFeature("attacks_per_turn") : null;
                    int moves = feature != null ? feature.getValue() : 1;
                    for (int j = 0; j < moves; j++) response.add(PassActionResponse.INSTANCE);
                }
                this.setActionResponses(response);
            }
        }
    }

    ShowdownActionRequest getRequest();
    List<ActiveBattlePokemon> getActivePokemon();
    BattleAI getBattleAI();
    PokemonBattle getBattle();
    BattleSide getSide();
    void setActionResponses(List<? extends ShowdownActionResponse> responses);
    List<BattlePokemon> getPokemonList();
}
