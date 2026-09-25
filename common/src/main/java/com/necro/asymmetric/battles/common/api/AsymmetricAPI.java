package com.necro.asymmetric.battles.common.api;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.pokemon.evolution.progress.EvolutionProgress;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.runner.ShowdownService;
import com.cobblemon.mod.common.net.messages.client.battle.BattleApplyPassResponsePacket;
import com.cobblemon.mod.common.pokemon.evolution.progress.LastBattleCriticalHitsEvolutionProgress;

import java.util.ArrayList;
import java.util.List;

public class AsymmetricAPI {
    public static void setMultiBattleActor(BattleActor actor, PokemonBattle battle, int side) {
        if (battle.getEnded()) return;
        else if (!battle.getFormat().getBattleType().getName().equalsIgnoreCase("multi")) return;

        BattleSide battleSide = side == 1 || side == 3 ? battle.getSide1() : battle.getSide2();
        int index = side == 1 || side == 2 ? 0 : 1;
        battleSide.getActors()[index] = actor;
        actor.setBattle(battle);
        actor.setShowdownId("p" + side);
        actor.getResponses().addFirst(PassActionResponse.INSTANCE);
        actor.setMustChoose(false);
        battle.getActors().forEach(a -> a.sendUpdate(new BattleApplyPassResponsePacket()));
        for (int i = 0; i < battle.getFormat().getBattleType().getSlotsPerActor(); i++) {
            actor.getActivePokemon().add(new ActiveBattlePokemon(actor, actor.getPokemonList().get(i)));
        }

        actor.getPokemonList().forEach(pokemon -> {
            if (pokemon.getEntity() != null) pokemon.getEntity().setBattleId(battle.getBattleId());
            pokemon.getEffectedPokemon().getEvolutionProxy().current().progress()
                .stream().filter(LastBattleCriticalHitsEvolutionProgress.class::isInstance)
                .forEach(EvolutionProgress::reset);
        });
        List<String> messages = new ArrayList<>();
        messages.add(String.format(">eval " +
                "const side = battle.sides[%1$d]; " +
                "side.name = \"%3$s\"; " +
                "side.initTeam(battle.getTeam({ \"team\": \"%4$s\" })); " +
                "side.totalFainted = 0; " +

                "const requests = battle.getRequests(\"move\"); " +
                "side.activeRequest = requests[%1$d]; " +
                "side.chooseTeam(%2$d); " +
                "for (let i = 0; i < Math.min(side.active.length, side.pokemon.length); i++) { " +
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
