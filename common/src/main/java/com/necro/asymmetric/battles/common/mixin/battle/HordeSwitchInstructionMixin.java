package com.necro.asymmetric.battles.common.mixin.battle;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.PokemonSeenEvent;
import com.cobblemon.mod.common.battles.interpreter.instructions.SwitchInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.asymmetric.battles.common.api.actor.HordeBattleActor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(SwitchInstruction.Companion.class)
public class HordeSwitchInstructionMixin {
    @Inject(method = "broadcastSwitch", at = @At("HEAD"), remap = false, cancellable = true)
    private void silenceHordeSwitch(PokemonBattle battle, BattleActor actor, BattlePokemon newPokemon, BattlePokemon illusion, CallbackInfo ci) {
        if (!(actor instanceof HordeBattleActor)) return;
        for (UUID uuid : battle.getPlayerUUIDs()) {
            CobblemonEvents.POKEMON_SEEN.post(new PokemonSeenEvent(uuid, (illusion != null ? illusion : newPokemon).getEffectedPokemon()));
        }
        ci.cancel();
    }
}
