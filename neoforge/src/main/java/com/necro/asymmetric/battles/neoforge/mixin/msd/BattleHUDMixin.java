package com.necro.asymmetric.battles.neoforge.mixin.msd;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.client.battle.ClientBattleSide;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.github.yajatkaul.mega_showdown.api.lilycobble.networking.battle.BattlePokemonState;
import com.github.yajatkaul.mega_showdown.api.lilycobble.networking.battle.BattleSideState;
import com.github.yajatkaul.mega_showdown.client.battle.hud.BattleHUD;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Mixin(BattleHUD.class)
public class BattleHUDMixin {
    @WrapOperation(
        method = "tickLocalBattleInfo",
        at = @At(
            value = "INVOKE",
            target = "Lcom/cobblemon/mod/common/client/battle/ClientBattleSide;getActiveClientBattlePokemon()Ljava/lang/Iterable;"
        )
    )
    private static Iterable<ActiveClientBattlePokemon> filterAlliesOnTick(ClientBattleSide instance, Operation<Iterable<ActiveClientBattlePokemon>> original) {
        Iterable<ActiveClientBattlePokemon> all = original.call(instance);
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (Minecraft.getInstance().player == null || battle == null || !battle.getBattleFormat().getBattleType().getName().equalsIgnoreCase("raid")) return all;
        else if (battle.getSpectating()) return List.of();

        UUID player = Minecraft.getInstance().player.getUUID();
        boolean isAlly = instance.getActors().stream().anyMatch(actor -> actor.getUuid().equals(player));
        if (!isAlly) return all;

        return StreamSupport.stream(all.spliterator(), false).filter(pokemon -> pokemon.getActor().getUuid().equals(player)).toList();
    }

    @WrapOperation(
        method = {"receivePacket", "lambda$receivePacket$0", "lambda$receivePacket$1"},
        at = @At(
            value = "INVOKE",
            target = "Lcom/github/yajatkaul/mega_showdown/api/lilycobble/networking/battle/BattleSideState;getPokemon()Ljava/util/List;"
        )
    )
    private static List<BattlePokemonState> filterAlliesOnTickOnPacket(BattleSideState instance, Operation<List<BattlePokemonState>> original) {
        List<BattlePokemonState> all = original.call(instance);
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (Minecraft.getInstance().player == null || battle == null || !battle.getBattleFormat().getBattleType().getName().equalsIgnoreCase("raid")) return all;
        else if (battle.getSpectating()) return List.of();

        UUID player = Minecraft.getInstance().player.getUUID();
        boolean isAlly = instance.actors().stream().anyMatch(state -> state.uuid().equals(player));
        ClientBattleActor actor = battle.getParticipatingActor(player);

        Set<UUID> valid = new HashSet<>();
        if (isAlly) {
            for (Pokemon pokemon : CobblemonClient.INSTANCE.getStorage().getParty()) {
                if (pokemon != null) valid.add(pokemon.getUuid());
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
