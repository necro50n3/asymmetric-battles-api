package com.necro.asymmetric.battles.common.api;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.evolution.progress.EvolutionProgress;
import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools;
import com.cobblemon.mod.common.api.spawning.SpawnCause;
import com.cobblemon.mod.common.api.spawning.spawner.BasicSpawner;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.runner.ShowdownService;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.evolution.progress.LastBattleCriticalHitsEvolutionProgress;
import com.necro.asymmetric.battles.common.api.actor.DummyBattleActor;
import com.necro.asymmetric.battles.common.api.spawning.BattleSpawnPool;
import com.necro.asymmetric.battles.common.api.spawning.BattleSpawnablePosition;
import com.necro.asymmetric.battles.common.network.AsymmetricNetworkMessages;
import com.necro.asymmetric.battles.common.registry.SpawnPoolTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Core runtime API for dynamically modifying active Cobblemon battles.
 * <p>
 * This class provides utility functions to join or substitute a {@link BattleActor}
 * into an ongoing Multi Battle without restarting the battle engine.
 */
public class AsymmetricAPI {
    public static BasicSpawner SPAWNER;

    /**
     * Dynamically injects a new {@link BattleActor} into an ongoing Multi Battle at the specified side slot.
     * @param actor  The {@link BattleActor} joining the battle (Player, NPC, Wild, or Horde actor).
     * @param battle The active {@link PokemonBattle} instance.
     * @param slot   The battle side slot (1 to 4):
     *               <ul>
     *                 <li><b>Side 1 (Ally Team):</b>
     *                   <ul>
     *                     <li>{@code slot = 1} (Showdown {@code p1}): Primary actor (Index 0).</li>
     *                     <li>{@code slot = 3} (Showdown {@code p3}): Ally actor (Index 1).</li>
     *                   </ul>
     *                 </li>
     *                 <li><b>Side 2 (Opposing Team):</b>
     *                   <ul>
     *                     <li>{@code slot = 2} (Showdown {@code p2}): Primary opponent (Index 0).</li>
     *                     <li>{@code slot = 4} (Showdown {@code p4}): Ally opponent (Index 1).</li>
     *                   </ul>
     *                 </li>
     *               </ul>
     *
     * @see AsymmetricBattleBuilder#multiBattle
     */
    public static void setMultiBattleActor(BattleActor actor, PokemonBattle battle, int slot) {
        if (!battle.getFormat().getBattleType().getName().equalsIgnoreCase("multi")) return;
        int index = slot == 1 || slot == 2 ? 0 : 1;
        setBattleActor(actor, battle, slot, index);
    }

    /**
     * Dynamically injects a new {@link BattleActor} into an ongoing Raid Battle at the specified slot.
     * @param actor  The {@link BattleActor} joining the battle (Player, NPC, Trainer).
     * @param battle The active {@link PokemonBattle} instance.
     * @param slot   The battle side slot (1 to 4):
     *               <ul>
     *                 <li><b>Side 1 (Ally Team):</b>
     *                   <ul>
     *                     <li>{@code slot = 1} (Showdown {@code p2}): Raid participant (Index 0).</li>
     *                     <li>{@code slot = 2} (Showdown {@code p3}): Raid participant (Index 1).</li>
     *                     <li>{@code slot = 3} (Showdown {@code p4}): Raid participant (Index 2).</li>
     *                     <li>{@code slot = 4} (Showdown {@code p5}): Raid participant (Index 3).</li>
     *                   </ul>
     *                 </li>
     *                 <li><b>Side 2 (Opposing Team):</b>
     *                   <ul>
     *                     <li>{@code slot = N/A} (Showdown {@code p1}): Raid boss (Index 0).</li>
     *                   </ul>
     *                 </li>
     *               </ul>
     */
    public static void setRaidBattleActor(BattleActor actor, PokemonBattle battle, int slot) {
        if (!battle.getFormat().getBattleType().getName().equalsIgnoreCase("raid")) return;
        setBattleActor(actor, battle, 1, slot);
    }

    /**
     * Dynamically injects a new {@link BattleActor} into an ongoing Raid Battle at the next empty slot.
     * @param actor  The {@link BattleActor} joining the battle (Player, NPC, Trainer).
     * @param battle The active {@link PokemonBattle} instance.
     *
     * @see AsymmetricAPI#setRaidBattleActor(BattleActor, PokemonBattle, int)
     */
    public static void setRaidBattleActor(BattleActor actor, PokemonBattle battle) {
        int index = -1;
        BattleActor[] actors = battle.getSide1().getActors();
        for (int i = 0; i < actors.length; i++) {
            if (actors[i] instanceof DummyBattleActor) {
                index = i;
                break;
            }
        }

        if (index == -1) return;
        setRaidBattleActor(actor, battle, index);
    }

    private static void setBattleActor(BattleActor actor, PokemonBattle battle, int side, int slot) {
        if (battle.getEnded()) return;

        BattleSide battleSide = side == 1 || side == 3 ? battle.getSide1() : battle.getSide2();
        battleSide.getActors()[slot] = actor;
        actor.setBattle(battle);
        actor.setShowdownId("p" + side);
        for (int i = 0; i < battle.getFormat().getBattleType().getSlotsPerActor(); i++) {
            actor.getActivePokemon().add(new ActiveBattlePokemon(actor, actor.getPokemonList().get(i)));
        }
        actor.getPokemonList().forEach(pokemon -> {
            if (pokemon.getEntity() != null) pokemon.getEntity().setBattleId(battle.getBattleId());
            pokemon.getEffectedPokemon().getEvolutionProxy().current().progress()
                .stream().filter(LastBattleCriticalHitsEvolutionProgress.class::isInstance)
                .forEach(EvolutionProgress::reset);
        });

        List<String> messages = new ArrayList<>();
        messages.add(String.format(
            ">insertactor %1$d %2$d %3$s %4$s",
            side - 1,
            actor.getPokemonList().size(),
            actor.getUuid(),
            BattleRegistry.INSTANCE.packTeam(actor.getPokemonList())
        ));

        ShowdownService.Companion.getService().send(battle.getBattleId(), messages.toArray(new String[]{}));
        for (BattleActor iterActor : battle.getActors()) {
            if (!(iterActor instanceof PlayerBattleActor player)) return;
            AsymmetricNetworkMessages.BATTLE_ACTOR_UPDATE.accept(player.getEntity(), side, slot, player.getSide() == battleSide, actor);
        }
    }

    public static @Nullable Pokemon getRandomBattleSpawn(@Nullable BattleSpawnPool pool, ServerPlayer player, ServerLevel level, BlockPos blockPos, PokemonBattle battle, PokemonProperties rootProperties, int baseLevel, Supplier<Pokemon> defaultSpawn) {
        if (pool == null) return defaultSpawn.get();
        SpawnCause cause = new SpawnCause(SPAWNER, player);
        BattleSpawnablePosition spawnablePosition = new BattleSpawnablePosition(cause, level, blockPos, List.of(), battle, rootProperties, baseLevel);
        Pokemon pokemon = pool.getRandom(spawnablePosition, player);
        return pokemon != null ? pokemon : defaultSpawn.get();
    }

    public static @Nullable Pokemon getRandomBattleSpawn(@Nullable BattleSpawnPool pool, ServerPlayer player, ServerLevel level, BlockPos blockPos, PokemonBattle battle, PokemonProperties rootProperties, int baseLevel) {
        return getRandomBattleSpawn(pool, player, level, blockPos, battle, rootProperties, baseLevel, () -> null);
    }

    public static @Nullable Pokemon getRandomBattleSpawn(String type, ServerPlayer player, ServerLevel level, BlockPos blockPos, PokemonBattle battle, PokemonProperties rootProperties, int baseLevel, Supplier<Pokemon> defaultSpawn) {
        return getRandomBattleSpawn(SpawnPoolTypeRegistry.get(type, rootProperties), player, level, blockPos, battle, rootProperties, baseLevel, defaultSpawn);
    }

    public static @Nullable Pokemon getRandomBattleSpawn(String type, ServerPlayer player, ServerLevel level, BlockPos blockPos, PokemonBattle battle, PokemonProperties rootProperties, int baseLevel) {
        return getRandomBattleSpawn(type, player, level, blockPos, battle, rootProperties, baseLevel, () -> null);
    }

    public static void onServerStart() {
        SPAWNER = new BasicSpawner("battle", CobblemonSpawnPools.WORLD_SPAWN_POOL, 0, Map.of());
    }
}
