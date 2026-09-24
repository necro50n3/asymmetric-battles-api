package com.necro.asymmetric.battles.common.mixin.interaction;

import com.cobblemon.mod.common.net.messages.client.PlayerInteractOptionsPacket;
import com.cobblemon.mod.common.net.messages.client.PlayerInteractOptionsPacket.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(PlayerInteractOptionsPacket.Options.class)
public class PlayerInteractOptionsMixin {
    @Shadow
    @Final
    @Mutable
    private static Options[] $VALUES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void addAsymmetricBattleOptions(CallbackInfo ci) {
        Options[] oldValues = $VALUES;
        Options[] newValues = oldValues;
        List<String> options = List.of("QUADRUPLE_BATTLE", "PENTUPLE_BATTLE", "SEXTUPLE_BATTLE");
        for (String name : options) {
            Options option = createOption(name, oldValues.length);
            newValues = Arrays.copyOf(oldValues, oldValues.length + 1);
            newValues[oldValues.length] = option;
        }
        $VALUES = newValues;
    }

    @Invoker("<init>")
    private static Options createOption(String name, int ordinal) {
        throw new AssertionError();
    }
}
