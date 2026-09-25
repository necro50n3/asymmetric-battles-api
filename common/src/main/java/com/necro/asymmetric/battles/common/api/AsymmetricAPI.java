package com.necro.asymmetric.battles.common.api;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.pokemon.evolution.progress.EvolutionProgress;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.runner.ShowdownService;
import com.cobblemon.mod.common.pokemon.evolution.progress.LastBattleCriticalHitsEvolutionProgress;
import com.necro.asymmetric.battles.common.util.MultiBattleUtils;

import java.util.ArrayList;
import java.util.List;

/*
 * Dynamically add a new BattleActor to an existing Multi Battle.
 * Sides 1 and 3 represent the ally/player side, and Sides 2 and 4 represent the opposing side.
 */

public class AsymmetricAPI {
    public static void setMultiBattleActor(BattleActor actor, PokemonBattle battle, int side) {
        if (battle.getEnded()) return;
        else if (!battle.getFormat().getBattleType().getName().equalsIgnoreCase("multi")) return;

        BattleSide battleSide = side == 1 || side == 3 ? battle.getSide1() : battle.getSide2();
        int index = side == 1 || side == 2 ? 0 : 1;
        battleSide.getActors()[index] = actor;
        actor.setBattle(battle);
        actor.setShowdownId("p" + side);
        for (int i = 0; i < battle.getFormat().getBattleType().getSlotsPerActor(); i++) {
            actor.getActivePokemon().add(new ActiveBattlePokemon(actor, actor.getPokemonList().get(i)));
        }
        actor.getPokemonList().forEach(pokemon -> {
            if (pokemon.getEntity() != null) pokemon.getEntity().setBattleId(battle.getBattleId());
            pokemon.getEffectedPokemon().getEvolutionProxy().current().progress()
                .stream().filter(LastBattleCriticalHitsEvolutionProgress.class::isInstance)
                .forEach(EvolutionProgress::reset);
        });
        MultiBattleUtils.add(actor.getUuid());

        List<String> messages = new ArrayList<>();
        messages.add(String.format(">eval " +
                "const side = battle.sides[%1$d]; " +
                "side.name = \"%3$s\"; " +
                "side.initTeam(battle.getTeam({ \"team\": \"%4$s\" })); " +
                "side.totalFainted = 0; " +

                "for (let i = 0; i < side.pokemon.length; i++) { " +
                    "battle.initPokemon(side.pokemon[i]); " +
                "} " +

                "for (let i = 0; i < Math.min(side.active.length, side.pokemon.length); i++) { " +
                    "side.active[i] = side.pokemon[i]; " +
                "} " +

                "const requests = battle.getRequests(\"move\"); " +
                "side.emitRequest(requests[%1$d]); " +
                "side.chooseTeam(%2$d); " +

                "for (let i = 0; i < Math.min(side.active.length, side.pokemon.length); i++) { " +
                    "side.active[i] = null; " +
                    "battle.actions.switchIn(side.pokemon[i], i); " +
                "}",
            side - 1,
            actor.getPokemonList().size(),
            actor.getUuid(),
            BattleRegistry.INSTANCE.packTeam(actor.getPokemonList())
        ));

        ShowdownService.Companion.getService().send(battle.getBattleId(), messages.toArray(new String[]{}));
    }
}
