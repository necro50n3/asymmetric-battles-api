package com.necro.asymmetric.battles.common.mixin;

import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.battles.Targetable;
import com.necro.asymmetric.battles.common.util.AsymmetricUtils;
import kotlin.collections.CollectionsKt;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ActiveBattlePokemon.class)
public abstract class ActiveBattlePokemonMixin implements Targetable {
    @Shadow
    public abstract @NotNull BattleFormat getFormat();

    @Shadow
    public abstract @NotNull Iterable<Targetable> getAllActivePokemon();

    @Override
    public @NotNull List<Targetable> getAdjacent() {
        if (AsymmetricUtils.isAsymmetricBattle(this.getFormat())) return CollectionsKt.filter(this.getAllActivePokemon(), pokemon -> pokemon != this);
        else return Targetable.super.getAdjacent();
    }
}
