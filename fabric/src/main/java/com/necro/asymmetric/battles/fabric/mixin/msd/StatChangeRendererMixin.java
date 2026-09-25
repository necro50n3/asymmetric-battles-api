package com.necro.asymmetric.battles.fabric.mixin.msd;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay;
import com.github.yajatkaul.mega_showdown.client.battle.hud.StatChangeRenderer;
import com.llamalad7.mixinextras.sugar.Local;
import com.necro.asymmetric.battles.common.util.AsymmetricUtils;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StatChangeRenderer.class)
public class StatChangeRendererMixin {
    @ModifyVariable(
        method = "render",
        at = @At("STORE"),
        name = "startingLeftX",
        remap = false
    )
    private static int modifyStartingLeftX(int startingLeftX, @Local(argsOnly = true) int order) {
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (battle == null) return startingLeftX;
        if (!AsymmetricUtils.isAsymmetricBattle(battle.getBattleFormat())) return startingLeftX;
        else return BattleOverlay.HORIZONTAL_INSET + BattleOverlay.TILE_WIDTH - order * 2;
    }

    @ModifyVariable(
        method = "render",
        at = @At("STORE"),
        name = "startingRightX",
        remap = false
    )
    private static int modifyStartingRightX(int startingRightX, @Local(argsOnly = true) int order) {
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (battle == null) return startingRightX;
        if (!AsymmetricUtils.isAsymmetricBattle(battle.getBattleFormat())) return startingRightX;

        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        return screenWidth - BattleOverlay.HORIZONTAL_INSET - BattleOverlay.TILE_WIDTH - 1 + order * 2;
    }

    @ModifyVariable(
        method = "render",
        at = @At("STORE"),
        name = "y",
        remap = false
    )
    private static int modifyY(int y, @Local(argsOnly = true) int order) {
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (battle == null) return y;
        if (!AsymmetricUtils.isAsymmetricBattle(battle.getBattleFormat())) return y;
        else return BattleOverlay.VERTICAL_INSET + order * 20;
    }
}
