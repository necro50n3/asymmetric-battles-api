package com.necro.asymmetric.battles.common.mixin;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.battles.runner.graal.GraalShowdownService;
import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import com.necro.asymmetric.battles.common.api.AsymmetricAPI;
import com.necro.asymmetric.battles.common.config.AsymmetricConfig;
import com.necro.asymmetric.battles.common.util.AsymmetricUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.UUID;

@Mixin(GraalShowdownService.class)
public class DebugMixin {
    @Inject(method = "sendFromShowdown", at = @At("HEAD"), remap = false)
    private void debugSendFrom(String battleId, String message, CallbackInfo ci) {
        if (!AsymmetricConfig.Common.CONFIG.ENABLE_DEBUG.get()) return;
        AsymmetricBattlesAPI.LOGGER.info(message);
    }

    @Inject(method = "sendToShowdown", at = @At("HEAD"), remap = false)
    private void debugSendTo(UUID battleId, String[] messages, CallbackInfo ci) {
        if (!AsymmetricConfig.Common.CONFIG.ENABLE_DEBUG.get()) return;

        PokemonBattle battle = BattleRegistry.getBattle(battleId);
        if (battle != null && messages[0].startsWith(">p2") && AsymmetricUtils.isAsymmetricOrMultiBattle(battle)) {
            messages = Arrays.copyOf(messages, messages.length + 1);
            messages[messages.length - 1] = ">p4 move 1";
        }

        for (String message : messages) AsymmetricBattlesAPI.LOGGER.info(message);
    }

    @Inject(method = "startBattle", at = @At("HEAD"), remap = false)
    private void debugStartBattle(PokemonBattle battle, String[] messages, CallbackInfo ci) {
        if (!AsymmetricConfig.Common.CONFIG.ENABLE_DEBUG.get()) return;
        for (String message : messages) AsymmetricBattlesAPI.LOGGER.info(message);
    }
}
