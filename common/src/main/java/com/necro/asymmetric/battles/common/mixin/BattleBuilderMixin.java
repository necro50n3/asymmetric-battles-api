package com.necro.asymmetric.battles.common.mixin;

import com.cobblemon.mod.common.battles.BattleBuilder;
import com.cobblemon.mod.common.battles.BattleFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

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
    }
}
