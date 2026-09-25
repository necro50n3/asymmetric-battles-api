package com.necro.asymmetric.battles.common.api;

import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.battles.BattleRules;
import com.cobblemon.mod.common.battles.BattleType;
import com.cobblemon.mod.common.battles.BattleTypes;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.Set;

public class AsymmetricBattleFormats {
    public static final BattleType QUADRUPLES = BattleTypes.INSTANCE.makeBattleType("quadruples", Component.translatable("asymmetricbattles.battle.types.quadruples"), 1, 4);
    public static final BattleType PENTUPLES = BattleTypes.INSTANCE.makeBattleType("pentuples", Component.translatable("asymmetricbattles.battle.types.pentuples"), 1, 5);
    public static final BattleType SEXTUPLES = BattleTypes.INSTANCE.makeBattleType("sextuples", Component.translatable("asymmetricbattles.battle.types.sextuples"), 1, 6);
    public static final BattleType HORDE = BattleTypes.INSTANCE.makeBattleType("horde", Component.translatable("asymmetricbattles.battle.types.sextuples"), 1, 6);

    public static final BattleFormat GEN_9_QUADRUPLES = new BattleFormat(
        "cobblemon",
        QUADRUPLES,
        new HashSet<>(Set.of(BattleRules.OBTAINABLE, BattleRules.PAST, BattleRules.UNOBTAINABLE)),
        9,
        -1
    );
    public static final BattleFormat GEN_9_PENTUPLES = new BattleFormat(
        "cobblemon",
        PENTUPLES,
        new HashSet<>(Set.of(BattleRules.OBTAINABLE, BattleRules.PAST, BattleRules.UNOBTAINABLE)),
        9,
        -1
    );
    public static final BattleFormat GEN_9_SEXTUPLES = new BattleFormat(
        "cobblemon",
        SEXTUPLES,
        new HashSet<>(Set.of(BattleRules.OBTAINABLE, BattleRules.PAST, BattleRules.UNOBTAINABLE)),
        9,
        -1
    );
    public static final BattleFormat GEN_9_HORDE = new BattleFormat(
        "cobblemon",
        HORDE,
        new HashSet<>(Set.of(BattleRules.OBTAINABLE, BattleRules.PAST, BattleRules.UNOBTAINABLE)),
        9,
        -1
    );
}
