package com.necro.asymmetric.battles.neoforge.mixin.msd;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.github.yajatkaul.mega_showdown.client.battle.hud.StatChangeRenderer;
import com.necro.asymmetric.battles.common.util.AsymmetricUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(StatChangeRenderer.class)
public class StatChangeRendererMixin {
    @ModifyConstant(method = "render", constant = { @Constant(intValue = 4, ordinal = 0), @Constant(intValue = 4, ordinal = 2) })
    private static int modifyHorizontalSpacing(int constant) {
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (battle == null) return constant;
        if (!AsymmetricUtils.isAsymmetricBattle(battle.getBattleFormat())) return constant;
        else return 2;
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 28, ordinal = 0))
    private static int modifyVerticalSpacing(int constant) {
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (battle == null) return constant;
        if (!AsymmetricUtils.isAsymmetricBattle(battle.getBattleFormat())) return constant;
        else return 18;
    }
}
