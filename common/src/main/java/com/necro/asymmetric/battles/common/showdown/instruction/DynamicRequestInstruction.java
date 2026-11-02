package com.necro.asymmetric.battles.common.showdown.instruction;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.ai.StrongBattleAI;
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.net.messages.client.battle.BattleInitializePacket;
import com.cobblemon.mod.common.net.messages.client.battle.BattleMakeChoicePacket;
import com.cobblemon.mod.common.net.messages.client.battle.BattleSetTeamPokemonPacket;
import com.necro.asymmetric.battles.common.mixin.battle.StrongBattleAIAccessor;
import kotlin.Pair;
import kotlin.Unit;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public record DynamicRequestInstruction(BattleMessage message) implements InterpreterInstruction {
    @Override
    public void invoke(@NotNull PokemonBattle battle) {
        Pair<BattleActor, ActiveBattlePokemon> pair = this.message.actorAndActivePokemon(0, battle);
        if (pair == null) return;
        BattleActor actor = pair.component1();
        for (BattleActor iterActor : battle.getActors()) {
            if (iterActor instanceof AIBattleActor ai && ai.getBattleAI() instanceof StrongBattleAI strongAi) {
                ((StrongBattleAIAccessor) (Object) strongAi).getActiveTracker().initialize(ai.getSide());
            }
        }
        if (actor instanceof PlayerBattleActor playerActor && playerActor.getEntity() != null) {
            ServerPlayer player = playerActor.getEntity();
            new BattleInitializePacket(battle, battle.getSide1()).sendToPlayer(player);
            new BattleSetTeamPokemonPacket(actor.getPokemonList().stream().map(BattlePokemon::getEffectedPokemon).toList()).sendToPlayer(player);

            battle.dispatchGo(() -> {
                actor.setMustChoose(true);
                playerActor.sendUpdate(new BattleMakeChoicePacket());
                return Unit.INSTANCE;
            });
        }
    }
}
