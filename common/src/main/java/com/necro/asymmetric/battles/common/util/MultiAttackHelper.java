package com.necro.asymmetric.battles.common.util;

import com.cobblemon.mod.common.api.pokemon.feature.IntSpeciesFeature;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.pokemon.Pokemon;

public class MultiAttackHelper {
    public static int attackCount(Pokemon pokemon) {
        IntSpeciesFeature feature = pokemon.getFeature("attacks_per_turn");
        return feature != null ? feature.getValue() : 1;
    }

    public static boolean canMultiAttack(Pokemon pokemon) {
        return attackCount(pokemon) > 1;
    }
}
