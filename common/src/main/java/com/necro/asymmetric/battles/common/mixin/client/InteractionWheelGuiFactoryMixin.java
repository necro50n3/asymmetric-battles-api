package com.necro.asymmetric.battles.common.mixin.client;

import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.gui.interact.battleRequest.BattleConfigureGUI;
import com.cobblemon.mod.common.client.gui.interact.wheel.InteractWheelGuiFactoryKt;
import com.cobblemon.mod.common.client.gui.interact.wheel.InteractWheelOption;
import com.cobblemon.mod.common.client.gui.interact.wheel.Orientation;
import com.cobblemon.mod.common.net.messages.client.PlayerInteractOptionsPacket;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.sugar.Local;
import com.necro.asymmetric.battles.common.battle.AsymmetricOptions;
import kotlin.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static com.cobblemon.mod.common.util.MiscUtilsKt.cobblemonResource;

@Mixin(InteractWheelGuiFactoryKt.class)
public class InteractionWheelGuiFactoryMixin {
    @ModifyVariable(method = "createPlayerInteractGui", at = @At(value = "STORE"), remap = false, name = "options")
    private static Multimap<Orientation, InteractWheelOption> addJoinBattleOption(Multimap<Orientation, InteractWheelOption> options, @Local(argsOnly = true) PlayerInteractOptionsPacket packet) {
        InteractWheelOption joinOption = new InteractWheelOption(
            cobblemonResource("textures/gui/interact/interact_wheel_icon_battle.png"),
            cobblemonResource("textures/gui/interact/interact_wheel_icon_exclamation.png"),
            true,
            "asymmetricbattles.ui.interact.join",
            () -> null,
            () -> {
                BattleConfigureGUI.Companion.sendBattleRequest(BattleFormat.Companion.getGEN_9_MULTI(), packet);
                return Unit.INSTANCE;
            }
        );

        if (packet.getOptions().get(AsymmetricOptions.JOIN_BATTLE) == PlayerInteractOptionsPacket.OptionStatus.AVAILABLE) options.put(Orientation.NORTHWEST, joinOption);
        if (CobblemonClient.INSTANCE.getBattle() != null) options.removeAll(Orientation.NORTHEAST);
        return options;
    }
}
