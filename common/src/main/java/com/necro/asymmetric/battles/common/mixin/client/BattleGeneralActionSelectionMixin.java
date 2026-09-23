package com.necro.asymmetric.battles.common.mixin.client;

import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.battles.FleeAttemptActionResponse;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.client.battle.SingleActionRequest;
import com.cobblemon.mod.common.client.gui.battle.BattleGUI;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleGeneralActionSelection;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

import static com.cobblemon.mod.common.util.LocalizationUtilsKt.battleLang;

@Mixin(BattleGeneralActionSelection.class)
public abstract class BattleGeneralActionSelectionMixin {
    @Shadow
    protected abstract void addOption(int rank, MutableComponent text, ResourceLocation texture, Function0<Unit> onClick);

    @Shadow
    public abstract void playDownSound(@NotNull SoundManager soundManager);

    @WrapOperation(method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/cobblemon/mod/common/client/CobblemonClient;getBattle()Lcom/cobblemon/mod/common/client/battle/ClientBattle;",
            ordinal = 1
        ),
        remap = false
    )
    private ClientBattle fixWildActionButtons(CobblemonClient instance, Operation<ClientBattle> original, @Local(argsOnly = true) BattleGUI battleGUI, @Local(argsOnly = true) SingleActionRequest request) {
        ClientBattle battle = original.call(instance);
        if (battle == null) return null;
        List<ClientBattleActor> wildActors = battle.getSide2().getActors();
        if (wildActors.isEmpty() || wildActors.stream().noneMatch(actor -> actor.getType() == ActorType.WILD)) return battle;

        int tempActive = 0;
        for (ActiveClientBattlePokemon pokemon : battle.getSide2().getActiveClientBattlePokemon()) {
            if (pokemon.getBattlePokemon() != null) tempActive++;
        }
        int active = tempActive;

        addOption(2, battleLang("ui.capture"), BattleGUI.Companion.getBagResource(), () -> {
            battle.setMinimised(true);
            String prompt = active == 1 ? "throw_pokeball_prompt" : "pokeball_not_alone";
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(battleLang(prompt), false);
            }
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return Unit.INSTANCE;
        });

        addOption(3, battleLang("ui.run"), BattleGUI.Companion.getRunResource(), () -> {
            battle.setMinimised(true);
            battleGUI.selectAction(request, new FleeAttemptActionResponse());
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return Unit.INSTANCE;
        });

        return null;
    }
}
