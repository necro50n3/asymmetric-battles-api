package com.necro.asymmetric.battles.fabric.events;

import com.necro.asymmetric.battles.common.api.AsymmetricAPI;
import net.minecraft.server.MinecraftServer;

public class AsymmetricEvents {
    public static void onServerStarted(MinecraftServer server) {
        AsymmetricAPI.onServerStart();
    }
}
