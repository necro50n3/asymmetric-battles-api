package com.necro.asymmetric.battles.fabric;

import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import net.fabricmc.api.ModInitializer;

public class AsymmetricBattlesAPIFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        AsymmetricBattlesAPI.init();
    }

}
