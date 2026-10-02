package com.necro.asymmetric.battles.common.mixin.client;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.SingleActionRequest;
import com.cobblemon.mod.common.client.gui.battle.BattleGUI;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleActionSelection;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleMoveSelection;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleTargetSelection;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BattleMoveSelection.class)
public abstract class BattleMoveSelectionMixin extends BattleActionSelection {
    @Shadow
    private List<BattleMoveSelection.MoveTile> moveTiles;

    public BattleMoveSelectionMixin(@NotNull BattleGUI battleGUI, @NotNull SingleActionRequest request, int x, int y, int width, int height, @NotNull MutableComponent name) {
        super(battleGUI, request, x, y, width, height, name);
    }

    @Inject(method = "mousePrimaryClicked", at = @At("HEAD"), remap = false, cancellable = true)
    private void onMultiBattle1v1(double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        BattleMoveSelection.MoveTile move = this.moveTiles.stream().filter(moveTile -> moveTile.isHovered(mouseX, mouseY)).findFirst().orElse(null);
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (move == null || battle == null) return;
        if (!battle.getBattleFormat().getBattleType().getName().equals("multi")) return;

        int side1 = 0;
        for (ActiveClientBattlePokemon pokemon : battle.getSide1().getActiveClientBattlePokemon()) {
            if (pokemon.hasPokemon() && ++side1 > 1) return;
        }

        int side2 = 0;
        for (ActiveClientBattlePokemon pokemon : battle.getSide2().getActiveClientBattlePokemon()) {
            if (pokemon.hasPokemon() && ++side2 > 1) return;
        }

        BattleTargetSelection selection = new BattleTargetSelection(this.getBattleGUI(), this.getRequest(), move.getMove(), move.getResponse().getGimmickID(), move.getMove().getGimmickMove());
        BattleTargetSelection.TargetTile target = selection.getTargetTiles().stream().filter(BattleTargetSelection.TargetTile::getSelectable).findFirst().orElse(null);
        if (target == null) return;
        target.onClick();
        cir.setReturnValue(true);
    }
}
