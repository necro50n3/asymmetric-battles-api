package com.necro.asymmetric.battles.common.mixin.interaction;

import com.cobblemon.mod.common.battles.ChallengeManager;
import com.cobblemon.mod.common.battles.TeamManager;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.llamalad7.mixinextras.sugar.Local;
import com.necro.asymmetric.battles.common.api.AsymmetricBattleBuilder;
import com.necro.asymmetric.battles.common.api.BattleParticipant;
import kotlin.Unit;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mixin(ChallengeManager.class)
public class ChallengeManagerMixin {
    @ModifyVariable(method = "canAccept*", at = @At(value = "STORE"), remap = false, name = "players")
    private List<ServerPlayer> padExtraPlayers(List<ServerPlayer> players, @Local(name = "existingReceiverTeam") TeamManager.MultiBattleTeam receiverTeam, @Local(name = "existingSenderTeam") TeamManager.MultiBattleTeam senderTeam) {
        if (receiverTeam == null || senderTeam == null) return players;
        List<ServerPlayer> paddedPlayers = new ArrayList<>(players);
        if (senderTeam.getTeamPlayers().size() == 1) paddedPlayers.add(1, senderTeam.getTeamPlayers().getFirst());
        if (receiverTeam.getTeamPlayers().size() == 1) paddedPlayers.add(receiverTeam.getTeamPlayers().getFirst());
        return paddedPlayers;
    }

    @Inject(method = "onAccept(Lcom/cobblemon/mod/common/battles/ChallengeManager$BattleChallenge;)V", at = @At("HEAD"), remap = false, cancellable = true)
    private void overrideMultiBattleAccept(ChallengeManager.BattleChallenge request, CallbackInfo ci) {
        if (!(request instanceof ChallengeManager.MultiBattleChallenge multiBattleRequest)) return;
        List<ServerPlayer> side1 = multiBattleRequest.getReceiverTeam().getTeamPlayers();
        List<ServerPlayer> side2 = multiBattleRequest.getSenderTeam().getTeamPlayers();
        if (side1.isEmpty() || side2.isEmpty() || (side1.size() == 2 && side2.size() == 2)) return;

        BattleParticipant<?> p1 = BattleParticipant.player(side1.getFirst(), PlayerExtensionsKt.party(side1.getFirst()).toGappyList().stream().filter(Objects::nonNull).findFirst().orElseThrow().getUuid());
        BattleParticipant<?> p2 = BattleParticipant.player(side2.getFirst(), PlayerExtensionsKt.party(side2.getFirst()).toGappyList().stream().filter(Objects::nonNull).findFirst().orElseThrow().getUuid());
        BattleParticipant<?> p3 = side1.size() == 2
            ? BattleParticipant.player(side1.get(1), PlayerExtensionsKt.party(side1.get(1)).toGappyList().stream().filter(Objects::nonNull).findFirst().orElseThrow().getUuid())
            : BattleParticipant.dummy();
        BattleParticipant<?> p4 = side2.size() == 2
            ? BattleParticipant.player(side2.get(1), PlayerExtensionsKt.party(side2.get(1)).toGappyList().stream().filter(Objects::nonNull).findFirst().orElseThrow().getUuid())
            : BattleParticipant.dummy();

        AsymmetricBattleBuilder.multiBattle(p1, p2, p3, p4).ifErrored(error -> {
            error.sendTo(side1, component -> component);
            error.sendTo(side2, component -> component);
            return Unit.INSTANCE;
        });

        ci.cancel();
    }
}
