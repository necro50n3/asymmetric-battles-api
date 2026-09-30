package com.necro.asymmetric.battles.fabric;

import com.necro.asymmetric.battles.common.AsymmetricBattlesAPIClient;
import com.necro.asymmetric.battles.fabric.network.AsymmetricNetworkMessagesFabric;
import net.fabricmc.api.ClientModInitializer;

public class AsymmetricBattlesAPIFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AsymmetricBattlesAPIClient.init();
        AsymmetricNetworkMessagesFabric.registerS2CPayload();
    }
}
