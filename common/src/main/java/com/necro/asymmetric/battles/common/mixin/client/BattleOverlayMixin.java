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
import org.spongepowered.asm.mixin.injection.*;

import java.util.stream.StreamSupport;

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

    @ModifyVariable(method = "drawTile", at = @At("HEAD"), argsOnly = true, ordinal = 3, remap = false)
    private boolean forceCompact(boolean isCompact) {
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        return isCompact || (battle != null && AsymmetricUtils.isAsymmetricOrMultiBattle(battle.getBattleFormat()));
    }

    @ModifyVariable(method = "drawTile", at = @At(value = "STORE", ordinal = 0), remap = false, name = "x")
    private float overrideX(float x, @Local(argsOnly = true) ActiveClientBattlePokemon pokemon, @Local(argsOnly = true, ordinal = 0) int rank) {
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (battle == null || !AsymmetricUtils.isAsymmetricOrMultiBattle(battle.getBattleFormat())) return x;

        int validSlots = (int) StreamSupport.stream(pokemon.getActor().getSide().getActiveClientBattlePokemon().spliterator(), false)
            .filter(active -> active.getBattlePokemon() != null)
            .count();
        return 12 + (validSlots - rank - 1) * 2F;
    }

    @ModifyVariable(method = "drawTile", at = @At(value = "STORE", ordinal = 0), remap = false, name = "y")
    private int overrideY(int y, @Local(argsOnly = true, ordinal = 0) int rank) {
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (battle == null || !AsymmetricUtils.isAsymmetricOrMultiBattle(battle.getBattleFormat())) return y;

        return 10 + rank * 20;
    }
}
