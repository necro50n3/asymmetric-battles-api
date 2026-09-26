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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Functional factory and conversion layer transforming in-game entities into Cobblemon {@link BattleActor} instances.
 * <p>
 * For in-built RCT API compatibility use RCTBattleParticipant.
 *
 * @param <T> The concrete type of {@link BattleActor} produced by this participant.
 */
@FunctionalInterface
public interface BattleParticipant<T extends BattleActor> {

    /**
     * Resolves and instantiates the underlying {@link BattleActor}.
     *
     * @return The constructed {@link BattleActor} instance.
     */
    T toActor();

    //

    /**
     * Creates a {@link PlayerBattleActor} participant.
     *
     * @param player         The server player.
     * @param leadingPokemon The UUID of the Pokémon that should lead the party, or {@code null} for the first valid.
     * @param cloneParties   Whether to clone the party data.
     * @param healFirst      Whether to fully restore the party's HP and status before entering battle.
     * @param adjustLevel    Target level to scale all Pokémon to, or {@code -1} for unchanged levels.
     * @return A participant supplying a {@link PlayerBattleActor}.
     */
    static BattleParticipant<PlayerBattleActor> player(ServerPlayer player, @Nullable UUID leadingPokemon, boolean cloneParties, boolean healFirst, int adjustLevel) {
        List<BattlePokemon> battleTeam = PlayerExtensionsKt.party(player).toBattleTeam(cloneParties || adjustLevel > 0, healFirst, leadingPokemon);
        battleTeam.sort(Comparator.comparing(pokemon -> pokemon.getHealth() <= 0));
        return () -> new PlayerBattleActor(player.getUUID(), battleTeam);
    }

    /**
     * Creates a {@link PlayerBattleActor} participant.
     *
     * @param player         The server player.
     * @param leadingPokemon The UUID of the Pokémon that should lead the party, or {@code null} for the first valid.
     * @param cloneParties   Whether to clone the party data.
     * @param healFirst      Whether to fully restore the party's HP and status before entering battle.
     * @return A participant supplying a {@link PlayerBattleActor}.
     */
    static BattleParticipant<PlayerBattleActor> player(ServerPlayer player, @Nullable UUID leadingPokemon, boolean cloneParties, boolean healFirst) {
        return player(player, leadingPokemon, cloneParties, healFirst, -1);
    }

    /**
     * Creates a {@link PlayerBattleActor} participant.
     *
     * @param player         The server player.
     * @param leadingPokemon The UUID of the Pokémon that should lead the party, or {@code null} for the first valid.
     * @param adjustLevel    Target level to scale all Pokémon to, or {@code -1} for unchanged levels.
     * @return A participant supplying a {@link PlayerBattleActor}.
     */
    static BattleParticipant<PlayerBattleActor> player(ServerPlayer player, @Nullable UUID leadingPokemon, int adjustLevel) {
        return player(player, leadingPokemon, false, false, adjustLevel);
    }

    /**
     * Creates a {@link PlayerBattleActor} participant.
     *
     * @param player         The server player.
     * @param leadingPokemon The UUID of the Pokémon that should lead the party, or {@code null} for the first valid.
     * @return A participant supplying a {@link PlayerBattleActor}.
     */
    static BattleParticipant<PlayerBattleActor> player(ServerPlayer player, @Nullable UUID leadingPokemon) {
        return player(player, leadingPokemon, -1);
    }

    /**
     * Creates a wild {@link PokemonBattleActor} participant from an in-world Pokémon entity.
     *
     * @param pokemonEntity The wild Pokémon entity.
     * @return A participant supplying a {@link PokemonBattleActor}.
     */
    static BattleParticipant<PokemonBattleActor> wild(PokemonEntity pokemonEntity) {
        return wild(pokemonEntity, new RandomBattleAI());
    }

    /**
     * Creates a wild {@link PokemonBattleActor} participant from a Pokémon instance.
     *
     * @param pokemon The Pokémon instance.
     * @return A participant supplying a {@link PokemonBattleActor}.
     */
    static BattleParticipant<PokemonBattleActor> wild(Pokemon pokemon) {
        return wild(pokemon, new RandomBattleAI());
    }

    /**
     * Creates a wild {@link PokemonBattleActor} participant from an in-world Pokémon entity.
     *
     * @param pokemonEntity The wild Pokémon entity.
     * @param battleAI      The AI controller determining move selections.
     * @return A participant supplying a {@link PokemonBattleActor}.
     */
    static BattleParticipant<PokemonBattleActor> wild(PokemonEntity pokemonEntity, BattleAI battleAI) {
        return wild(pokemonEntity.getPokemon(), battleAI);
    }

    /**
     * Creates a wild {@link PokemonBattleActor} participant from a Pokémon instance.
     *
     * @param pokemon  The Pokémon instance.
     * @param battleAI The AI controller determining move selections.
     * @return A participant supplying a {@link PokemonBattleActor}.
     */
    static BattleParticipant<PokemonBattleActor> wild(Pokemon pokemon, BattleAI battleAI) {
        return () -> new PokemonBattleActor(
            pokemon.getUuid(),
            new BattlePokemon(pokemon, pokemon, p -> Unit.INSTANCE),
            Cobblemon.config.getDefaultFleeDistance(),
            battleAI
        );
    }

    /**
     * Creates an {@link NPCBattleActor} participant from a trainer NPC entity.
     *
     * @param npc The NPC entity.
     * @return A participant supplying an {@link NPCBattleActor}.
     */
    static BattleParticipant<NPCBattleActor> npc(@NotNull NPCEntity npc) {
        return () -> {
            assert npc.getParty() != null && npc.getSkill() != null;
            return new NPCBattleActor(npc, npc.getParty(), npc.getSkill());
        };
    }

    /**
     * Creates a {@link HordeBattleActor} participant from an explicit list of wild Pokémon entities.
     *
     * @param pokemonList The list of wild Pokémon entities (first entity becomes the horde leader).
     * @return A participant supplying a {@link HordeBattleActor}.
     */
    static BattleParticipant<HordeBattleActor> horde(List<PokemonEntity> pokemonList) {
        return horde(pokemonList, Cobblemon.config.getDefaultFleeDistance());
    }

    /**
     * Creates a {@link HordeBattleActor} participant from an explicit list of wild Pokémon entities.
     *
     * @param pokemonList  The list of wild Pokémon entities (first entity becomes the horde leader).
     * @param fleeDistance The maximum distance before the horde flees.
     * @return A participant supplying a {@link HordeBattleActor}.
     */
    static BattleParticipant<HordeBattleActor> horde(List<PokemonEntity> pokemonList, float fleeDistance) {
        List<BattlePokemon> hordeTeam = pokemonList.stream().map(pokemon -> new BattlePokemon(pokemon.getPokemon(), pokemon.getPokemon(), p -> Unit.INSTANCE)).toList();
        BattlePokemon leader = hordeTeam.getFirst();
        return () -> new HordeBattleActor(leader.getUuid(), leader, hordeTeam, fleeDistance);
    }

    /**
     * Creates a {@link HordeBattleActor} participant from a target Pokémon entity by querying herd memory.
     *
     * @param pokemonEntity The leader or member entity of the wild herd.
     * @return A participant supplying a {@link HordeBattleActor}, or {@code null} if the entity is not part of a valid herd or has no visible members.
     */
    static @Nullable BattleParticipant<HordeBattleActor> horde(PokemonEntity pokemonEntity) {
        return horde(pokemonEntity, Cobblemon.config.getDefaultFleeDistance());
    }

    /**
     * Creates a {@link HordeBattleActor} participant from a target Pokémon entity by querying herd memory.
     *
     * @param pokemonEntity The leader or member entity of the wild herd.
     * @param fleeDistance  The maximum distance before the horde flees.
     * @return A participant supplying a {@link HordeBattleActor}, or {@code null} if the entity is not part of a valid herd or has no visible members.
     */
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

    /**
     * Creates a {@link DummyBattleActor} participant.
     *
     * @param uuid The unique identifier for the dummy placeholder.
     * @return A participant supplying a {@link DummyBattleActor}.
     */
    static BattleParticipant<DummyBattleActor> dummy(UUID uuid) {
        return () -> new DummyBattleActor(uuid);
    }

    /**
     * Creates a {@link DummyBattleActor} participant.
     *
     * @return A participant supplying a {@link DummyBattleActor}.
     */
    static BattleParticipant<DummyBattleActor> dummy() {
        return dummy(UUID.randomUUID());
    }
}
