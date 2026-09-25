package com.necro.asymmetric.battles.common.mixin.client;

import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.battles.Targetable;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon;
import com.necro.asymmetric.battles.common.util.AsymmetricUtils;
import kotlin.collections.CollectionsKt;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ActiveClientBattlePokemon.class)
public abstract class ActiveClientBattlePokemonMixin implements Targetable {
    @Shadow
    public abstract @NotNull BattleFormat getFormat();

    @Shadow
    public abstract @NotNull Iterable<Targetable> getAllActivePokemon();

    @Override
    public @NotNull List<Targetable> getAdjacent() {
        if (AsymmetricUtils.isAsymmetricBattle(this.getFormat())) return CollectionsKt.filter(
            this.getAllActivePokemon(),
            pokemon -> {
                if (pokemon == this) return false;
                ClientBattlePokemon battlePokemon = ((ActiveClientBattlePokemon) pokemon).getBattlePokemon();
                return battlePokemon != null && battlePokemon.getHpValue() > 0;
            }
        );
        else return Targetable.super.getAdjacent();
    }
}
