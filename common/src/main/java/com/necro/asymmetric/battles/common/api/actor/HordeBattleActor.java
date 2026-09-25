package com.necro.asymmetric.battles.common.api.actor;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.battles.model.actor.FleeableBattleActor;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.ai.RandomBattleAI;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
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

/*
 * The HordeBattleActor is used in Horde Battles to represent a team of wild Pokemon.
 */

public class HordeBattleActor extends AIBattleActor implements FleeableBattleActor {
    public final BattlePokemon leader;
    private final float fleeDistance;

    public HordeBattleActor(@NotNull UUID uuid, BattlePokemon leader, @NotNull List<? extends BattlePokemon> pokemonList, float fleeDistance, @NotNull BattleAI battleAI) {
        super(uuid, pokemonList, battleAI);
        this.leader = leader;
        this.fleeDistance = fleeDistance;
    }

    public HordeBattleActor(@NotNull UUID uuid, BattlePokemon leader, @NotNull List<? extends BattlePokemon> pokemonList, float fleeDistance) {
        this(uuid, leader, pokemonList, fleeDistance, new RandomBattleAI());
    }

    public HordeBattleActor(@NotNull UUID uuid, BattlePokemon leader, @NotNull List<? extends BattlePokemon> pokemonList, @NotNull BattleAI battleAI) {
        this(uuid, leader, pokemonList, Cobblemon.config.getDefaultFleeDistance(), battleAI);
    }

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
}
