package com.necro.asymmetric.battles.common.mixin.interaction;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.net.NetworkPacket;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.battles.ChallengeManager;
import com.cobblemon.mod.common.net.messages.server.BattleChallengePacket;
import com.cobblemon.mod.common.net.serverhandling.ChallengeHandler;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.asymmetric.battles.common.battle.MultiBattleJoin;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChallengeHandler.class)
public abstract class ChallengeHandlerMixin {
    @Inject(method = "handle*", at = @At("HEAD"), remap = false, cancellable = true)
    private void replaceChallenge(@Coerce NetworkPacket<?> p, MinecraftServer server, ServerPlayer player, CallbackInfo ci) {
        if (!(p instanceof BattleChallengePacket packet)) return;
        Entity entity = player.level().getEntity(packet.getTargetedEntityId());
        if (!(entity instanceof ServerPlayer target)) return;

        PokemonBattle battle =  BattleRegistry.getBattleByParticipatingPlayer(target);
        if (battle == null || !battle.isPvW() || !battle.getFormat().getBattleType().getName().equals("multi")) return;

        Pokemon leadingPokemon = PlayerExtensionsKt.party(player).get(packet.getSelectedPokemonId());
        if (leadingPokemon == null) return;

        ChallengeManager.BattleChallenge challenge = new MultiBattleJoin(player, target, leadingPokemon.getUuid());
        ChallengeManager.INSTANCE.sendRequest(challenge);
        ci.cancel();
    }
}
