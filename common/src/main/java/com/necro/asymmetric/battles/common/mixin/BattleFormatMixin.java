package com.necro.asymmetric.battles.common.mixin;

import com.cobblemon.mod.common.battles.BattleFormat;
import com.necro.asymmetric.battles.common.api.ExtraBattleFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(BattleFormat.Companion.class)
public class BattleFormatMixin {
    @Inject(method = "fromFormatIdentifier", at = @At("HEAD"), remap = false, cancellable = true)
    private void fromFormatIdentifierInject(String id, CallbackInfoReturnable<BattleFormat> cir) {
        if (Set.of("quadruple_battle", "quadruple", "quadruples").contains(id)) cir.setReturnValue(ExtraBattleFormats.GEN_9_QUADRUPLES);
        else if (Set.of("pentuple_battle", "pentuple", "pentuples").contains(id)) cir.setReturnValue(ExtraBattleFormats.GEN_9_PENTUPLES);
        else if (Set.of("sextuple_battle", "sextuple", "sextuples").contains(id)) cir.setReturnValue(ExtraBattleFormats.GEN_9_SEXTUPLES);
    }
}
