package com.necro.asymmetric.battles.common.mixin.client;

import com.cobblemon.mod.common.client.battle.ClientBattlePokemon;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleTargetSelection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(BattleTargetSelection.TargetTile.class)
public class TargetTileMixin {
    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lkotlin/jvm/internal/Intrinsics;checkNotNull(Ljava/lang/Object;)V",
            ordinal = 0
        ),
        remap = false
    )
    private static void allowNullBattlePokemon(Object object) {}

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/cobblemon/mod/common/client/battle/ClientBattlePokemon;getUuid()Ljava/util/UUID;"
        ),
        remap = false
    )
    private UUID allowNullUUID(ClientBattlePokemon pokemon) {
        return pokemon == null ? null : pokemon.getUuid();
    }
}
