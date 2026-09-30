package com.necro.asymmetric.battles.common.network;

import net.minecraft.server.level.ServerPlayer;

public interface ServerPacket {
    void handle(ServerPlayer player);
}
