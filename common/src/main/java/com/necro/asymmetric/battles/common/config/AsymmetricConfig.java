package com.necro.asymmetric.battles.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class AsymmetricConfig {
    public static final class Common {
        public static final AsymmetricConfig.Common CONFIG;
        public static final ModConfigSpec CONFIG_SPEC;

        public final ModConfigSpec.BooleanValue ENABLE_PVP_CHALLENGES;
        public final ModConfigSpec.BooleanValue ENABLE_DEBUG;

        private Common(ModConfigSpec.Builder builder) {
            ENABLE_PVP_CHALLENGES = builder
                .comment("Whether Quadruple, Pentuple and Sextuple Battles are available in the Player Battle Challenge screen. This does not disable the battle formats themselves.")
                .translation("asymmetricbattles.config.enable_pvp_challenges")
                .define("enable_pvp_challenges", false);

            ENABLE_DEBUG = builder
                .comment("Log showdown inputs and outputs for debugging.")
                .translation("asymmetricbattles.config.enable_debug")
                .define("enable_debug", false);
        }

        static {
            Pair<Common, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Common::new);
            CONFIG = pair.getLeft();
            CONFIG_SPEC = pair.getRight();
        }
    }
}
