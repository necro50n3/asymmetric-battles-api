package com.necro.asymmetric.battles.common.mixin;

import com.cobblemon.mod.common.battles.runner.graal.GraalShowdownUnbundler;
import com.necro.asymmetric.battles.common.showdown.ShowdownLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GraalShowdownUnbundler.class)
public class GraalShowdownUnbundlerMixin {
    @Unique
    private boolean crd_loadedBattleSim = false;

    @Inject(method = "attemptUnbundle", at = @At("TAIL"), remap = false)
    private void attemptUnbundleInject(CallbackInfo ci) {
        if (!this.crd_loadedBattleSim) {
            new ShowdownLoader().load();
            this.crd_loadedBattleSim = true;
        }
    }
}
