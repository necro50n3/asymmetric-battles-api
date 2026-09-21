package com.necro.asymmetric.battles.common.mixin;

import com.cobblemon.mod.common.battles.BattleBuilder;
import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.battles.BattleStartError;
import com.cobblemon.mod.common.battles.InsufficientPokemonError;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collection;

@Mixin(BattleBuilder.class)
public class BattleBuilderMixin {
    @ModifyVariable(
        method = "pve(Lnet/minecraft/server/level/ServerPlayer;Lcom/cobblemon/mod/common/entity/pokemon/PokemonEntity;Ljava/util/UUID;Lcom/cobblemon/mod/common/battles/BattleFormat;ZZFLcom/cobblemon/mod/common/api/storage/party/PartyStore;)Lcom/cobblemon/mod/common/battles/BattleStartResult;",
        at = @At("HEAD"),
        argsOnly = true,
        remap = false
    )
    private BattleFormat modifyBattleFormat(BattleFormat battleFormat) {
        return BattleFormat.Companion.getGEN_9_DOUBLES();
        // return ExtraBattleFormats.GEN_9_SEXTUPLES;
    }

    @WrapOperation(method = { "pvp1v1*", "pvp2v2*", "pve*", "pvn*" }, at = @At(value = "INVOKE", target = "Ljava/util/Collection;add(Ljava/lang/Object;)Z"))
    private static boolean cancelInsufficientPokemonError(Collection<BattleStartError> set, Object error, Operation<Boolean> original) {
        if (aba_shouldCancel(error)) return false;
        return original.call(set, error);
    }

    @Unique
    private static boolean aba_shouldCancel(Object error) {
        AsymmetricBattlesAPI.LOGGER.info(error.getClass().getName());
        if (!(error instanceof InsufficientPokemonError battleError)) return false;
        else return battleError.getHadCount() != 0;
    }
}
