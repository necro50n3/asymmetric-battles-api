package com.necro.asymmetric.battles.common.mixin.battle;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import kotlin.text.Regex;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BattleMessage.class)
public class BattleMessageMixin {
    @Final
    @Shadow
    @Mutable
    private static Regex PNX_MATCHER;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void extendPNXMatcher(CallbackInfo ci) {
        PNX_MATCHER = new Regex("p\\d[a-f]");
    }
}
