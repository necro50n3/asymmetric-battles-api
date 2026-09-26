package com.necro.asymmetric.battles.common.util;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor;
import com.cobblemon.mod.common.battles.BattleSide;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.asymmetric.battles.common.api.actor.HordeBattleActor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PokemonLocatorUtils {
    public static void rearrangeBattle(BattleSide side1, BattleSide side2) {
        BattleActor[] actors1 = side1.getActors();
        BattleActor[] actors2 = side2.getActors();
        for (BattleActor actor : actors1) {
            if (actor instanceof HordeBattleActor) rearrangePokemon(actor.getPokemonList(), actors2);
        }
        for (BattleActor actor : actors2) {
            if (actor instanceof HordeBattleActor) rearrangePokemon(actor.getPokemonList(), actors1);
        }
        rearrangeActors(actors1, actors2);
    }

    private static void rearrangePokemon(@NotNull List<BattlePokemon> side1, @NotNull BattleActor[] side2) {
        if (side1.size() <= 1) return;

        List<Vec3> side1Positions = getPositions(side1);
        List<Vec3> side2Positions = getPositions(side2);

        Vec3 side1Center = getCenter(side1Positions);
        Vec3 side2Center = getCenter(side2Positions);

        sortPokemon(side1, side1Positions, side1Center, side2Center);
    }

    private static void rearrangeActors(@NotNull BattleActor[] side1, @NotNull BattleActor[] side2) {
        if (side1.length <= 1 && side2.length <= 1) return;

        List<Vec3> side1Positions = getPositions(side1);
        List<Vec3> side2Positions = getPositions(side2);

        Vec3 side1Center = getCenter(side1Positions);
        Vec3 side2Center = getCenter(side2Positions);

        sortActors(side1, side1Positions, side1Center, side2Center);
        sortActors(side2, side2Positions, side2Center, side1Center);
    }

    private static List<Vec3> getPositions(List<BattlePokemon> side) {
        return side.stream().map(pokemon -> {
            if (pokemon == null) return null;
            return pokemon.getEntity() != null ? pokemon.getEntity().position() : null;
        }).toList();
    }

    private static List<Vec3> getPositions(BattleActor[] side) {
        return Arrays.stream(side).map(actor -> {
            if (actor instanceof EntityBackedBattleActor<?> entity) return entity.getInitialPos();
            else return null;
        }).toList();
    }

    private static void sortPokemon(@NotNull List<BattlePokemon> side, List<Vec3> positions, Vec3 selfCenter, Vec3 targetCenter) {
        if (side.size() <= 1) return;
        List<BattlePokemon> sorted = sortCommon(side, positions, selfCenter, targetCenter);
        side.clear();
        side.addAll(sorted);
    }

    private static void sortActors(@NotNull BattleActor[] side, List<Vec3> positions, Vec3 selfCenter, Vec3 targetCenter) {
        if (side.length <= 1) return;
        List<BattleActor> sorted = sortCommon(Arrays.stream(side).toList(), positions, selfCenter, targetCenter);
        for (int i = 0; i < side.length; i++) side[i] = sorted.get(i);
    }

    private static <T> List<T> sortCommon(List<T> list, List<Vec3> positions, Vec3 selfCenter, Vec3 targetCenter) {
        List<ActorEntry<T>> entries = new ArrayList<>();
        List<T> failed = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            T actor = list.get(i);
            Vec3 pos = positions.get(i);
            if (pos == null) {
                failed.add(actor);
                continue;
            }
            double score = getLateralScore(pos, selfCenter, targetCenter);
            entries.add(new ActorEntry<>(actor, score));
        }
        entries.sort(Comparator.comparingDouble(ActorEntry::score));

        List<T> sorted = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) sorted.add(entries.get(i).actor());
        sorted.addAll(failed);
        return sorted;
    }

    private static Vec3 getCenter(List<Vec3> positions) {
        double sumX = 0;
        double sumY = 0;
        double sumZ = 0;
        int count = 0;
        for (Vec3 pos : positions) {
            if (pos != null) {
                sumX += pos.x;
                sumY += pos.y;
                sumZ += pos.z;
                count++;
            }
        }
        if (count == 0) return Vec3.ZERO;
        return new Vec3(sumX / count, sumY / count, sumZ / count);
    }

    private static double getLateralScore(Vec3 pos, Vec3 selfCenter, Vec3 targetCenter) {
        double dirX = targetCenter.x - selfCenter.x;
        double dirZ = targetCenter.z - selfCenter.z;
        double relX = pos.x - selfCenter.x;
        double relZ = pos.z - selfCenter.z;
        return dirX * relZ - dirZ * relX;
    }

    private record ActorEntry<T>(T actor, double score) {}
}
