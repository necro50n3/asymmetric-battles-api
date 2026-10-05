package com.necro.asymmetric.battles.common.mixin;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.runner.graal.GraalShowdownService;
import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(GraalShowdownService.class)
public class DebugMixin {
    @Inject(method = "sendFromShowdown", at = @At("HEAD"), remap = false)
    private void debugSendFrom(String battleId, String message, CallbackInfo ci) {
        if (!AsymmetricBattlesAPI.CONFIG.ENABLE_DEBUG) return;
        AsymmetricBattlesAPI.LOGGER.info(message);
    }

    @Inject(method = "sendToShowdown", at = @At("HEAD"), remap = false)
    private void debugSendTo(UUID battleId, String[] messages, CallbackInfo ci) {
        if (!AsymmetricBattlesAPI.CONFIG.ENABLE_DEBUG) return;
        for (String message : messages) AsymmetricBattlesAPI.LOGGER.info(message);
    }

    @Inject(method = "startBattle", at = @At("HEAD"), remap = false)
    private void debugStartBattle(PokemonBattle battle, String[] messages, CallbackInfo ci) {
        if (!AsymmetricBattlesAPI.CONFIG.ENABLE_DEBUG) return;
        for (String message : messages) AsymmetricBattlesAPI.LOGGER.info(message);
    }
}
