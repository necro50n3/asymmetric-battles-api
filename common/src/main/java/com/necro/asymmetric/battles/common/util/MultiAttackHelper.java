package com.necro.asymmetric.battles.common.util;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.pokemon.feature.IntSpeciesFeature;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.exception.IllegalActionChoiceException;
import com.cobblemon.mod.common.pokemon.Pokemon;
import org.apache.commons.lang3.function.TriFunction;

import java.util.ArrayList;
import java.util.List;

public class MultiAttackHelper {
    public static <T, E extends Targetable> List<T> iterate(ShowdownActionRequest request, List<E> activePokemon, TriFunction<E, ShowdownMoveset, Boolean, T> iterator) {
        int size = Math.max(request.getActive() != null ? request.getActive().size() : 0, request.getForceSwitch().size());
        List<T> responses = new ArrayList<>();

        for (int index = 0; index < size; index++) {
            E active;
            if (activePokemon.size() > index) active = activePokemon.get(index);
            else throw new IllegalStateException("No active Pokémon for slot " + index + " but needed to choose action for it?");
            ShowdownMoveset moveset = request.getActive() != null && request.getActive().size() > index ? request.getActive().get(index) : null;
            boolean forceSwitch = request.getForceSwitch().size() > index && request.getForceSwitch().get(index);

            BattlePokemon pokemon = ((ActiveBattlePokemon) active).getBattlePokemon();
            if (pokemon == null || forceSwitch) {
                responses.add(iterator.apply(active, moveset, forceSwitch));
                continue;
            }

            int moves = attackCount(pokemon.getEffectedPokemon());
            for (int i = 0; i < moves; i++) responses.add(iterator.apply(active, moveset, false));
        }

        return responses;
    }

    public static void setResponses(ShowdownActionRequest request, List<ActiveBattlePokemon> activePokemon, BattleActor actor,
                                    List<ShowdownActionResponse> pending, List<ShowdownActionResponse> responses,
                                    List<ShowdownActionResponse> expecting, List<ShowdownActionResponse> original
    ) {
        int size = Math.max(request.getActive() != null ? request.getActive().size() : 0, request.getForceSwitch().size());
        int running = 0;

        for (int index = 0; index < size; index++) {
            ActiveBattlePokemon active;
            if (activePokemon.size() > index) active = activePokemon.get(index);
            else continue;
            ShowdownMoveset moveset = request.getActive() != null && request.getActive().size() > index ? request.getActive().get(index) : null;
            boolean forceSwitch = request.getForceSwitch().size() > index && request.getForceSwitch().get(index);

            BattlePokemon pokemon = active.getBattlePokemon();
            if (pokemon == null || forceSwitch) {
                validate(actor, pending.get(running++), responses, active, moveset, forceSwitch, expecting, original);
                continue;
            }

            int moves = attackCount(pokemon.getEffectedPokemon());
            for (int i = 0; i < moves; i++) {
                validate(actor, pending.get(running++), responses, active, moveset, false, expecting, original);
            }
        }

        if (!expecting.isEmpty()) {
            throw new IllegalActionChoiceException(actor, "Invalid action choice: a capture was expected. Are you hacking me?");
        }
    }

    private static void validate(BattleActor actor, ShowdownActionResponse pending, List<ShowdownActionResponse> responses,
                                 ActiveBattlePokemon active, ShowdownMoveset moveset, boolean forceSwitch,
                                 List<ShowdownActionResponse> expecting, List<ShowdownActionResponse> original
    ) {
        if (!pending.isValid(active, moveset, forceSwitch)) {
            expecting.clear();
            expecting.addAll(original);
            throw new IllegalActionChoiceException(actor, "Invalid action choice for ${activeBattlePokemon.battlePokemon!!.getName().string}: $response");
        }
        else if (pending instanceof ForcePassActionResponse) {
            responses.add(expecting.removeFirst());
        }
        else responses.add(pending);
    }

    public static List<String> writeResponses(ShowdownActionRequest request, List<ActiveBattlePokemon> activePokemon, List<ShowdownActionResponse> responses) {
        List<String> showdownMessages = new ArrayList<>();
        int size = Math.max(request.getActive() != null ? request.getActive().size() : 0, request.getForceSwitch().size());
        int running = 0;

        for (int index = 0; index < size; index++) {
            ActiveBattlePokemon active;
            if (activePokemon.size() > index) active = activePokemon.get(index);
            else continue;
            ShowdownMoveset moveset = request.getActive() != null && request.getActive().size() > index ? request.getActive().get(index) : null;
            boolean forceSwitch = request.getForceSwitch().size() > index && request.getForceSwitch().get(index);

            BattlePokemon pokemon = active.getBattlePokemon();
            if (pokemon == null || forceSwitch) {
                showdownMessages.add(responses.get(running++).toShowdownString(active, moveset));
                continue;
            }

            int moves = attackCount(pokemon.getEffectedPokemon());
            for (int i = 0; i < moves; i++) {
                String showdownString;
                if (i == 0) showdownString = responses.get(running++).toShowdownString(active, moveset);
                else {
                    assert moveset != null;
                    // Stop early to prevent battle error. Should fix this to account for the Pokémon index at some point.
                    if (running >= responses.size()) continue;
                    showdownString = ((IMultiAttacker) responses.get(running++)).aba_toMultiAttackShowdownString(active, moveset);
                }
                showdownMessages.add(showdownString);
            }
        }

        return showdownMessages;
    }

    public static int attackCount(Pokemon pokemon) {
        IntSpeciesFeature feature = pokemon.getFeature("attacks_per_turn");
        return feature != null ? feature.getValue() : 1;
    }

    public static boolean canMultiAttack(Pokemon pokemon) {
        return attackCount(pokemon) > 1;
    }
}
