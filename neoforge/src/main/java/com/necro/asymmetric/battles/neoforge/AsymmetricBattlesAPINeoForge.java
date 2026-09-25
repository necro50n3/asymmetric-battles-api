package com.necro.asymmetric.battles.neoforge;

import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import com.necro.asymmetric.battles.common.compat.ModCompat;
import com.necro.asymmetric.battles.common.config.AsymmetricConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(AsymmetricBattlesAPI.MODID)
public class AsymmetricBattlesAPINeoForge {
    public AsymmetricBattlesAPINeoForge(IEventBus modBus, ModContainer container) {
        for (ModCompat mod : ModCompat.values()) {
            mod.setLoaded(ModList.get().isLoaded(mod.getModid()));
        }

        container.registerConfig(ModConfig.Type.COMMON, AsymmetricConfig.Common.CONFIG_SPEC);
        AsymmetricBattlesAPI.init();
    }
}
