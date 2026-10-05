package com.necro.asymmetric.battles.common.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name="asymmetricbattles-common")
public class AsymmetricConfig implements ConfigData {
    @Comment("Whether Quadruple, Pentuple and Sextuple Battles are available in the Player Battle Challenge screen. This does not disable the battle formats themselves.")
    public boolean ENABLE_PVP_CHALLENGES;
    @Comment("Log showdown inputs and outputs for debugging.")
    public boolean ENABLE_DEBUG;
}
