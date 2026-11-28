package com.necro.asymmetric.battles.common.mixin.interaction;

import com.cobblemon.mod.common.api.interaction.RequestManager;
import com.cobblemon.mod.common.api.interaction.ServerPlayerActionRequest;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RequestManager.class)
public class RequestManagerMixin {
    @ModifyExpressionValue(
        method = "sendRequest",
        at = @At(
            value = "INVOKE",
            target = "Lcom/cobblemon/mod/common/api/interaction/RequestManager;isBusy(Lnet/minecraft/server/level/ServerPlayer;)Z"
        ),
        remap = false
    )
    private boolean modifyIsBusy(boolean original, @Local(argsOnly = true) ServerPlayerActionRequest request) {
        if (!(request.getKey().equalsIgnoreCase("join"))) return original;
        return PlayerExtensionsKt.isTrading(request.getReceiver());
    }
}
