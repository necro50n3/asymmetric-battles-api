package com.necro.asymmetric.battles.common.api;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.cobblemon.mod.common.battles.ai.RandomBattleAI;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.asymmetric.battles.common.actor.DummyBattleActor;
import kotlin.Unit;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class AsymmetricBattleBuilder {
    // Start a 1v1 wild battle in a Multi Battle format.
    public static BattleStartResult pveMulti1v1(ServerPlayer player, UUID dummyAlly, PokemonEntity pokemonEntity, UUID dummyFoe, @Nullable UUID leadingPokemon) {
        List<BattlePokemon> battleTeam = PlayerExtensionsKt.party(player).toBattleTeam(false, false, leadingPokemon);
        battleTeam.sort(Comparator.comparing(pokemon -> pokemon.getHealth() <= 0));
        PlayerBattleActor playerActor = new PlayerBattleActor(player.getUUID(), battleTeam);
        PokemonBattleActor wildActor = new PokemonBattleActor(
            pokemonEntity.getPokemon().getUuid(),
            new BattlePokemon(pokemonEntity.getPokemon(), pokemonEntity.getPokemon(), p -> Unit.INSTANCE),
            Cobblemon.config.getDefaultFleeDistance(),
            new RandomBattleAI()
        );
        BattleFormat battleFormat = BattleFormat.Companion.getGEN_9_MULTI();
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

        playerActor.setBattleTheme(pokemonEntity.getBattleTheme());

        if (errors.isEmpty()) {
            return BattleRegistry.startBattle(
                battleFormat,
                new BattleSide(playerActor, new DummyBattleActor(dummyAlly)),
                new BattleSide(wildActor, new DummyBattleActor(dummyFoe)),
                true
            );
        }
        else return errors;
    }
}
