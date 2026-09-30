package com.necro.asymmetric.battles.neoforge.network;

import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import com.necro.asymmetric.battles.common.network.AsymmetricNetworkMessages;
import com.necro.asymmetric.battles.common.network.ClientPacket;
import com.necro.asymmetric.battles.common.network.ServerPacket;
import com.necro.asymmetric.battles.common.network.packet.MultiBattleActorUpdatePacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = AsymmetricBattlesAPI.MODID)
public class AsymmetricNetworkMessagesNeoForge {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar payloadRegistrar = event.registrar(AsymmetricBattlesAPI.MODID).versioned("1.0.0").optional();
        payloadRegistrar.playToClient(MultiBattleActorUpdatePacket.PACKET_TYPE, MultiBattleActorUpdatePacket.CODEC, AsymmetricNetworkMessagesNeoForge::handle);

        init();
    }

    public static void init() {
        AsymmetricNetworkMessages.MULTI_BATTLE_ACTOR_UPDATE = (player, side, isAlly, actor) ->
            AsymmetricNetworkMessagesNeoForge.sendPacketToPlayer(player, new MultiBattleActorUpdatePacket(side, isAlly, actor));
    }

    public static void sendPacketToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

    public static void sendPacketToAll(CustomPacketPayload packet) {
        PacketDistributor.sendToAllPlayers(packet);
    }

    public static void sendPacketToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    private static void handle(ClientPacket packet, IPayloadContext context) {
        context.enqueueWork(packet::handle);
    }

    private static void handle(ServerPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> packet.handle((ServerPlayer) context.player()));
    }
}