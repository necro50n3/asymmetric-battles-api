package com.necro.asymmetric.battles.common.util;

import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.ShowdownMoveset;
import org.jetbrains.annotations.NotNull;

public interface IMultiAttacker {
    String aba_toMultiAttackShowdownString(ActiveBattlePokemon activeBattlePokemon, @NotNull ShowdownMoveset showdownMoveSet);
}
