package com.necro.asymmetric.battles.common.mixin.client;

import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.battles.FleeAttemptActionResponse;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.client.battle.SingleActionRequest;
import com.cobblemon.mod.common.client.gui.battle.BattleGUI;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleGeneralActionSelection;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.cobblemon.mod.common.util.LocalizationUtilsKt.battleLang;

@Mixin(BattleGeneralActionSelection.class)
public abstract class BattleGeneralActionSelectionMixin {
    @Shadow
    protected abstract void addOption(int rank, MutableComponent text, ResourceLocation texture, Function0<Unit> onClick);

    @Shadow
    public abstract void playDownSound(@NotNull SoundManager soundManager);

    @Inject(method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/cobblemon/mod/common/client/CobblemonClient;getBattle()Lcom/cobblemon/mod/common/client/battle/ClientBattle;"
        ),
        cancellable = true
    )
    private void fixWildActionButtons(BattleGUI battleGUI, SingleActionRequest request, CallbackInfo ci) {
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (battle == null) return;
        List<ClientBattleActor> wildActors = battle.getSide2().getActors();
        if (wildActors.isEmpty() || wildActors.getFirst().getType() != ActorType.WILD) return;

        AtomicInteger active = new AtomicInteger();
        battle.getSide2().getActiveClientBattlePokemon().forEach(pokemon -> active.getAndIncrement());

        addOption(2, battleLang("ui.capture"), BattleGUI.Companion.getBagResource(), () -> {
            battle.setMinimised(true);
            String prompt = active.get() == 1 ? "throw_pokeball_prompt" : "pokeball_not_alone";
            if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.displayClientMessage(battleLang(prompt), false);
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return Unit.INSTANCE;
        });

        addOption(3, battleLang("ui.run"), BattleGUI.Companion.getRunResource(), () -> {
            battle.setMinimised(true);
            battleGUI.selectAction(request, new FleeAttemptActionResponse());
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return Unit.INSTANCE;
        });

        ci.cancel();
    }
}
