package com.necro.asymmetric.battles.neoforge.events;

import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import com.necro.asymmetric.battles.common.api.AsymmetricAPI;
import com.necro.asymmetric.battles.common.command.MultiBattleCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber(modid = AsymmetricBattlesAPI.MODID)
public class AsymmetricEvents {
    @SubscribeEvent
    private static void onServerStarted(ServerStartedEvent event) {
        AsymmetricAPI.onServerStart();
    }

    @SubscribeEvent
    private static void onCommandRegistration(RegisterCommandsEvent event) {
        MultiBattleCommand.register(event.getDispatcher());
    }
}
