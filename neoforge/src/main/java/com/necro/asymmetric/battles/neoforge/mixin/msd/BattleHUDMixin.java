package com.necro.asymmetric.battles.neoforge.mixin.msd;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.client.battle.ClientBattleSide;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.github.yajatkaul.mega_showdown.api.lilycobble.networking.battle.BattlePokemonState;
import com.github.yajatkaul.mega_showdown.api.lilycobble.networking.battle.BattleSideState;
import com.github.yajatkaul.mega_showdown.client.battle.hud.BattleHUD;
import com.github.yajatkaul.mega_showdown.client.battle.storage.BattlePokemonMemory;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mixin(BattleHUD.class)
public class BattleHUDMixin {
    @Shadow
    private static BattlePokemonMemory getOrCreateMemory(ActiveClientBattlePokemon pokemon) {
        throw new AssertionError();
    }

    @Inject(method = "drawStatChanges", at = @At("HEAD"), cancellable = true)
    private static void disableStatChangesForRaids(GuiGraphics context, ActiveClientBattlePokemon pokemon, boolean isLeft, int rank, boolean isCompact, CallbackInfo ci) {
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (battle == null) return;
        if (battle.getBattleFormat().getBattleType().getName().equalsIgnoreCase("raid")) ci.cancel();
    }

    @WrapOperation(
        method = {"receivePacket", "lambda$receivePacket$0", "lambda$receivePacket$1"},
        at = @At(
            value = "INVOKE",
            target = "Lcom/github/yajatkaul/mega_showdown/api/lilycobble/networking/battle/BattleSideState;getPokemon()Ljava/util/List;"
        )
    )
    private static List<BattlePokemonState> filterAlliesOnPacket(BattleSideState instance, Operation<List<BattlePokemonState>> original) {
        List<BattlePokemonState> all = original.call(instance);
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (Minecraft.getInstance().player == null || battle == null || !battle.getBattleFormat().getBattleType().getName().equalsIgnoreCase("raid")) return all;
        else if (battle.getSpectating()) return List.of();

        UUID player = Minecraft.getInstance().player.getUUID();
        boolean isAlly = instance.actors().stream().anyMatch(state -> state.uuid().equals(player));
        ClientBattleActor actor = battle.getParticipatingActor(player);

        Set<UUID> valid = new HashSet<>();
        if (isAlly && actor != null) {
            ClientBattleSide allySide = actor.getSide();
            for (ActiveClientBattlePokemon pokemon : allySide.getActiveClientBattlePokemon()) {
                if (pokemon == null || pokemon.getBattlePokemon() == null) continue;
                ClientParty party = CobblemonClient.INSTANCE.getStorage().getParty();
                if (party.getPosition(pokemon.getBattlePokemon().getUuid()) != -1) valid.add(pokemon.getBattlePokemon().getUuid());
                else getOrCreateMemory(pokemon);
            }
        }
        else if (actor != null) {
            ClientBattleSide opposingSide = actor.getSide() == battle.getSide1() ? battle.getSide2() : battle.getSide1();
            for (ActiveClientBattlePokemon pokemon : opposingSide.getActiveClientBattlePokemon()) {
                if (pokemon != null && pokemon.getBattlePokemon() != null) valid.add(pokemon.getBattlePokemon().getUuid());
            }
        }
        else return all;

        return all.stream().filter(s -> valid.contains(s.uuid())).toList();
    }
}
