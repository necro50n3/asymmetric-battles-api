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

/**
 * Factory and orchestrator for initiating asymmetric and standard Pokémon battles.
 * <p>
 * Supports building:
 * <ul>
 *   <li><b>Standard Battles:</b> Any battle format between players, NPCs, or wild Pokémon.</li>
 *   <li><b>Multi Battles:</b> 2v2 battles supporting dynamic ally join-in via {@link DummyBattleActor} placeholders.</li>
 *   <li><b>Horde Battles:</b> 1vX horde battles against multiple Pokémon at the same time.</li>
 * </ul>
 */
public class AsymmetricBattleBuilder {

    /**
     * Starts a standard battle between a player and an opposing participant.
     *
     * @param p1     The initiating player participant.
     * @param p2     The opposing participant (Player, NPC, Wild Pokémon, etc.).
     * @param format The battle format / ruleset to use (e.g. {@code GEN_9_SINGLES}, {@code GEN_9_QUADRUPLES}).
     * @return A {@link BattleStartResult} containing the started battle or validation errors.
     * @see com.necro.asymmetric.battles.common.api.AsymmetricBattleFormats
     */
    public static BattleStartResult battle(BattleParticipant<PlayerBattleActor> p1, BattleParticipant<? extends BattleActor> p2, BattleFormat format) {
        return battle(p1, p2, format, -1);
    }

    /**
     * Starts a standard battle between a player and an opposing participant.
     *
     * @param p1          The initiating player participant.
     * @param p2          The opposing participant (Player, NPC, Wild Pokémon, etc.).
     * @param format      The battle format / ruleset to use (e.g. {@code GEN_9_SINGLES}, {@code GEN_9_QUADRUPLES}).
     * @param adjustLevel The level to scale all player Pokémon to (e.g. 50), or {@code -1} for no adjustment.
     * @return A {@link BattleStartResult} containing the started battle or validation errors.
     * @see com.necro.asymmetric.battles.common.api.AsymmetricBattleFormats
     */
    public static BattleStartResult battle(BattleParticipant<PlayerBattleActor> p1, BattleParticipant<? extends BattleActor> p2, BattleFormat format, int adjustLevel) {
        return battle(p1.toActor(), p2.toActor(), format, adjustLevel);
    }

    /**
     * Starts a standard battle between a player and an opposing participant.
     *
     * @param p1          The initiating player participant.
     * @param p2          The opposing participant (Player, NPC, Wild Pokémon, etc.).
     * @param format      The battle format / ruleset to use (e.g. {@code GEN_9_SINGLES}, {@code GEN_9_QUADRUPLES}).
     * @param adjustLevel The level to scale all player Pokémon to (e.g. 50), or {@code -1} for no adjustment.
     * @return A {@link BattleStartResult} containing the started battle or validation errors.
     * @see com.necro.asymmetric.battles.common.api.AsymmetricBattleFormats
     */
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

    /**
     * Starts a 4-participant Multi Battle.
     *
     * @param p1 Side 1 Primary actor (Slot 1).
     * @param p2 Side 2 Primary actor (Slot 2).
     * @param p3 Side 1 Ally actor (Slot 3).
     * @param p4 Side 2 Ally actor (Slot 4).
     * @return A {@link BattleStartResult} containing the started battle or validation errors.
     */
    public static BattleStartResult multiBattle(BattleParticipant<? extends BattleActor> p1, BattleParticipant<? extends BattleActor> p2, BattleParticipant<? extends BattleActor> p3, BattleParticipant<? extends BattleActor> p4) {
        return multiBattle(p1, p2, p3, p4, -1);
    }

    /**
     * Starts a 4-participant Multi Battle.
     *
     * @param p1          Side 1 Primary actor (Slot 1).
     * @param p2          Side 2 Primary actor (Slot 2).
     * @param p3          Side 1 Ally actor (Slot 3).
     * @param p4          Side 2 Ally actor (Slot 4).
     * @param adjustLevel The level to scale all player Pokémon to (e.g. 50), or {@code -1} for no adjustment.
     * @return A {@link BattleStartResult} containing the started battle or validation errors.
     */
    public static BattleStartResult multiBattle(BattleParticipant<? extends BattleActor> p1, BattleParticipant<? extends BattleActor> p2, BattleParticipant<? extends BattleActor> p3, BattleParticipant<? extends BattleActor> p4, int adjustLevel) {
        return multiBattle(p1.toActor(), p2.toActor(), p3.toActor(), p4.toActor(), adjustLevel);
    }

    /**
     * Starts an open Multi Battle with 2 initial primary participants.
     * <p>
     * Slots 3 and 4 are populated with {@link DummyBattleActor} instances, allowing additional actors
     * to join mid-battle via {@link AsymmetricAPI#setMultiBattleActor}.
     *
     * @param p1 Side 1 Primary actor (Slot 1).
     * @param p2 Side 2 Primary actor (Slot 2).
     * @return A {@link BattleStartResult} containing the started battle or validation errors.
     */
    public static BattleStartResult multiBattle(BattleParticipant<? extends BattleActor> p1, BattleParticipant<? extends BattleActor> p2) {
        return multiBattle(p1, p2, -1);
    }

    /**
     * Starts an open Multi Battle with 2 initial primary participants, placeholder ally slots, and level adjustment.
     *
     * @param p1 Side 1 Primary actor (Slot 1).
     * @param p2 Side 2 Primary actor (Slot 2).
     * @param adjustLevel The level to scale all player Pokémon to (e.g. 50), or {@code -1} for no adjustment.
     * @return A {@link BattleStartResult} containing the started battle or validation errors.
     */
    public static BattleStartResult multiBattle(BattleParticipant<? extends BattleActor> p1, BattleParticipant<? extends BattleActor> p2, int adjustLevel) {
        return multiBattle(p1.toActor(), p2.toActor(), BattleParticipant.dummy().toActor(), BattleParticipant.dummy().toActor(), adjustLevel);
    }

    /**
     * Starts an open Multi Battle with 2 initial primary participants, placeholder ally slots, and level adjustment.
     *
     * @param p1          Side 1 Primary actor (Slot 1).
     * @param p2          Side 2 Primary actor (Slot 2).
     * @param p3          Side 1 Ally actor (Slot 3).
     * @param p4          Side 2 Ally actor (Slot 4).
     * @param adjustLevel The level to scale all player Pokémon to (e.g. 50), or {@code -1} for no adjustment.
     * @return A {@link BattleStartResult} containing the started battle or validation errors.
     */
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
                    return Unit.INSTANCE;
                });
        }
        else return errors;
    }

    /**
     * Starts a Horde Battle between a player and a wild Pokémon horde.
     *
     * @param p1 The player participant.
     * @param p2 The wild horde.
     * @return A {@link BattleStartResult} containing the started battle or validation errors.
     */
    public static BattleStartResult hordeBattle(BattleParticipant<PlayerBattleActor> p1, BattleParticipant<HordeBattleActor> p2) {
        return hordeBattle(p1.toActor(), p2.toActor());
    }

    /**
     * Starts a Horde Battle between a player and a wild Pokémon horde.
     *
     * @param p1 The player participant.
     * @param p2 The wild horde.
     * @return A {@link BattleStartResult} containing the started battle or validation errors.
     */
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

    /**
     * Validates a player actor's battle eligibility, applies level adjustments to a temporary party store,
     * checks for busy/fainted Pokémon, and configures the opposing battle BGM theme.
     *
     * @param actor              The actor to validate (ignored if not a {@link PlayerBattleActor}).
     * @param errors             The error accumulator.
     * @param battleTheme        The battle theme BGM to assign to the player.
     * @param adjustLevel        Target level scaling, or {@code -1}.
     * @param battlePartyStores  Output list storing temporary cloned party data for level-scaled battles.
     */
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

    /**
     * Resolves the custom battle BGM theme associated with an actor or its underlying entity.
     *
     * @param actor The battle actor to query.
     * @return The {@link ResourceLocation} of the battle theme, or {@code null} if none is specified.
     */
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
