package com.necro.asymmetric.battles.neoforge.mixin.msd;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.github.yajatkaul.mega_showdown.api.lilycobble.networking.battle.BattleActorState;
import com.github.yajatkaul.mega_showdown.api.lilycobble.networking.battle.BattlePokemonState;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@Mixin(BattleActorState.class)
public class BattleActorStateMixin {
    @ModifyVariable(method = "of", at = @At(value = "STORE", ordinal = 0), remap = false, name = "pokemon")
    private static List<BattlePokemonState> overrideActorsInRaids(List<BattlePokemonState> pokemon, @Local(argsOnly = true) BattleActor actor, @Local(argsOnly = true) Function<BattlePokemon, BattlePokemonState> stateCreator) {
        PokemonBattle battle = actor.getBattle();
        if (!battle.getFormat().getBattleType().getName().equalsIgnoreCase("raid")) return pokemon;
        return actor.getActivePokemon().stream()
            .filter(active -> active != null && active.getBattlePokemon() != null)
            .map(ActiveBattlePokemon::getBattlePokemon)
            .sorted(Comparator.comparing(battlePokemon -> battlePokemon.getEffectedPokemon().getDisplayName(false).getString()))
            .map(stateCreator).toList();
    }
}
