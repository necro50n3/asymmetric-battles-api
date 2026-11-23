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
    public static final BattleType HORDE = BattleTypes.INSTANCE.makeBattleType("horde", Component.translatable("asymmetricbattles.battle.types.horde"), 1, 6);
    public static final BattleType RAID = BattleTypes.INSTANCE.makeBattleType("raid", Component.translatable("asymmetricbattles.battle.types.raid"), 4, 1);

    public static final BattleType RAID_SINGLES = BattleTypes.INSTANCE.makeBattleType("raid_singles", Component.translatable("asymmetricbattles.battle.types.raid_singles"), 2, 1);
    public static final BattleType RAID_DOUBLES = BattleTypes.INSTANCE.makeBattleType("raid_doubles", Component.translatable("asymmetricbattles.battle.types.raid_doubles"), 2, 2);
    public static final BattleType RAID_TRIPLES = BattleTypes.INSTANCE.makeBattleType("raid_triples", Component.translatable("asymmetricbattles.battle.types.raid_triples"), 2, 3);
    public static final BattleType RAID_QUADRUPLES = BattleTypes.INSTANCE.makeBattleType("raid_quadruples", Component.translatable("asymmetricbattles.battle.types.raid_quadruples"), 2, 4);
    public static final BattleType RAID_PENTUPLES = BattleTypes.INSTANCE.makeBattleType("raid_pentuples", Component.translatable("asymmetricbattles.battle.types.raid_pentuples"), 2, 5);
    public static final BattleType RAID_SEXTUPLES = BattleTypes.INSTANCE.makeBattleType("raid_sextuples", Component.translatable("asymmetricbattles.battle.types.raid_sextuples"), 2, 6);

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
    public static final BattleFormat GEN_9_RAID = new BattleFormat(
        "cobblemon",
        RAID,
        new HashSet<>(Set.of(BattleRules.OBTAINABLE, BattleRules.PAST, BattleRules.UNOBTAINABLE)),
        9,
        -1
    );

    public static final BattleFormat GEN_9_RAID_SINGLES = new BattleFormat(
        "cobblemon",
        RAID_SINGLES,
        new HashSet<>(Set.of(BattleRules.OBTAINABLE, BattleRules.PAST, BattleRules.UNOBTAINABLE)),
        9,
        -1
    );
    public static final BattleFormat GEN_9_RAID_DOUBLES = new BattleFormat(
        "cobblemon",
        RAID_DOUBLES,
        new HashSet<>(Set.of(BattleRules.OBTAINABLE, BattleRules.PAST, BattleRules.UNOBTAINABLE)),
        9,
        -1
    );
    public static final BattleFormat GEN_9_RAID_TRIPLES = new BattleFormat(
        "cobblemon",
        RAID_TRIPLES,
        new HashSet<>(Set.of(BattleRules.OBTAINABLE, BattleRules.PAST, BattleRules.UNOBTAINABLE)),
        9,
        -1
    );
    public static final BattleFormat GEN_9_RAID_QUADRUPLES = new BattleFormat(
        "cobblemon",
        RAID_QUADRUPLES,
        new HashSet<>(Set.of(BattleRules.OBTAINABLE, BattleRules.PAST, BattleRules.UNOBTAINABLE)),
        9,
        -1
    );
    public static final BattleFormat GEN_9_RAID_PENTUPLES = new BattleFormat(
        "cobblemon",
        RAID_PENTUPLES,
        new HashSet<>(Set.of(BattleRules.OBTAINABLE, BattleRules.PAST, BattleRules.UNOBTAINABLE)),
        9,
        -1
    );
    public static final BattleFormat GEN_9_RAID_SEXTUPLES = new BattleFormat(
        "cobblemon",
        RAID_SEXTUPLES,
        new HashSet<>(Set.of(BattleRules.OBTAINABLE, BattleRules.PAST, BattleRules.UNOBTAINABLE)),
        9,
        -1
    );
}
