package com.necro.asymmetric.battles.common.mixin.entity;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonServerDelegate;
import com.necro.asymmetric.battles.common.util.IBattleSpawn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PokemonServerDelegate.class)
public class PokemonServerDelegateMixin {
    @Shadow
    public PokemonEntity entity;

    @Inject(method = "doDeathDrops", at = @At("HEAD"), remap = false, cancellable = true)
    private void doDeathDropsInject(CallbackInfo ci) {
        if (this.entity != null && ((IBattleSpawn) this.entity.getPokemon()).aba_isBattleSpawn()) ci.cancel();
    }
}
