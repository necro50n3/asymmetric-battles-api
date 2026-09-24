package com.necro.asymmetric.battles.common.mixin.battle;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.ai.RandomBattleAI;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.necro.asymmetric.battles.common.util.AsymmetricUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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
    private Object modifyTargets(Object result, @Local(argsOnly = true) PokemonBattle battle) {
        List<ActiveBattlePokemon> target = (List<ActiveBattlePokemon>) result;
        if (AsymmetricUtils.isAsymmetricBattle(battle)) return target;
        if (target == null || target.isEmpty()) return target;
        return target.stream().filter(targetable -> targetable.getBattlePokemon() != null).toList();
    }
}
