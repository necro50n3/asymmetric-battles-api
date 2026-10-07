package com.necro.asymmetric.battles.neoforge.events;

import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import com.necro.asymmetric.battles.common.api.AsymmetricAPI;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = AsymmetricBattlesAPI.MODID)
public class AsymmetricEvents {
    @SubscribeEvent
    private static void onServerStarted() {
        AsymmetricAPI.onServerStart();
    }
}
