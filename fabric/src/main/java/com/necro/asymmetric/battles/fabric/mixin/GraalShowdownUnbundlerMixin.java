package com.necro.asymmetric.battles.fabric.mixin;

import com.cobblemon.mod.common.battles.runner.graal.GraalShowdownUnbundler;
import com.necro.asymmetric.battles.fabric.showdown.FabricShowdownLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GraalShowdownUnbundler.class, priority = 2000)
public class GraalShowdownUnbundlerMixin {
    @Unique
    private boolean crd_loadedBattleSim = false;

    @Inject(method = "attemptUnbundle", at = @At("TAIL"), remap = false)
    private void attemptUnbundleInject(CallbackInfo ci) {
        if (!this.crd_loadedBattleSim) {
            new FabricShowdownLoader().load();
            this.crd_loadedBattleSim = true;
        }
    }
}
