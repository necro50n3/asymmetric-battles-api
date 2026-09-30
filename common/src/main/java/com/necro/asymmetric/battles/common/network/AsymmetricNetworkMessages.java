package com.necro.asymmetric.battles.common.network;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.necro.asymmetric.battles.common.util.QuadConsumer;
import net.minecraft.server.level.ServerPlayer;

public class AsymmetricNetworkMessages {
    public static QuadConsumer<ServerPlayer, Integer, Boolean, BattleActor> MULTI_BATTLE_ACTOR_UPDATE;
}
