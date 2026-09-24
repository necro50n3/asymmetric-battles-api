package com.necro.asymmetric.battles.common.mixin.client;

import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.battles.Targetable;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.necro.asymmetric.battles.common.util.AsymmetricUtils;
import kotlin.collections.CollectionsKt;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(ActiveClientBattlePokemon.class)
public abstract class ActiveClientBattlePokemonMixin implements Targetable {
    @Shadow
    public abstract @NotNull BattleFormat getFormat();

    @Shadow
    public abstract @NotNull Iterable<Targetable> getAllActivePokemon();

    @Override
    public @NotNull List<Targetable> getAdjacent() {
        if (this.aba_isAsymmetricBattleFormat(this.getFormat())) return CollectionsKt.filter(this.getAllActivePokemon(), pokemon -> pokemon != this);
        else return Targetable.super.getAdjacent();
    }

    @Unique
    private boolean aba_isAsymmetricBattleFormat(BattleFormat format) {
        return AsymmetricUtils.isAsymmetricBattle(format);
    }
}
