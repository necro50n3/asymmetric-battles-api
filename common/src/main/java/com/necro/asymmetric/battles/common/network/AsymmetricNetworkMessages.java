package com.necro.asymmetric.battles.common.network;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.necro.asymmetric.battles.common.util.QuintConsumer;
import net.minecraft.server.level.ServerPlayer;

public class AsymmetricNetworkMessages {
    public static QuintConsumer<ServerPlayer, Integer, Integer, Boolean, BattleActor> BATTLE_ACTOR_UPDATE;
}
