package com.necro.asymmetric.battles.neoforge;

import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = AsymmetricBattlesAPI.MODID, dist = Dist.CLIENT)
public class AsymmetricBattlesAPINeoForgeClient {
    public AsymmetricBattlesAPINeoForgeClient(IEventBus modBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        AsymmetricBattlesAPI.init();
    }
}
