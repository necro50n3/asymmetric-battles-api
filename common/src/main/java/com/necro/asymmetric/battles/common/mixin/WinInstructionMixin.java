package com.necro.asymmetric.battles.common.mixin;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.interpreter.instructions.WinInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.necro.asymmetric.battles.common.actor.DummyBattleActor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Mixin(WinInstruction.class)
public abstract class WinInstructionMixin {
    @Final
    @Shadow
    private BattleMessage message;

    @ModifyExpressionValue(
        method = "invoke",
        at = @At(
            value = "INVOKE",
            target = "Lcom/cobblemon/mod/common/api/battles/model/PokemonBattle;isPvW()Z",
            ordinal = 0
        )
    )
    private boolean fixWildWin(boolean original, @Local(argsOnly = true) PokemonBattle battle) {
        if (!original) return false;
        if (!Arrays.stream(battle.getSide2().getActors()).allMatch(actor -> actor.getType() == ActorType.WILD)) return true;
        else if (battle.getSide1().getActors().length > 1 && !(battle.getSide1().getActors()[1] instanceof DummyBattleActor)) return true;
        else if (battle.getSide1().getActors()[0].getType() != ActorType.PLAYER) return true;

        BattleActor nonPlayerActor;
        if (battle.getFormat() == BattleFormat.Companion.getGEN_9_MULTI()) nonPlayerActor = battle.getSide2().getActors()[1];
        else nonPlayerActor = battle.getSide2().getActors()[0];
        List<BattlePokemon> wildPokemonList = nonPlayerActor.getPokemonList();
        if (wildPokemonList.isEmpty()) return false;
        BattlePokemon wildPokemon = wildPokemonList.getFirst();

        String user = this.message.argumentAt(0);
        if (user == null) return false;

        List<UUID> ids = Arrays.stream(user.split("&"))
            .map(String::trim)
            .map(UUID::fromString)
            .toList();

        List<BattleActor> winners = ids.stream()
            .map(battle::getActor)
            .toList();

        List<BattleActor> losers = new ArrayList<>();
        for (BattleActor actor : battle.getActors()) {
            if (!winners.contains(actor)) losers.add(actor);
        }

        boolean wasCaught = battle.getShowdownMessages().stream().anyMatch(message -> message.contains("capture"));

        if (!wasCaught && losers.stream().anyMatch(pokemon -> pokemon.getUuid() == wildPokemon.getUuid())) {
            PokemonEntity pokemonEntity = wildPokemon.getEffectedPokemon().getEntity();
            if (pokemonEntity == null) return false;
            pokemonEntity.setKiller(((PlayerBattleActor) battle.getSide1().getActors()[0]).getEntity());
        }

        return false;
    }

    @ModifyVariable(
        method = "invoke",
        at = @At("STORE"),
        name = "winners"
    )
    private List<BattleActor> filterWinners(List<BattleActor> winners, @Local(argsOnly = true) PokemonBattle battle) {
        String user = this.message.argumentAt(0);
        if (user == null) return winners;

        List<UUID> ids = Arrays.stream(user.split("&"))
            .map(String::trim)
            .map(UUID::fromString)
            .toList();

        return ids.stream()
            .map(battle::getActor)
            .filter(actor -> !(actor instanceof DummyBattleActor))
            .toList();
    }

    @ModifyVariable(
        method = "invoke",
        at = @At("STORE"),
        name = "losers"
    )
    private List<BattleActor> filterLosers(List<BattleActor> losers, @Local(argsOnly = true) PokemonBattle battle) {
        String user = this.message.argumentAt(0);
        if (user == null) return losers;

        List<UUID> ids = Arrays.stream(user.split("&"))
            .map(String::trim)
            .map(UUID::fromString)
            .toList();

        List<BattleActor> winners = ids.stream()
            .map(battle::getActor)
            .filter(actor -> !(actor instanceof DummyBattleActor))
            .toList();

        List<BattleActor> trueLosers = new ArrayList<>();
        for (BattleActor actor : battle.getActors()) {
            if (!(actor instanceof DummyBattleActor) && !winners.contains(actor)) trueLosers.add(actor);
        }
        return trueLosers;
    }
}
