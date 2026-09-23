package com.necro.asymmetric.battles.common.mixin;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.battles.BattleSide;
import com.necro.asymmetric.battles.common.actor.DummyBattleActor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(PokemonBattle.class)
public abstract class PokemonBattleMixin {
    @Shadow
    public abstract BattleSide getSide1();

    @Shadow
    public abstract BattleSide getSide2();

    @Inject(method = "isPvW", at = @At("HEAD"), remap = false, cancellable = true)
    private void isPvWInject(CallbackInfoReturnable<Boolean> cir) {
        if (!Arrays.stream(this.getSide2().getActors()).allMatch(actor -> actor.getType() == ActorType.WILD)) return;
        else if (this.getSide1().getActors().length > 1 && !(this.getSide1().getActors()[1] instanceof DummyBattleActor)) return;
        else if (this.getSide1().getActors()[0].getType() != ActorType.PLAYER) return;
        cir.setReturnValue(true);
    }
}
