package com.necro.asymmetric.battles.common.api;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.npc.NPCBattleActor;
import com.necro.asymmetric.battles.common.api.actor.HordeBattleActor;
import com.necro.asymmetric.battles.common.battle.InvalidDummyActorError;
import com.necro.asymmetric.battles.common.api.actor.DummyBattleActor;
import com.necro.asymmetric.battles.common.util.PokemonLocatorUtils;
import kotlin.Unit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/*
 * Start Basic Battles, Multi Battles or Horde Battles.
 */

public class AsymmetricBattleBuilder {
    public static BattleStartResult battle(BattleParticipant<PlayerBattleActor> p1, BattleParticipant<? extends BattleActor> p2, BattleFormat format) {
        return battle(p1, p2, format, -1);
    }

    public static BattleStartResult battle(BattleParticipant<PlayerBattleActor> p1, BattleParticipant<? extends BattleActor> p2, BattleFormat format, int adjustLevel) {
        return battle(p1.toActor(), p2.toActor(), format, adjustLevel);
    }

    public static BattleStartResult battle(PlayerBattleActor p1, BattleActor p2, BattleFormat format, int adjustLevel) {
        List<PlayerPartyStore> battlePartyStores = new ArrayList<>();
        ErroredBattleStart errors = new ErroredBattleStart();

        checkPlayerActor(p1, errors, getBattleTheme(p2), adjustLevel, battlePartyStores);
        checkPlayerActor(p2, errors, getBattleTheme(p1), adjustLevel, battlePartyStores);

        if (errors.isEmpty()) {
            BattleSide side1 = new BattleSide(p1);
            BattleSide side2 = new BattleSide(p2);
            PokemonLocatorUtils.rearrangeBattle(side1, side2);
            return BattleRegistry.startBattle(format, side1, side2, true)
                .ifSuccessful(battle -> {
                    battle.getBattlePartyStores().addAll(battlePartyStores);
                    return Unit.INSTANCE;
                });
        }
        else return errors;
    }

    public static BattleStartResult multiBattle(BattleParticipant<? extends BattleActor> p1, BattleParticipant<? extends BattleActor> p2, BattleParticipant<? extends BattleActor> p3, BattleParticipant<? extends BattleActor> p4) {
        return multiBattle(p1, p2, p3, p4, -1);
    }

    public static BattleStartResult multiBattle(BattleParticipant<? extends BattleActor> p1, BattleParticipant<? extends BattleActor> p2, BattleParticipant<? extends BattleActor> p3, BattleParticipant<? extends BattleActor> p4, int adjustLevel) {
        return multiBattle(p1.toActor(), p2.toActor(), p3.toActor(), p4.toActor(), adjustLevel);
    }

    public static BattleStartResult multiBattle(BattleParticipant<? extends BattleActor> p1, BattleParticipant<? extends BattleActor> p2) {
        return multiBattle(p1, p2, -1);
    }

    public static BattleStartResult multiBattle(BattleParticipant<? extends BattleActor> p1, BattleParticipant<? extends BattleActor> p2, int adjustLevel) {
        return multiBattle(p1.toActor(), p2.toActor(), BattleParticipant.dummy().toActor(), BattleParticipant.dummy().toActor(), adjustLevel);
    }

    public static BattleStartResult multiBattle(BattleActor p1, BattleActor p2, BattleActor p3, BattleActor p4, int adjustLevel) {
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
            BattleSide side1 = new BattleSide(p1, p3);
            BattleSide side2 = new BattleSide(p2, p4);
            PokemonLocatorUtils.rearrangeBattle(side1, side2);
            return BattleRegistry.startBattle(BattleFormat.Companion.getGEN_9_MULTI(), side1, side2, true)
                .ifSuccessful(battle -> {
                    battle.getBattlePartyStores().addAll(battlePartyStores);

//                    PokemonBattleActor pokemonActor = (PokemonBattleActor) p2;
//                    PokemonEntity original = pokemonActor.getEntity();
//                    PokemonEntity entity = original.level().getNearestEntity(
//                        PokemonEntity.class,
//                        TargetingConditions.DEFAULT,
//                        original,
//                        original.getX(),
//                        original.getY(),
//                        original.getZ(),
//                        original.getBoundingBox().inflate(8, 2, 8)
//                    );
//                    if (entity == null) return Unit.INSTANCE;
//                    Pokemon pokemon = entity.getPokemon();
//
//                    PokemonBattleActor newActor = new PokemonBattleActor(
//                        pokemon.getUuid(),
//                        new BattlePokemon(pokemon, pokemon, p -> Unit.INSTANCE),
//                        Cobblemon.config.getDefaultFleeDistance(),
//                        new RandomBattleAI()
//                    );
//                    AsymmetricAPI.setMultiBattleActor(newActor, battle, 4);

                    return Unit.INSTANCE;
                });
        }
        else return errors;
    }

    public static BattleStartResult hordeBattle(BattleParticipant<PlayerBattleActor> p1, BattleParticipant<HordeBattleActor> p2) {
        return hordeBattle(p1.toActor(), p2.toActor());
    }

    public static BattleStartResult hordeBattle(PlayerBattleActor p1, HordeBattleActor p2) {
        BattleFormat battleFormat = AsymmetricBattleFormats.GEN_9_HORDE;
        ErroredBattleStart errors = new ErroredBattleStart();

        checkPlayerActor(p1, errors, getBattleTheme(p2), -1, List.of());

        if (errors.isEmpty()) {
            BattleSide side1 = new BattleSide(p1);
            BattleSide side2 = new BattleSide(p2);
            PokemonLocatorUtils.rearrangeBattle(side1, side2);
            return BattleRegistry.startBattle(battleFormat, side1, side2, true);
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
            case HordeBattleActor hordeActor when hordeActor.getEntity() != null -> hordeActor.getEntity().getBattleTheme();
            case null, default -> null;
        };
    }
}
