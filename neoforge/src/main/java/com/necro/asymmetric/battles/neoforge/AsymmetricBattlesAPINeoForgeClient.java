package com.necro.asymmetric.battles.neoforge;

import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import com.necro.asymmetric.battles.common.AsymmetricBattlesAPIClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = AsymmetricBattlesAPI.MODID, dist = Dist.CLIENT)
public class AsymmetricBattlesAPINeoForgeClient {
    public AsymmetricBattlesAPINeoForgeClient(IEventBus modBus, ModContainer container) {
        AsymmetricBattlesAPIClient.init();
    }
}
