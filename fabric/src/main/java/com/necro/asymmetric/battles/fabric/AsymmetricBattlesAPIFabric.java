package com.necro.asymmetric.battles.fabric;

import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import com.necro.asymmetric.battles.common.compat.ModCompat;
import com.necro.asymmetric.battles.common.config.AsymmetricConfig;
import com.necro.asymmetric.battles.fabric.network.AsymmetricNetworkMessagesFabric;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.fml.config.ModConfig;

public class AsymmetricBattlesAPIFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        for (ModCompat mod : ModCompat.values()) {
            mod.setLoaded(FabricLoader.getInstance().isModLoaded(mod.getModid()));
        }

        NeoForgeConfigRegistry.INSTANCE.register(AsymmetricBattlesAPI.MODID, ModConfig.Type.COMMON, AsymmetricConfig.Common.CONFIG_SPEC);
        AsymmetricBattlesAPI.init();
        AsymmetricNetworkMessagesFabric.registerPayload();
    }

}
