package com.necro.asymmetric.battles.neoforge.mixin.rctapi;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.gitlab.srcmc.rctapi.api.battle.BattleManager;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerNPC;
import com.necro.asymmetric.battles.common.compat.rctapi.RCTBattleParticipant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BattleManager.class)
public class BattleManagerMixin {
    @Inject(method = "toBattleActor(Lcom/gitlab/srcmc/rctapi/api/trainer/TrainerNPC;)Lcom/cobblemon/mod/common/api/battles/model/actor/BattleActor;", at = @At("HEAD"), cancellable = true)
    private static void maybeMultiAttackBattleActor(TrainerNPC participant, CallbackInfoReturnable<BattleActor> cir) {
        cir.setReturnValue(RCTBattleParticipant.trainer(participant).toActor());
    }
}
