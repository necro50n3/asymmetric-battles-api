package com.necro.asymmetric.battles.common.mixin.interaction;

import com.cobblemon.mod.common.net.messages.client.PlayerInteractOptionsPacket.Options;
import com.cobblemon.mod.common.net.messages.client.PlayerInteractOptionsPacket.OptionStatus;
import com.cobblemon.mod.common.net.serverhandling.RequestInteractionsHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.necro.asymmetric.battles.common.battle.AsymmetricOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.EnumMap;

@Mixin(RequestInteractionsHandler.class)
public class RequestInteractionsHandlerMixin {
    @WrapOperation(
        method = "handle(Lcom/cobblemon/mod/common/net/messages/server/RequestPlayerInteractionsPacket;Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/level/ServerPlayer;)V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/EnumMap;isEmpty()Z"
        ),
        remap = false
    )
    private boolean addAsymmetricBattleOptions(EnumMap<Options, OptionStatus> instance, Operation<Boolean> original) {
        if (instance.containsKey(Options.SINGLE_BATTLE)) {
            instance.put(Options.DOUBLE_BATTLE, OptionStatus.AVAILABLE);
            instance.put(Options.TRIPLE_BATTLE, OptionStatus.AVAILABLE);
            instance.put(AsymmetricOptions.QUADRUPLE_BATTLE, OptionStatus.AVAILABLE);
            instance.put(AsymmetricOptions.PENTUPLE_BATTLE, OptionStatus.AVAILABLE);
            instance.put(AsymmetricOptions.SEXTUPLE_BATTLE, OptionStatus.AVAILABLE);
        }
        return original.call(instance);
    }
}
