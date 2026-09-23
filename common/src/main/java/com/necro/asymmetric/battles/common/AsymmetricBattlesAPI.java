package com.necro.asymmetric.battles.common;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class AsymmetricBattlesAPI {
    public static final String MODID = "asymmetricbattles";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        LOGGER.info("Initiating {}", MODID);
    }
}
