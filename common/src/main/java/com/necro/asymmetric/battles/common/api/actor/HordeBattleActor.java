package com.necro.asymmetric.battles.common.api.actor;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor;
import com.cobblemon.mod.common.api.battles.model.actor.FleeableBattleActor;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.api.net.NetworkPacket;
import com.cobblemon.mod.common.battles.actor.MultiPokemonBattleActor;
import com.cobblemon.mod.common.battles.ai.RandomBattleAI;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.net.messages.client.battle.BattleEndPacket;
import com.necro.asymmetric.battles.common.api.BattleParticipant;
import kotlin.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * A specialized {@link MultiPokemonBattleActor} representing a coordinated team of wild Pokémon in Horde Battles.
 * @see com.necro.asymmetric.battles.common.api.AsymmetricBattleFormats#GEN_9_HORDE
 * @see com.necro.asymmetric.battles.common.api.AsymmetricBattleBuilder#hordeBattle(BattleParticipant, BattleParticipant)
 */
public class HordeBattleActor extends MultiPokemonBattleActor implements FleeableBattleActor, EntityBackedBattleActor<PokemonEntity> {
    private BattlePokemon leader;
    private final float fleeDistance;

    /**
     * Full constructor for a horde battle actor.
     *
     * @param uuid         The unique identifier of the horde actor.
     * @param leader       The primary {@link BattlePokemon} acting as the leader.
     * @param pokemonList  The full roster of wild Pokémon participating in the horde.
     * @param fleeDistance Maximum distance challenger can move away before the horde flees.
     * @param battleAI     The AI decider for turn action selections across the horde.
     */
    public HordeBattleActor(@NotNull UUID uuid, BattlePokemon leader, @NotNull List<? extends BattlePokemon> pokemonList, float fleeDistance, @NotNull BattleAI battleAI) {
        super(pokemonList, battleAI, uuid);
        this.leader = leader;
        this.fleeDistance = fleeDistance;
    }

    /**
     * Constructs a horde actor with custom flee distance and default {@link RandomBattleAI}.
     *
     * @param uuid         The unique identifier.
     * @param leader       The leader battle Pokémon.
     * @param pokemonList  The full list of battle Pokémon.
     * @param fleeDistance Maximum flee distance.
     */
    public HordeBattleActor(@NotNull UUID uuid, BattlePokemon leader, @NotNull List<? extends BattlePokemon> pokemonList, float fleeDistance) {
        this(uuid, leader, pokemonList, fleeDistance, new RandomBattleAI());
    }

    /**
     * Constructs a horde actor with custom AI and default flee distance (2x standard wild flee distance).
     *
     * @param uuid        The unique identifier.
     * @param leader      The leader battle Pokémon.
     * @param pokemonList The full list of battle Pokémon.
     * @param battleAI    The AI controller.
     */
    public HordeBattleActor(@NotNull UUID uuid, BattlePokemon leader, @NotNull List<? extends BattlePokemon> pokemonList, @NotNull BattleAI battleAI) {
        this(uuid, leader, pokemonList, Cobblemon.config.getDefaultFleeDistance() * 2, battleAI);
    }

    /**
     * Constructs a horde actor with default flee distance and {@link RandomBattleAI}.
     *
     * @param uuid        The unique identifier.
     * @param leader      The leader battle Pokémon.
     * @param pokemonList The full list of battle Pokémon.
     */
    public HordeBattleActor(@NotNull UUID uuid, BattlePokemon leader, @NotNull List<? extends BattlePokemon> pokemonList) {
        this(uuid, leader, pokemonList, new RandomBattleAI());
    }

    @Override
    public @NotNull ActorType getType() {
        return ActorType.WILD;
    }

    @Override
    public @NotNull MutableComponent getName() {
        return this.leader.getEffectedPokemon().getSpecies().getTranslatedName();
    }

    @Override
    public @NotNull MutableComponent nameOwned(@NotNull String name) {
        return Component.literal(name);
    }

    @Override
    public float getFleeDistance() {
        return this.fleeDistance;
    }

    @Override
    public @Nullable Pair<ServerLevel, Vec3> getWorldAndPosition() {
        ServerPlayer ownerPlayer = this.leader.getEffectedPokemon().getOwnerPlayer();
        if (ownerPlayer != null) return new Pair<>(ownerPlayer.serverLevel(), ownerPlayer.position());

        PokemonEntity entity = this.leader.getEntity();
        assert entity != null;
        ServerLevel level = (ServerLevel) entity.level();
        return new Pair<>(level, entity.position());
    }

    @Override
    public void sendUpdate(@NotNull NetworkPacket<?> packet) {
        super.sendUpdate(packet);
        if (packet instanceof BattleEndPacket) {
            for (BattlePokemon pokemon : this.getPokemonList()) {
                if (pokemon.getEntity() == null) continue;
                pokemon.getEntity().setBattleId(null);
            }
        }
    }

    /**
     * @return The current horde leader {@link BattlePokemon}.
     */
    public BattlePokemon getLeader() {
        return this.leader;
    }

    /**
     * Updates the designated leader of the horde.
     *
     * @param pokemon The new leader.
     */
    public void setLeader(BattlePokemon pokemon) {
        this.leader = pokemon;
    }

    @Override
    public @Nullable PokemonEntity getEntity() {
        return this.leader.getEntity();
    }

    @Override
    public @Nullable Vec3 getInitialPos() {
        return this.leader.getEntity() != null ? this.leader.getEntity().position() : null;
    }
}
