package com.necro.asymmetric.battles.common.mixin.battle;

import com.cobblemon.mod.common.battles.*;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;

@Mixin(BattleBuilder.class)
public class BattleBuilderMixin {
    @WrapOperation(method = { "pvp1v1*", "pvp2v2*", "pve*", "pvn*" }, at = @At(value = "INVOKE", target = "Ljava/util/Collection;add(Ljava/lang/Object;)Z"), remap = false)
    private static boolean cancelInsufficientPokemonError(Collection<BattleStartError> set, Object error, Operation<Boolean> original) {
        if (aba_shouldCancel(error)) return false;
        return original.call(set, error);
    }

    @Unique
    private static boolean aba_shouldCancel(Object error) {
        return error instanceof InsufficientPokemonError;
    }
}
