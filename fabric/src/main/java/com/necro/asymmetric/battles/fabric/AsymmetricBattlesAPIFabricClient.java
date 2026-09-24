package com.necro.asymmetric.battles.fabric;

import com.necro.asymmetric.battles.common.AsymmetricBattlesAPIClient;
import net.fabricmc.api.ClientModInitializer;

public class AsymmetricBattlesAPIFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AsymmetricBattlesAPIClient.init();
    }
}
