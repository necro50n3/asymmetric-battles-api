package com.necro.asymmetric.battles.common.mixin.battle;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.interpreter.instructions.SwitchInstruction;
import com.necro.asymmetric.battles.common.util.MultiBattleUtils;
import kotlin.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SwitchInstruction.class)
public abstract class SwitchInstructionMixin {
    @Shadow
    public abstract BattleMessage getPublicMessage();

    @Inject(method = "invoke", at = @At("RETURN"), remap = false)
    private void invokeInject(PokemonBattle battle, CallbackInfo ci) {
        battle.dispatch(() -> {
            Pair<String, String> pair = this.getPublicMessage().pnxAndUuid(0);
            if (pair == null) return DispatchResultKt.getGO();
            String pnx = pair.getFirst();
            if (!pnx.equalsIgnoreCase("p4b") && !pnx.equalsIgnoreCase("p2a")) return DispatchResultKt.getGO();
            Pair<BattleActor, ActiveBattlePokemon> actorPair = battle.getActorAndActiveSlotFromPNX(pnx);
            BattleActor actor = actorPair.getFirst();

            if (MultiBattleUtils.has(actor.getUuid()) && actor instanceof AIBattleActor aiActor) aiActor.onChoiceRequested();
            return DispatchResultKt.getGO();
        });
    }
}
