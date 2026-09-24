package com.necro.asymmetric.battles.common.mixin.client;

import com.cobblemon.mod.common.battles.InBattleGimmickMove;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleSide;
import com.cobblemon.mod.common.client.battle.SingleActionRequest;
import com.cobblemon.mod.common.client.gui.battle.BattleGUI;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleActionSelection;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleTargetSelection;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleTargetSelection.TargetTile;
import com.necro.asymmetric.battles.common.util.AsymmetricUtils;
import kotlin.collections.CollectionsKt;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static com.cobblemon.mod.common.client.gui.battle.subscreen.BattleTargetSelection.*;
import static com.cobblemon.mod.common.client.gui.battle.subscreen.BattleTargetSelection.Companion.ArrowDirection;

@Mixin(BattleTargetSelection.class)
public abstract class BattleTargetSelectionMixin extends BattleActionSelection {
    @Final
    @Shadow
    @Mutable
    private List<TargetTile> baseTiles;

    @Shadow
    @Mutable
    private List<TargetTile> targetTiles;

    @Final
    @Shadow
    private List<ActiveClientBattlePokemon> targets;

    public BattleTargetSelectionMixin(@NotNull BattleGUI battleGUI, @NotNull SingleActionRequest request, int x, int y, int width, int height, @NotNull MutableComponent name) {
        super(battleGUI, request, x, y, width, height, name);
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void modifyBaseTiles(BattleGUI battleGUI, SingleActionRequest request, InBattleMove move, String gimmickID, InBattleGimmickMove gimmickMove, CallbackInfo ci) {
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (battle == null || !AsymmetricUtils.isAsymmetricBattle(battle.getBattleFormat())) return;

        List<TargetTile> baseTiles = new ArrayList<>();

        ClientBattleSide allySide = request.getActivePokemon().getActor().getSide();
        ClientBattleSide foeSide = allySide == battle.getSide1() ? battle.getSide2() : battle.getSide1();

        int allyTeamSize = CollectionsKt.filter(allySide.getActiveClientBattlePokemon(), pokemon -> pokemon.getBattlePokemon() != null).size();
        int foeTeamSize = CollectionsKt.filter(foeSide.getActiveClientBattlePokemon(), pokemon -> pokemon.getBattlePokemon() != null).size();

        int allyIndex = 0;
        int foeIndex = 0;

        for (ActiveClientBattlePokemon target : this.targets) {
            if (target.getBattlePokemon() == null) continue;
            boolean isAlly = target.isAllied(request.getActivePokemon());

            int teamSize;
            int sideIndex;
            if (isAlly) {
                teamSize = allyTeamSize;
                sideIndex = allyIndex++;
            } else {
                teamSize = foeTeamSize;
                sideIndex = foeIndex++;
            }

            int fieldPos = isAlly ? sideIndex % teamSize : teamSize - 1 - (sideIndex % teamSize);
            boolean verticalAligned = false;

            float x;
            float y;
            ArrowDirection arrowDirection;

            if (verticalAligned) {
                x = this.getX() + ((float) Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2) + (isAlly ? -(TARGET_WIDTH + MOVE_HORIZONTAL_SPACING) : MOVE_HORIZONTAL_SPACING);
                y = this.getY() + (5 + BACKGROUND_HEIGHT - (TARGET_HEIGHT + MOVE_VERTICAL_SPACING) * teamSize) / 2 + (TARGET_HEIGHT + MOVE_VERTICAL_SPACING) * fieldPos;
                arrowDirection = isAlly ? ArrowDirection.RIGHT : ArrowDirection.LEFT;
            }
            else {
                x = this.getX() + (float) Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - (TARGET_WIDTH + MOVE_HORIZONTAL_SPACING) * teamSize / 2 + MOVE_HORIZONTAL_SPACING + fieldPos * TARGET_WIDTH;
                y = this.getY() + 44 + (isAlly ? TARGET_HEIGHT + MOVE_VERTICAL_SPACING : 0F);
                if (fieldPos == 0) arrowDirection = ArrowDirection.RIGHT;
                else if (fieldPos == teamSize - 1) arrowDirection = ArrowDirection.LEFT;
                else arrowDirection = isAlly ? ArrowDirection.UP : ArrowDirection.DOWN;
            }

            baseTiles.add(((BattleTargetSelection) (Object) this).new TargetTile((BattleTargetSelection) (Object) this, target, x, y, arrowDirection));
        }

        this.baseTiles = baseTiles;
        this.targetTiles = baseTiles;
    }
}
