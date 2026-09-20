package com.necro.asymmetric.battles.neoforge;

import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(AsymmetricBattlesAPI.MODID)
public class AsymmetricBattlesAPINeoForge {
    public AsymmetricBattlesAPINeoForge(IEventBus modBus, ModContainer container) {
        AsymmetricBattlesAPI.init();
    }
}
