package com.necro.asymmetric.battles.common.mixin.battle;

import com.cobblemon.mod.common.battles.BattleFormat;
import com.necro.asymmetric.battles.common.api.AsymmetricBattleFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(BattleFormat.Companion.class)
public class BattleFormatMixin {
    @Inject(method = "fromFormatIdentifier", at = @At("HEAD"), remap = false, cancellable = true)
    private void fromFormatIdentifierInject(String id, CallbackInfoReturnable<BattleFormat> cir) {
        if (Set.of("quadruple_battle", "quadruple", "quadruples").contains(id)) cir.setReturnValue(AsymmetricBattleFormats.GEN_9_QUADRUPLES);
        else if (Set.of("pentuple_battle", "pentuple", "pentuples").contains(id)) cir.setReturnValue(AsymmetricBattleFormats.GEN_9_PENTUPLES);
        else if (Set.of("sextuple_battle", "sextuple", "sextuples").contains(id)) cir.setReturnValue(AsymmetricBattleFormats.GEN_9_SEXTUPLES);
        else if (Set.of("horde_battle", "horde").contains(id)) cir.setReturnValue(AsymmetricBattleFormats.GEN_9_HORDE);
        else if (Set.of("raid_battle", "raid").contains(id)) cir.setReturnValue(AsymmetricBattleFormats.GEN_9_RAID);
        else if (Set.of("raid_single_battle", "raid_single", "raid_singles").contains(id)) cir.setReturnValue(AsymmetricBattleFormats.GEN_9_RAID_SINGLES);
        else if (Set.of("raid_double_battle", "raid_double", "raid_doubles").contains(id)) cir.setReturnValue(AsymmetricBattleFormats.GEN_9_RAID_DOUBLES);
        else if (Set.of("raid_triple_battle", "raid_triple", "raid_triples").contains(id)) cir.setReturnValue(AsymmetricBattleFormats.GEN_9_RAID_TRIPLES);
        else if (Set.of("raid_quadruple_battle", "raid_quadruple", "raid_quadruples").contains(id)) cir.setReturnValue(AsymmetricBattleFormats.GEN_9_RAID_QUADRUPLES);
        else if (Set.of("raid_pentuple_battle", "raid_pentuple", "raid_pentuples").contains(id)) cir.setReturnValue(AsymmetricBattleFormats.GEN_9_RAID_PENTUPLES);
        else if (Set.of("raid_sextuple_battle", "raid_sextuple", "raid_sextuples").contains(id)) cir.setReturnValue(AsymmetricBattleFormats.GEN_9_RAID_SEXTUPLES);
    }
}
