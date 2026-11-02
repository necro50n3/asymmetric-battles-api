package com.necro.asymmetric.battles.common.mixin.client;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleSide;
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
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

    @ModifyVariable(method = "drawTile", at = @At(value = "STORE", ordinal = 0), remap = false, name = "x")
    private float overrideX(float x, @Local(argsOnly = true) ActiveClientBattlePokemon pokemon, @Local(argsOnly = true, ordinal = 0) int rank, @Share("validSlots") LocalIntRef validSlots) {
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (battle == null || !AsymmetricUtils.isAsymmetricOrMultiBattle(battle.getBattleFormat())) return x;

        validSlots.set((int) StreamSupport.stream(pokemon.getActor().getSide().getActiveClientBattlePokemon().spliterator(), false)
            .filter(active -> active.getBattlePokemon() != null)
            .count()
        );
        return 12 + (validSlots.get() - rank - 1) * 2F;
    }

    @ModifyVariable(method = "drawTile", at = @At(value = "STORE", ordinal = 0), remap = false, name = "y")
    private int overrideY(int y, @Local(argsOnly = true) ActiveClientBattlePokemon pokemon, @Local(argsOnly = true, ordinal = 0) boolean left, @Local(argsOnly = true, ordinal = 0) int rank, @Share("validSlots") LocalIntRef validSlots) {
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (battle == null || !AsymmetricUtils.isAsymmetricOrMultiBattle(battle.getBattleFormat())) return y;

        int digit = pokemon.getActorShowdownId().charAt(1) - '0';
        int playerNumberOffset = (digit - 1) / 2 * 10;

        return 10 + rank * 20 + (left ? playerNumberOffset : (validSlots.get() - 1) * 10 - playerNumberOffset);
    }
}
