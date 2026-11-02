package com.necro.asymmetric.battles.common.mixin.client;

import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.battles.Targetable;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattleSide;
import com.necro.asymmetric.battles.common.util.AsymmetricUtils;
import kotlin.collections.CollectionsKt;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ActiveClientBattlePokemon.class)
public abstract class ActiveClientBattlePokemonMixin implements Targetable {
    @Shadow
    public abstract @NotNull BattleFormat getFormat();

    @Shadow
    public abstract @NotNull Iterable<Targetable> getAllActivePokemon();

    @Shadow
    public abstract @NotNull ClientBattleActor getActor();

    @Inject(method = "getLetter", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void aba_getCorrectLetter(CallbackInfoReturnable<Character> cir) {
        ClientBattleActor actor = this.getActor();
        ClientBattleSide side = this.getActor().getSide();

        List<ClientBattleActor> actors = side.getActors();
        int actorIndex = actors.indexOf(actor);
        if (actorIndex == -1) return;

        int slotsPerActor = this.getFormat().getBattleType().getSlotsPerActor();
        int activeIndex = Math.max(0, actor.getActivePokemon().indexOf((ActiveClientBattlePokemon) (Object) this));

        int slotOffset = (actorIndex * slotsPerActor) + activeIndex;
        cir.setReturnValue((char) ('a' + slotOffset));
    }

    @Inject(method = "getAdjacent", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void aba_getAllAdjacent(CallbackInfoReturnable<List<Targetable>> cir) {
        if (!AsymmetricUtils.isAsymmetricBattle(this.getFormat())) return;
        cir.setReturnValue(CollectionsKt.filter(
            this.getAllActivePokemon(),
            pokemon -> {
                if (pokemon == this) return false;
                ClientBattlePokemon battlePokemon = ((ActiveClientBattlePokemon) pokemon).getBattlePokemon();
                return battlePokemon != null && battlePokemon.getHpValue() > 0;
            }
        ));
    }
}
