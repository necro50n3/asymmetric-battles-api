package com.necro.asymmetric.battles.common.mixin.battle;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.ShowdownActionRequest;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.necro.asymmetric.battles.common.api.actor.MultiAttackBattleActor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BattleActor.class)
public abstract class BattleActorMixin {
    @Shadow
    public String showdownId;
    @Shadow
    public PokemonBattle battle;
    @Shadow
    private ShowdownActionRequest request;
    @Shadow
    private List<ShowdownActionResponse> responses;
    @Final
    @Shadow
    private List<ShowdownActionResponse> expectingPassActions;
    @Shadow
    private boolean mustChoose;

    @Inject(method = "setActionResponses", at = @At("HEAD"), remap = false, cancellable = true)
    private void setMultipleActionResponses(List<ShowdownActionResponse> responses, CallbackInfo ci) {
        if (!(this instanceof MultiAttackBattleActor actor)) return;
        if (this.request == null) return;
        actor.setMultiResponses((BattleActor) actor);

        this.mustChoose = false;
        this.battle.checkForInputDispatch();

        ci.cancel();
    }

    @Inject(method = "writeShowdownResponse", at = @At("HEAD"), remap = false, cancellable = true)
    private void writeShowdownResponseInject(CallbackInfo ci) {
        if (!(this instanceof MultiAttackBattleActor actor)) return;

        List<String> showdownMessages = actor.writeMultiResponses();

        this.responses.clear();
        this.request = null;
        this.expectingPassActions.clear();
        this.battle.writeShowdownAction(String.format(">%s %s", this.showdownId, String.join(", ", showdownMessages)));

        ci.cancel();
    }
}
