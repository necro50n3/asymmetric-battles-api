package com.necro.asymmetric.battles.common.mixin.battle;

import com.cobblemon.mod.common.battles.*;
import com.necro.asymmetric.battles.common.util.IMultiAttacker;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MoveActionResponse.class)
public abstract class MoveActionResponseMixin extends ShowdownActionResponse implements IMultiAttacker {
    @Shadow
    private String moveName;
    @Shadow
    private String targetPnx;

    public MoveActionResponseMixin() {
        super(ShowdownActionResponseType.MOVE);
    }

    @Override
    public String aba_toMultiAttackShowdownString(ActiveBattlePokemon activeBattlePokemon, @NotNull ShowdownMoveset showdownMoveSet) {
        String pnx = this.targetPnx;
        int moveIndex = 0;
        for (int i = 0; i < showdownMoveSet.getMoves().size(); i++) {
            if (showdownMoveSet.getMoves().get(i).getId().equals(this.moveName)) {
                moveIndex = i + 1;
                break;
            }
        }

        if (pnx != null) {
            ActiveBattlePokemon targetPokemon = activeBattlePokemon.getBattle().getActorAndActiveSlotFromPNX(pnx).getSecond();
            String digit = targetPokemon.getSignedDigitRelativeTo(activeBattlePokemon);
            return "extramove " + moveIndex + " " + digit;
        } else {
            return "extramove " + moveIndex;
        }
    }
}
