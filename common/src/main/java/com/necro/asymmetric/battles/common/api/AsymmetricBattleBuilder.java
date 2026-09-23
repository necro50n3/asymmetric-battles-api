package com.necro.asymmetric.battles.common.api;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.npc.NPCBattleActor;
import com.necro.asymmetric.battles.common.battle.InvalidDummyActorError;
import com.necro.asymmetric.battles.common.battle.DummyBattleActor;
import kotlin.Unit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AsymmetricBattleBuilder {
    public static BattleStartResult multiBattle(BattleParticipant p1, BattleParticipant p2, BattleParticipant p3, BattleParticipant p4, int adjustLevel) {
        return multiBattleCommon(p1.toActor(), p2.toActor(), p3.toActor(), p4.toActor(), adjustLevel);
    }

    public static BattleStartResult multiBattle(BattleParticipant p1, BattleParticipant p2, BattleParticipant p3, BattleParticipant p4) {
        return multiBattle(p1, p2, p3, p4, -1);
    }
    public static BattleStartResult multiBattle(BattleParticipant p1, BattleParticipant p4, int adjustLevel) {
        return multiBattleCommon(p1.toActor(), p4.toActor(), BattleParticipant.dummy().toActor(), BattleParticipant.dummy().toActor(), adjustLevel);
    }

    public static BattleStartResult multiBattle(BattleParticipant p1, BattleParticipant p2) {
        return multiBattle(p1, p2, -1);
    }

    private static BattleStartResult multiBattleCommon(BattleActor p1, BattleActor p2, BattleActor p3, BattleActor p4, int adjustLevel) {
        List<PlayerPartyStore> battlePartyStores = new ArrayList<>();
        ErroredBattleStart errors = new ErroredBattleStart();

        if (p1 instanceof DummyBattleActor || p2 instanceof DummyBattleActor) {
            errors.getGeneralErrors().add(new InvalidDummyActorError());
        }
        else {
            ResourceLocation side1Theme = getBattleTheme(p1);
            ResourceLocation side2Theme = getBattleTheme(p2);
            checkPlayerActor(p1, errors, side2Theme, adjustLevel, battlePartyStores);
            checkPlayerActor(p2, errors, side2Theme, adjustLevel, battlePartyStores);
            checkPlayerActor(p3, errors, side1Theme, adjustLevel, battlePartyStores);
            checkPlayerActor(p4, errors, side1Theme, adjustLevel, battlePartyStores);
        }

        if (errors.isEmpty()) {
            return BattleRegistry.startBattle(BattleFormat.Companion.getGEN_9_MULTI(), new BattleSide(p1, p3), new BattleSide(p2, p4), true)
                .ifSuccessful(battle -> {
                    battle.getBattlePartyStores().addAll(battlePartyStores);
                    return Unit.INSTANCE;
                });
        }
        else return errors;
    }

    private static void checkPlayerActor(BattleActor actor, ErroredBattleStart errors, @Nullable ResourceLocation battleTheme, int adjustLevel, List<PlayerPartyStore> battlePartyStores) {
        if (!(actor instanceof PlayerBattleActor playerActor)) return;
        ServerPlayer player = playerActor.getEntity();
        if (player == null) return;

        List<BattlePokemon> battleTeam = playerActor.getPokemonList();

        if (adjustLevel > 0) {
            PlayerPartyStore tempStore = new PlayerPartyStore(player.getUUID());
            for (int i = 0; i < battleTeam.size(); i++) {
                BattlePokemon battlePokemon = battleTeam.get(i);
                battlePokemon.getEffectedPokemon().setLevel(adjustLevel);
                battlePokemon.getEffectedPokemon().heal();
                tempStore.set(i, battlePokemon.getEffectedPokemon());
            }
            battlePartyStores.add(tempStore);
        }

        if (!battleTeam.isEmpty() && battleTeam.getFirst().getHealth() <= 0) {
            errors.getParticipantErrors().get(playerActor).add(BattleStartError.Companion.insufficientPokemon(
                player,
                BattleFormat.Companion.getGEN_9_MULTI().getBattleType().getSlotsPerActor(),
                playerActor.getPokemonList().size()
            ));
        }

        if (playerActor.getPokemonList().stream().anyMatch(battlePokemon -> battlePokemon.getEntity() != null && battlePokemon.getEntity().isBusy())) {
            errors.getParticipantErrors().get(playerActor).add(BattleStartError.Companion.targetIsBusy(player.getDisplayName()));
        }

        if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
            errors.getParticipantErrors().get(playerActor).add(BattleStartError.Companion.alreadyInBattle(playerActor));
        }

        if (battleTheme != null) playerActor.setBattleTheme(battleTheme);
    }

    private static ResourceLocation getBattleTheme(BattleActor actor) {
        return switch (actor) {
            case PlayerBattleActor playerActor -> playerActor.getBattleTheme();
            case PokemonBattleActor pokemonActor when pokemonActor.getEntity() != null -> pokemonActor.getEntity().getBattleTheme();
            case NPCBattleActor npcActor -> npcActor.getEntity().getBattleTheme();
            case null, default -> null;
        };
    }
}
