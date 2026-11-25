package com.necro.asymmetric.battles.common.api.actor;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.api.pokemon.feature.IntSpeciesFeature;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.exception.IllegalActionChoiceException;
import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import com.necro.asymmetric.battles.common.util.IMultiAttacker;
import com.necro.asymmetric.battles.common.util.MultiAttackHelper;
import org.apache.commons.lang3.function.TriFunction;

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
        this.getMultiAttackResponses().clear();
        try {
            List<List<ShowdownActionResponse>> responses = new ArrayList<>();

            if (this.getRequest() != null) {
                responses.addAll(iterate((pokemon, moveset, forceSwitch) ->
                    this.getBattleAI().choose(pokemon, this.getBattle(), this.getSide(), moveset, forceSwitch)
                ));
                this.getPokemonList().forEach(pokemon -> pokemon.setWillBeSwitchedIn(false));
            }
            else {
                for (int i = 0; i < this.getActivePokemon().size(); i++) {
                    List<ShowdownActionResponse> subResponses = new ArrayList<>();
                    ActiveBattlePokemon pokemon = this.getActivePokemon().get(i);
                    IntSpeciesFeature feature = pokemon.getBattlePokemon() != null ? pokemon.getBattlePokemon().getEffectedPokemon().getFeature("attacks_per_turn") : null;
                    int moves = feature != null ? feature.getValue() : 1;
                    for (int j = 0; j < moves; j++) subResponses.add(PassActionResponse.INSTANCE);
                    responses.add(subResponses);
                }
                AsymmetricBattlesAPI.LOGGER.warn("AI requested choice, but no request was set. Returning PassActionResponses.");
            }

            this.getMultiAttackResponses().addAll(responses);
            this.setActionResponses(flatList(responses));
        }
        catch (IllegalActionChoiceException exception) {
            AsymmetricBattlesAPI.LOGGER.error("AI was unable to choose an action, we're going to need to pass!");
            List<List<ShowdownActionResponse>> responses = new ArrayList<>();

            if (this.getRequest() != null) {
                responses.addAll(iterate((battleMon, moveset, forceSwitch) -> PassActionResponse.INSTANCE));
            }
            else {
                for (int i = 0; i < this.getActivePokemon().size(); i++) {
                    List<ShowdownActionResponse> subResponses = new ArrayList<>();
                    ActiveBattlePokemon pokemon = this.getActivePokemon().get(i);
                    IntSpeciesFeature feature = pokemon.getBattlePokemon() != null ? pokemon.getBattlePokemon().getEffectedPokemon().getFeature("attacks_per_turn") : null;
                    int moves = feature != null ? feature.getValue() : 1;
                    for (int j = 0; j < moves; j++) subResponses.add(PassActionResponse.INSTANCE);
                    responses.add(subResponses);
                }
            }

            this.getMultiAttackResponses().addAll(responses);
            this.setActionResponses(flatList(responses));
        }
    }

    private <T> List<List<T>> iterate(TriFunction<ActiveBattlePokemon, ShowdownMoveset, Boolean, T> iterator) {
        int size = Math.max(this.getRequest().getActive() != null ? this.getRequest().getActive().size() : 0, this.getRequest().getForceSwitch().size());
        List<List<T>> responses = new ArrayList<>();

        for (int index = 0; index < size; index++) {
            List<T> subResponses = new ArrayList<>();
            ActiveBattlePokemon active;
            if (this.getActivePokemon().size() > index) active = this.getActivePokemon().get(index);
            else throw new IllegalStateException("No active Pokémon for slot " + index + " but needed to choose action for it?");
            ShowdownMoveset moveset = this.getRequest().getActive() != null && this.getRequest().getActive().size() > index ? this.getRequest().getActive().get(index) : null;
            boolean forceSwitch = this.getRequest().getForceSwitch().size() > index && this.getRequest().getForceSwitch().get(index);

            BattlePokemon pokemon = active.getBattlePokemon();
            if (pokemon == null || forceSwitch) {
                subResponses.add(iterator.apply(active, moveset, forceSwitch));
                responses.add(subResponses);
                continue;
            }

            int moves = MultiAttackHelper.attackCount(pokemon.getEffectedPokemon());
            for (int i = 0; i < moves; i++) subResponses.add(iterator.apply(active, moveset, false));
            responses.add(subResponses);
        }

        return responses;
    }

    private static <T> List<T> flatList(List<List<T>> list) {
        return list.stream().flatMap(List::stream).toList();
    }

    /**
     * Overrides the default responses setter from AI Battle Actors to utilise multiple attacks per turn.
     */
    default void setMultiResponses(BattleActor actor) {
        int size = this.getMultiAttackResponses().size();
        List<ShowdownActionResponse> expecting = this.getExpectingPassActions();
        List<ShowdownActionResponse> original = new ArrayList<>(expecting);

        for (int index = 0; index < size; index++) {
            List<ShowdownActionResponse> subResponses = this.getMultiAttackResponses().get(index);

            ActiveBattlePokemon active;
            if (this.getActivePokemon().size() > index) active = this.getActivePokemon().get(index);
            else continue;
            ShowdownMoveset moveset = this.getRequest().getActive() != null && this.getRequest().getActive().size() > index ? this.getRequest().getActive().get(index) : null;
            boolean forceSwitch = this.getRequest().getForceSwitch().size() > index && this.getRequest().getForceSwitch().get(index);

            for (ShowdownActionResponse response : subResponses) validate(actor, response, active, moveset, forceSwitch, expecting, original);
        }

        if (!expecting.isEmpty()) {
            throw new IllegalActionChoiceException(actor, "Invalid action choice: a capture was expected. Are you hacking me?");
        }
    }

    private void validate(BattleActor actor, ShowdownActionResponse pending, ActiveBattlePokemon active, ShowdownMoveset moveset, boolean forceSwitch, List<ShowdownActionResponse> expecting, List<ShowdownActionResponse> original) {
        if (!pending.isValid(active, moveset, forceSwitch)) {
            expecting.clear();
            expecting.addAll(original);
            throw new IllegalActionChoiceException(actor, "Invalid action choice for ${activeBattlePokemon.battlePokemon!!.getName().string}: $response");
        }
        else if (pending instanceof ForcePassActionResponse) {
            this.getResponses().add(expecting.removeFirst());
        }
        else this.getResponses().add(pending);
    }


    /**
     * Writes the multi attack responses to Showdown strings to send to the Showdown Engine.
     */
    default List<String> writeMultiResponses() {
        List<String> showdownMessages = new ArrayList<>();
        int size = this.getMultiAttackResponses().size();

        for (int index = 0; index < size; index++) {
            List<ShowdownActionResponse> subResponses = this.getMultiAttackResponses().get(index);
            if (subResponses.isEmpty()) continue;
            ActiveBattlePokemon active;
            if (this.getActivePokemon().size() > index) active = this.getActivePokemon().get(index);
            else continue;
            ShowdownMoveset moveset = this.getRequest().getActive() != null && this.getRequest().getActive().size() > index ? this.getRequest().getActive().get(index) : null;

            showdownMessages.add(subResponses.getFirst().toShowdownString(active, moveset));
            assert subResponses.size() <= 1 || moveset != null;
            if (subResponses.getFirst() instanceof MoveActionResponse moveResponse && moveResponse.getMoveName().equals("recharge")) break;
            for (int j = 1; j < subResponses.size(); j++) {
                showdownMessages.add(((IMultiAttacker) subResponses.get(j)).aba_toMultiAttackShowdownString(active, moveset));
            }
        }
        this.getMultiAttackResponses().clear();

        return showdownMessages;
    }

    List<List<ShowdownActionResponse>> getMultiAttackResponses();
    List<ShowdownActionResponse> getResponses();
    List<ShowdownActionResponse> getExpectingPassActions();
    ShowdownActionRequest getRequest();
    List<ActiveBattlePokemon> getActivePokemon();
    BattleAI getBattleAI();
    PokemonBattle getBattle();
    BattleSide getSide();
    void setActionResponses(List<? extends ShowdownActionResponse> responses);
    List<BattlePokemon> getPokemonList();
}
