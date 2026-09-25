package com.necro.asymmetric.battles.common.util;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.battles.BattleType;
import com.cobblemon.mod.common.battles.BattleTypes;
import com.necro.asymmetric.battles.common.api.AsymmetricBattleFormats;

public class AsymmetricUtils {
    public static boolean isAsymmetricBattle(BattleType battleType) {
        if (battleType.getName().equalsIgnoreCase(AsymmetricBattleFormats.QUADRUPLES.getName())) return true;
        else if (battleType.getName().equalsIgnoreCase(AsymmetricBattleFormats.PENTUPLES.getName())) return true;
        else if (battleType.getName().equalsIgnoreCase(AsymmetricBattleFormats.SEXTUPLES.getName())) return true;
        else return battleType.getName().equalsIgnoreCase(AsymmetricBattleFormats.HORDE.getName());
    }

    public static boolean isAsymmetricBattle(BattleFormat battleFormat) {
        return isAsymmetricBattle(battleFormat.getBattleType());
    }

    public static boolean isAsymmetricBattle(PokemonBattle battle) {
        return isAsymmetricBattle(battle.getFormat().getBattleType());
    }

    public static boolean isAsymmetricOrMultiBattle(BattleType battleType) {
        if (battleType.getName().equalsIgnoreCase(BattleTypes.INSTANCE.getMULTI().getName())) return true;
        else return isAsymmetricBattle(battleType);
    }

    public static boolean isAsymmetricOrMultiBattle(BattleFormat battleFormat) {
        return isAsymmetricOrMultiBattle(battleFormat.getBattleType());
    }

    public static boolean isAsymmetricOrMultiBattle(PokemonBattle battle) {
        return isAsymmetricOrMultiBattle(battle.getFormat().getBattleType());
    }
}
