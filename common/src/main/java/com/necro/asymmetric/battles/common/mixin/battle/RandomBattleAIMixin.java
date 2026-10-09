package com.necro.asymmetric.battles.common.mixin.battle;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.ai.RandomBattleAI;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RandomBattleAI.class)
public class RandomBattleAIMixin {
    @SuppressWarnings("unchecked")
    @ModifyExpressionValue(
        method = "choose",
        at = @At(
            value = "INVOKE",
            target = "Lkotlin/jvm/functions/Function1;invoke(Ljava/lang/Object;)Ljava/lang/Object;"
        ),
        remap = false
    )
    private Object modifyTargets(Object result) {
        List<ActiveBattlePokemon> target = (List<ActiveBattlePokemon>) result;
        if (target == null || target.isEmpty()) return target;
        return target.stream().filter(targetable -> targetable.getBattlePokemon() != null).toList();
    }

    @WrapOperation(
        method = "choose",
        at = @At(
            value = "INVOKE",
            target = "Lcom/cobblemon/mod/common/battles/InBattleMove;canBeUsed()Z"
        ),
        remap = false
    )
    private boolean wrapCanBeUsed(InBattleMove instance, Operation<Boolean> original, @Local(argsOnly = true) PokemonBattle battle, @Local(argsOnly = true) BattleSide aiSide, @Share("filtered") LocalBooleanRef filtered) {
        boolean result = original.call(instance);
        if (!result) return false;
        else if (instance.getTarget() != MoveTarget.adjacentAlly) return true;
        else if (battle.getFormat().getBattleType().getPokemonPerSide() < 2) return true;
        long active = aiSide.getActivePokemon()
            .stream()
            .filter(pokemon -> pokemon.getBattlePokemon() != null && pokemon.getBattlePokemon().getHealth() > 0)
            .count();
        boolean filter = active > 1;
        if (filter) filtered.set(true);
        return filter;
    }

    @Inject(method = "choose", at = @At(value = "NEW", target = "com/cobblemon/mod/common/exception/IllegalActionChoiceException"), remap = false, cancellable = true)
    private void avoidThrowIfFiltered(ActiveBattlePokemon activeBattlePokemon, PokemonBattle battle, BattleSide aiSide, ShowdownMoveset moveset, boolean forceSwitch, CallbackInfoReturnable<ShowdownActionResponse> cir, @Share("filtered") LocalBooleanRef filtered) {
        if (filtered.get()) cir.setReturnValue(PassActionResponse.INSTANCE);
    }
}
