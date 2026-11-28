package com.necro.asymmetric.battles.common.mixin.client;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.keybind.keybinds.PartySendBinding;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PartySendBinding.class)
public abstract class PartySendBindingMixin {
    @Shadow
    protected abstract void checkForTargetInteractions(LocalPlayer player, Pokemon selectedPartyPokemon);

    @Inject(method = "toggleBattleScreen", at = @At("HEAD"), remap = false, cancellable = true)
    private void shiftKeyToWheel(CallbackInfo ci) {
        if (CobblemonClient.INSTANCE.getRequests().getBattleChallenges().isEmpty() || !Minecraft.getInstance().player.isCrouching()) return;

        Pokemon selectedPartyPokemon = null;
        if (CobblemonClient.INSTANCE.getStorage().getSelectedSlot() >= 0) {
            selectedPartyPokemon = CobblemonClient.INSTANCE.getStorage().getParty().get(CobblemonClient.INSTANCE.getStorage().getSelectedSlot());
        } else {
            for (Pokemon pokemon : CobblemonClient.INSTANCE.getStorage().getParty()) {
                if (pokemon == null) continue;
                selectedPartyPokemon = pokemon;
                break;
            }
        }
        if (selectedPartyPokemon == null) return;
        this.checkForTargetInteractions(Minecraft.getInstance().player, selectedPartyPokemon);
        ci.cancel();
    }
}
