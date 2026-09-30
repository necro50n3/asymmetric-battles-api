package com.necro.asymmetric.battles.common.api;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.pokemon.evolution.progress.EvolutionProgress;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.runner.ShowdownService;
import com.cobblemon.mod.common.pokemon.evolution.progress.LastBattleCriticalHitsEvolutionProgress;
import com.necro.asymmetric.battles.common.network.AsymmetricNetworkMessages;
import com.necro.asymmetric.battles.common.util.MultiBattleUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Core runtime API for dynamically modifying active Cobblemon battles.
 * <p>
 * This class provides utility functions to hot-join or substitute a {@link BattleActor}
 * into an ongoing Multi Battle without restarting the battle engine.
 *
 * <h4>Multi Battle Slot Layout:</h4>
 * <ul>
 *   <li><b>Side 1 (Ally Team):</b>
 *     <ul>
 *       <li>{@code side = 1} (Showdown {@code p1}): Primary actor (Index 0).</li>
 *       <li>{@code side = 3} (Showdown {@code p3}): Ally actor (Index 1).</li>
 *     </ul>
 *   </li>
 *   <li><b>Side 2 (Opposing Team):</b>
 *     <ul>
 *       <li>{@code side = 2} (Showdown {@code p2}): Primary opponent (Index 0).</li>
 *       <li>{@code side = 4} (Showdown {@code p4}): Ally opponent (Index 1).</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * @see AsymmetricBattleBuilder#multiBattle
 */
public class AsymmetricAPI {

    /**
     * Dynamically injects a new {@link BattleActor} into an ongoing Multi Battle at the specified slot.
     * @param actor  The {@link BattleActor} joining the battle (Player, NPC, Wild, or Horde actor).
     * @param battle The active {@link PokemonBattle} instance.
     * @param side   The battle side slot (1 to 4):
     *               <ul>
     *                 <li><b>Side 1 (Ally Team):</b>
     *                   <ul>
     *                     <li>{@code side = 1} (Showdown {@code p1}): Primary actor (Index 0).</li>
     *                     <li>{@code side = 3} (Showdown {@code p3}): Ally actor (Index 1).</li>
     *                   </ul>
     *                 </li>
     *                 <li><b>Side 2 (Opposing Team):</b>
     *                   <ul>
     *                     <li>{@code side = 2} (Showdown {@code p2}): Primary opponent (Index 0).</li>
     *                     <li>{@code side = 4} (Showdown {@code p4}): Ally opponent (Index 1).</li>
     *                   </ul>
     *                 </li>
     *               </ul>
     */
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
        for (BattleActor iterActor : battle.getActors()) {
            if (!(iterActor instanceof PlayerBattleActor player)) return;
            AsymmetricNetworkMessages.MULTI_BATTLE_ACTOR_UPDATE.accept(player.getEntity(), side, player.getSide() == battleSide, actor);
        }
    }
}
