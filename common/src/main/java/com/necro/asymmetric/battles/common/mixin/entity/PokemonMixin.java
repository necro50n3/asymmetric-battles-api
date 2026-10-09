package com.necro.asymmetric.battles.common.mixin.entity;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.asymmetric.battles.common.util.IBattleSpawn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Pokemon.class)
public abstract class PokemonMixin implements IBattleSpawn {
    @Unique
    private boolean aba_isBattleSpawn = false;

    @Shadow
    public abstract boolean isWild();

    @Override
    public void aba_setBattleSpawn() {
        this.aba_isBattleSpawn = true;
    }

    @Override
    public boolean aba_isBattleSpawn() {
        return this.aba_isBattleSpawn && this.isWild();
    }
}
