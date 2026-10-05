package com.necro.asymmetric.battles.common;

import com.mojang.logging.LogUtils;
import com.necro.asymmetric.battles.common.config.AsymmetricConfig;
import com.necro.asymmetric.battles.common.config.serializer.PrimitiveYamlConfigSerializer;
import me.shedaniel.autoconfig.AutoConfig;
import org.slf4j.Logger;

public class AsymmetricBattlesAPI {
    public static final String MODID = "asymmetricbattles";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static AsymmetricConfig CONFIG;

    public static void init() {
        LOGGER.info("Initiating {}", MODID);

        AutoConfig.register(AsymmetricConfig.class, PrimitiveYamlConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(AsymmetricConfig.class).getConfig();
    }
}
