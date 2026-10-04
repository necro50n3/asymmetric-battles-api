package com.necro.asymmetric.battles.common.mixin.client;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleSide;
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.necro.asymmetric.battles.common.util.AsymmetricUtils;
import kotlin.collections.CollectionsKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BattleOverlay.class)
public class BattleOverlayMixin {
    @WrapOperation(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lcom/cobblemon/mod/common/client/battle/ClientBattleSide;getActiveClientBattlePokemon()Ljava/lang/Iterable;"
        ),
        remap = false
    )
    private Iterable<ActiveClientBattlePokemon> filterPokemon(ClientBattleSide instance, Operation<Iterable<ActiveClientBattlePokemon>> original) {
        if (!AsymmetricUtils.isAsymmetricOrMultiBattle(instance.getBattle().getBattleFormat())) return original.call(instance);
        else return CollectionsKt.filter(original.call(instance), pokemon -> pokemon.getBattlePokemon() != null);
    }

    @ModifyVariable(
        method = "drawTile",
        at = @At(value = "HEAD"),
        argsOnly = true,
        ordinal = 8,
        remap = false
    )
    private boolean modifyIsCompact(boolean isCompact) {
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        return battle != null && AsymmetricUtils.isAsymmetricOrMultiBattle(battle.getBattleFormat());
    }

    @ModifyConstant(method = "drawTile", constant = @Constant(floatValue = 4F, ordinal = 0), remap = false)
    private float modifyHorizontalSpacing(float constant, @Local(argsOnly = true) ActiveClientBattlePokemon activeBattlePokemon) {
        if (!AsymmetricUtils.isAsymmetricOrMultiBattle(activeBattlePokemon.getFormat())) return constant;
        else return 2F;
    }

    @ModifyConstant(method = "drawTile", constant = @Constant(intValue = 30, ordinal = 0), remap = false)
    private int modifyVerticalSpacing(int constant, @Local(argsOnly = true) ActiveClientBattlePokemon activeBattlePokemon) {
        if (!AsymmetricUtils.isAsymmetricOrMultiBattle(activeBattlePokemon.getFormat())) return constant;
        else return 20;
    }
}
