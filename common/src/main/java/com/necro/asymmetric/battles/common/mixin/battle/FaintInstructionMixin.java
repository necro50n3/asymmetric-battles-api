package com.necro.asymmetric.battles.common.mixin.battle;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.interpreter.instructions.FaintInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.asymmetric.battles.common.api.actor.HordeBattleActor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Random;

@Mixin(FaintInstruction.class)
public abstract class FaintInstructionMixin {
    @Shadow
    public abstract BattleMessage getMessage();

    @Inject(method = "invoke", at = @At("HEAD"), remap = false)
    private void changeLeader(PokemonBattle battle, CallbackInfo ci) {
        battle.dispatchToFront(() -> {
            BattlePokemon pokemon = this.getMessage().battlePokemon(0, battle);
            if (pokemon == null) return DispatchResultKt.getGO();
            BattleActor actor = pokemon.getActor();
            if (!(actor instanceof HordeBattleActor hordeActor)) return DispatchResultKt.getGO();
            if (!pokemon.getUuid().equals(hordeActor.getLeader().getUuid())) return DispatchResultKt.getGO();
            List<BattlePokemon> remaining = actor.getPokemonList().stream().filter(p -> p.getHealth() > 0).toList();
            if (remaining.isEmpty()) return DispatchResultKt.getGO();
            int random = new Random().nextInt(remaining.size());
            hordeActor.setLeader(remaining.get(random));
            return DispatchResultKt.getGO();
        });
    }
}
