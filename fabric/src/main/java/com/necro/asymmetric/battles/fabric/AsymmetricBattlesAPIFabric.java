package com.necro.asymmetric.battles.fabric;

import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import com.necro.asymmetric.battles.common.compat.ModCompat;
import com.necro.asymmetric.battles.fabric.network.AsymmetricNetworkMessagesFabric;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class AsymmetricBattlesAPIFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        for (ModCompat mod : ModCompat.values()) {
            mod.setLoaded(FabricLoader.getInstance().isModLoaded(mod.getModid()));
        }

        AsymmetricBattlesAPI.init();
        AsymmetricNetworkMessagesFabric.registerPayload();
    }

}
