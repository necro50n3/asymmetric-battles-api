package com.necro.asymmetric.battles.common.api;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.cobblemon.mod.common.battles.ai.RandomBattleAI;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.npc.NPCBattleActor;
import com.cobblemon.mod.common.entity.npc.NPCEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.asymmetric.battles.common.actor.DummyBattleActor;
import kotlin.Unit;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@FunctionalInterface
public interface BattleParticipant {
    BattleActor toActor();

    static BattleParticipant player(ServerPlayer player, @Nullable UUID leadingPokemon, boolean cloneParties, boolean healFirst, int adjustLevel) {
        List<BattlePokemon> battleTeam = PlayerExtensionsKt.party(player).toBattleTeam(cloneParties || adjustLevel > 0, healFirst, leadingPokemon);
        battleTeam.sort(Comparator.comparing(pokemon -> pokemon.getHealth() <= 0));
        return () -> new PlayerBattleActor(player.getUUID(), battleTeam);
    }

    static BattleParticipant player(ServerPlayer player, @Nullable UUID leadingPokemon, boolean cloneParties, boolean healFirst) {
        return player(player, leadingPokemon, cloneParties, healFirst, -1);
    }

    static BattleParticipant player(ServerPlayer player, @Nullable UUID leadingPokemon, int adjustLevel) {
        return player(player, leadingPokemon, false, false, adjustLevel);
    }

    static BattleParticipant player(ServerPlayer player, @Nullable UUID leadingPokemon) {
        return player(player, leadingPokemon, -1);
    }

    static BattleParticipant wild(PokemonEntity pokemonEntity) {
        return wild(pokemonEntity.getPokemon());
    }

    static BattleParticipant wild(Pokemon pokemon) {
        return () -> new PokemonBattleActor(
            pokemon.getUuid(),
            new BattlePokemon(pokemon, pokemon, p -> Unit.INSTANCE),
            Cobblemon.config.getDefaultFleeDistance(),
            new RandomBattleAI()
        );
    }

    static BattleParticipant npc(NPCEntity npc) {
        assert npc.getParty() != null && npc.getSkill() != null;
        return () -> new NPCBattleActor(npc, npc.getParty(), npc.getSkill());
    }

    static BattleParticipant dummy(UUID uuid) {
        return () -> new DummyBattleActor(uuid);
    }

    static BattleParticipant dummy() {
        return dummy(UUID.randomUUID());
    }
}
