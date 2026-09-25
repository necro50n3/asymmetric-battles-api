package com.necro.asymmetric.battles.common.api;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonMemories;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.cobblemon.mod.common.battles.ai.RandomBattleAI;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.npc.NPCBattleActor;
import com.cobblemon.mod.common.entity.npc.NPCEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.asymmetric.battles.common.api.actor.DummyBattleActor;
import com.necro.asymmetric.battles.common.api.actor.HordeBattleActor;
import kotlin.Unit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/*
 * Translation layer from various entities to a BattleActor.
 */

@FunctionalInterface
public interface BattleParticipant<T extends BattleActor> {
    T toActor();

    // For in-built RCT API compatibility use RCTBattleParticipant.

    static BattleParticipant<PlayerBattleActor> player(ServerPlayer player, @Nullable UUID leadingPokemon, boolean cloneParties, boolean healFirst, int adjustLevel) {
        List<BattlePokemon> battleTeam = PlayerExtensionsKt.party(player).toBattleTeam(cloneParties || adjustLevel > 0, healFirst, leadingPokemon);
        battleTeam.sort(Comparator.comparing(pokemon -> pokemon.getHealth() <= 0));
        return () -> new PlayerBattleActor(player.getUUID(), battleTeam);
    }

    static BattleParticipant<PlayerBattleActor> player(ServerPlayer player, @Nullable UUID leadingPokemon, boolean cloneParties, boolean healFirst) {
        return player(player, leadingPokemon, cloneParties, healFirst, -1);
    }

    static BattleParticipant<PlayerBattleActor> player(ServerPlayer player, @Nullable UUID leadingPokemon, int adjustLevel) {
        return player(player, leadingPokemon, false, false, adjustLevel);
    }

    static BattleParticipant<PlayerBattleActor> player(ServerPlayer player, @Nullable UUID leadingPokemon) {
        return player(player, leadingPokemon, -1);
    }

    static BattleParticipant<PokemonBattleActor> wild(PokemonEntity pokemonEntity) {
        return wild(pokemonEntity, new RandomBattleAI());
    }

    static BattleParticipant<PokemonBattleActor>  wild(Pokemon pokemon) {
        return wild(pokemon, new RandomBattleAI());
    }

    static BattleParticipant<PokemonBattleActor>  wild(PokemonEntity pokemonEntity, BattleAI battleAI) {
        return wild(pokemonEntity.getPokemon(), battleAI);
    }

    static BattleParticipant<PokemonBattleActor>  wild(Pokemon pokemon, BattleAI battleAI) {
        return () -> new PokemonBattleActor(
            pokemon.getUuid(),
            new BattlePokemon(pokemon, pokemon, p -> Unit.INSTANCE),
            Cobblemon.config.getDefaultFleeDistance(),
            battleAI
        );
    }

    static BattleParticipant<NPCBattleActor> npc(NPCEntity npc) {
        assert npc.getParty() != null && npc.getSkill() != null;
        return () -> new NPCBattleActor(npc, npc.getParty(), npc.getSkill());
    }

    static BattleParticipant<HordeBattleActor> horde(List<PokemonEntity> pokemonList) {
        return horde(pokemonList, Cobblemon.config.getDefaultFleeDistance() * 2);
    }

    static BattleParticipant<HordeBattleActor> horde(List<PokemonEntity> pokemonList, float fleeDistance) {
        List<BattlePokemon> hordeTeam = pokemonList.stream().map(pokemon -> new BattlePokemon(pokemon.getPokemon(), pokemon.getPokemon(), p -> Unit.INSTANCE)).toList();
        BattlePokemon leader = hordeTeam.getFirst();
        return () -> new HordeBattleActor(leader.getUuid(), leader, hordeTeam, fleeDistance);
    }

    static @Nullable BattleParticipant<HordeBattleActor> horde(PokemonEntity pokemonEntity) {
        return horde(pokemonEntity, Cobblemon.config.getDefaultFleeDistance() * 2);
    }

    static @Nullable BattleParticipant<HordeBattleActor> horde(PokemonEntity pokemonEntity, float fleeDistance) {
        int herdSize = pokemonEntity.getBrain().hasMemoryValue(CobblemonMemories.HERD_SIZE) ? pokemonEntity.getBrain().getMemory(CobblemonMemories.HERD_SIZE).orElse(0) : 0;
        String herdLeader = pokemonEntity.getBrain().hasMemoryValue(CobblemonMemories.HERD_LEADER) ? pokemonEntity.getBrain().getMemory(CobblemonMemories.HERD_LEADER).orElse(null) : null;
        if (herdSize == 0 && herdLeader == null) return null;

        PokemonEntity leaderEntity;
        List<PokemonEntity> pokemonList = new ArrayList<>();
        if (herdLeader == null) leaderEntity = pokemonEntity;
        else leaderEntity = (PokemonEntity) ((ServerLevel) pokemonEntity.level()).getEntity(UUID.fromString(herdLeader));

        if (leaderEntity == null || !leaderEntity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)) return null;
        pokemonList.add(leaderEntity);
        leaderEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElseThrow().findAll(entity -> {
            if (!entity.getBrain().hasMemoryValue(CobblemonMemories.HERD_LEADER)) return false;
            return entity.getBrain().getMemory(CobblemonMemories.HERD_LEADER).orElseThrow().equalsIgnoreCase(leaderEntity.getStringUUID());
        }).forEach(entity -> {
            if (pokemonList.size() < 6) pokemonList.add((PokemonEntity) entity);
        });

        return horde(pokemonList, fleeDistance);
    }

    static BattleParticipant<DummyBattleActor> dummy(UUID uuid) {
        return () -> new DummyBattleActor(uuid);
    }

    static BattleParticipant<DummyBattleActor> dummy() {
        return dummy(UUID.randomUUID());
    }
}
