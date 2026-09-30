package com.necro.asymmetric.battles.fabric.network;

import com.necro.asymmetric.battles.common.network.AsymmetricNetworkMessages;
import com.necro.asymmetric.battles.common.network.ClientPacket;
import com.necro.asymmetric.battles.common.network.ServerPacket;
import com.necro.asymmetric.battles.common.network.packet.MultiBattleActorUpdatePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class AsymmetricNetworkMessagesFabric {
    public static void registerPayload() {
        PayloadTypeRegistry.playS2C().register(MultiBattleActorUpdatePacket.PACKET_TYPE, MultiBattleActorUpdatePacket.CODEC);

        init();
    }

    public static void registerS2CPayload() {
        ClientPlayNetworking.registerGlobalReceiver(MultiBattleActorUpdatePacket.PACKET_TYPE, AsymmetricNetworkMessagesFabric::handle);
    }

    public static void init() {
        AsymmetricNetworkMessages.MULTI_BATTLE_ACTOR_UPDATE = (player, side, isAlly,actor) ->
            AsymmetricNetworkMessagesFabric.sendPacketToPlayer(player, new MultiBattleActorUpdatePacket(side, isAlly, actor));
    }

    public static void sendPacketToServer(CustomPacketPayload packet) {
        ClientPlayNetworking.send(packet);
    }

    public static void sendPacketToAll(MinecraftServer server, CustomPacketPayload packet) {
        server.getPlayerList().getPlayers().forEach(player -> ServerPlayNetworking.send(player, packet));
    }

    public static void sendPacketToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        ServerPlayNetworking.send(player, packet);
    }

    private static void handle(ClientPacket packet, ClientPlayNetworking.Context context) {
        packet.handle();
    }

    private static void handle(ServerPacket packet, ServerPlayNetworking.Context context) {
        context.server().execute(() -> packet.handle(context.player()));
    }
}
