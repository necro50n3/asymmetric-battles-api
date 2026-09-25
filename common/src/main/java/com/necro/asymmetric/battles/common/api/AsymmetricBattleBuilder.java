package com.necro.asymmetric.battles.common.api;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonMemories;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.storage.party.PartyStore;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.cobblemon.mod.common.battles.ai.RandomBattleAI;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.npc.NPCBattleActor;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.asymmetric.battles.common.api.actor.HordeBattleActor;
import com.necro.asymmetric.battles.common.battle.InvalidDummyActorError;
import com.necro.asymmetric.battles.common.api.actor.DummyBattleActor;
import kotlin.Unit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/*
 * Start Multi Battles or Horde Battles.
 */

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

    public static BattleStartResult hordeBattleFromHerd(ServerPlayer player, PokemonEntity pokemonEntity, @Nullable UUID leadingPokemon) {
        return hordeBattleFromHerd(player, pokemonEntity, leadingPokemon, false, false);
    }

    public static BattleStartResult hordeBattleFromHerd(ServerPlayer player, PokemonEntity pokemonEntity, @Nullable UUID leadingPokemon, boolean cloneParties, boolean healFirst) {
        return hordeBattleFromHerd(player, pokemonEntity, leadingPokemon, cloneParties, healFirst, Cobblemon.config.getDefaultFleeDistance());
    }

    public static BattleStartResult hordeBattleFromHerd(ServerPlayer player, PokemonEntity pokemonEntity, @Nullable UUID leadingPokemon, boolean cloneParties, boolean healFirst, float fleeDistance) {
        return hordeBattleFromHerd(player, pokemonEntity, leadingPokemon, cloneParties, healFirst, fleeDistance, PlayerExtensionsKt.party(player));
    }

    public static BattleStartResult hordeBattleFromHerd(ServerPlayer player, PokemonEntity pokemonEntity, @Nullable UUID leadingPokemon, boolean cloneParties, boolean healFirst, float fleeDistance, PartyStore party) {
        int herdSize = pokemonEntity.getBrain().hasMemoryValue(CobblemonMemories.HERD_SIZE) ? pokemonEntity.getBrain().getMemory(CobblemonMemories.HERD_SIZE).orElse(0) : 0;
        String herdLeader = pokemonEntity.getBrain().hasMemoryValue(CobblemonMemories.HERD_LEADER) ? pokemonEntity.getBrain().getMemory(CobblemonMemories.HERD_LEADER).orElse(null) : null;
        if (herdSize == 0 && herdLeader == null) return null;

        PokemonEntity leader;
        List<PokemonEntity> horde = new ArrayList<>();
        if (herdLeader == null) leader = pokemonEntity;
        else leader = (PokemonEntity) ((ServerLevel) pokemonEntity.level()).getEntity(UUID.fromString(herdLeader));

        if (leader == null || !leader.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)) return null;
        horde.add(leader);
        leader.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElseThrow().findAll(entity -> {
            if (!entity.getBrain().hasMemoryValue(CobblemonMemories.HERD_LEADER)) return false;
            return entity.getBrain().getMemory(CobblemonMemories.HERD_LEADER).orElseThrow().equalsIgnoreCase(leader.getStringUUID());
        }).forEach(entity -> {
            if (horde.size() < 6) horde.add((PokemonEntity) entity);
        });

        return hordeBattle(player, horde, leadingPokemon, cloneParties, healFirst, fleeDistance, party);
    }

    public static BattleStartResult hordeBattle(ServerPlayer player, List<PokemonEntity> horde, @Nullable UUID leadingPokemon) {
        return hordeBattle(player, horde, leadingPokemon, false, false);
    }

    public static BattleStartResult hordeBattle(ServerPlayer player, List<PokemonEntity> horde, @Nullable UUID leadingPokemon, boolean cloneParties, boolean healFirst) {
        return hordeBattle(player, horde, leadingPokemon, cloneParties, healFirst, Cobblemon.config.getDefaultFleeDistance());
    }

    public static BattleStartResult hordeBattle(ServerPlayer player, List<PokemonEntity> horde, @Nullable UUID leadingPokemon, boolean cloneParties, boolean healFirst, float fleeDistance) {
        return hordeBattle(player, horde, leadingPokemon, cloneParties, healFirst, fleeDistance, PlayerExtensionsKt.party(player));
    }

    public static BattleStartResult hordeBattle(ServerPlayer player, List<PokemonEntity> horde, @Nullable UUID leadingPokemon, boolean cloneParties, boolean healFirst, float fleeDistance, PartyStore party) {
        List<BattlePokemon> battleTeam = party.toBattleTeam(cloneParties, healFirst, leadingPokemon);
        battleTeam.sort(Comparator.comparing(pokemon -> pokemon.getHealth() <= 0));
        PlayerBattleActor playerActor = new PlayerBattleActor(player.getUUID(), battleTeam);

        List<BattlePokemon> hordeTeam = horde.stream().map(pokemon -> new BattlePokemon(pokemon.getPokemon(), pokemon.getPokemon(), p -> Unit.INSTANCE)).toList();
        BattlePokemon leader = hordeTeam.getFirst();
        HordeBattleActor hordeActor = new HordeBattleActor(leader.getEffectedPokemon().getUuid(), leader, hordeTeam, fleeDistance);

        BattleFormat battleFormat = AsymmetricBattleFormats.GEN_9_HORDE;
        ErroredBattleStart errors = new ErroredBattleStart();

        if (!battleTeam.isEmpty() && battleTeam.getFirst().getHealth() <= 0) {
            errors.getParticipantErrors().get(playerActor).add(BattleStartError.Companion.insufficientPokemon(
                player,
                battleFormat.getBattleType().getSlotsPerActor(),
                playerActor.getPokemonList().size()
            ));
        }

        if (playerActor.getPokemonList().stream().anyMatch(battlePokemon -> battlePokemon.getEntity() != null && battlePokemon.getEntity().isBusy())) {
            errors.getParticipantErrors().get(playerActor).add(BattleStartError.Companion.targetIsBusy(player.getDisplayName()));
        }

        if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
            errors.getParticipantErrors().get(playerActor).add(BattleStartError.Companion.alreadyInBattle(playerActor));
        }

        playerActor.setBattleTheme(horde.getFirst().getBattleTheme());

        if (errors.isEmpty()) {
            return BattleRegistry.startBattle(battleFormat, new BattleSide(playerActor), new BattleSide(hordeActor), true)
                .ifSuccessful(battle -> {
                if (!cloneParties) horde.forEach(pokemon -> pokemon.setBattleId(battle.getBattleId()));
                return Unit.INSTANCE;
            });
        }
        else return errors;
    }
}
