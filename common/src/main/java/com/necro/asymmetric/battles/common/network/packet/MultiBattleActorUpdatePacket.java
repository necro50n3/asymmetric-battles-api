package com.necro.asymmetric.battles.common.network.packet;

import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.*;
import com.cobblemon.mod.common.net.IntSize;
import com.cobblemon.mod.common.net.messages.client.battle.BattleInitializePacket;
import com.cobblemon.mod.common.util.NetExtensionsKt;
import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import com.necro.asymmetric.battles.common.network.ClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record MultiBattleActorUpdatePacket(int side, boolean isAlly, BattleInitializePacket.BattleActorDTO actor) implements CustomPacketPayload, ClientPacket {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(AsymmetricBattlesAPI.MODID, "multi_battle_actor_update");
    public static final Type<MultiBattleActorUpdatePacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, MultiBattleActorUpdatePacket> CODEC = StreamCodec.ofMember(MultiBattleActorUpdatePacket::write, MultiBattleActorUpdatePacket::read);

    public MultiBattleActorUpdatePacket(int side, boolean isAlly, BattleActor actor) {
        this(side, isAlly, new BattleInitializePacket.BattleActorDTO(
            actor.getUuid(),
            actor.getName(),
            actor.getShowdownId(),
            actor.getActivePokemon().stream().map(pokemon -> pokemon.getBattlePokemon() == null ? null : BattleInitializePacket.ActiveBattlePokemonDTO.Companion.fromPokemon(
                pokemon.getBattlePokemon(),
                isAlly,
                pokemon.getIllusion()
            )).toList(),
            actor.getType())
        );
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(this.side);
        buf.writeBoolean(this.isAlly);
        buf.writeUUID(this.actor.getUuid());
        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buf, this.actor.getDisplayName());
        buf.writeUtf(this.actor.getShowdownId());
        NetExtensionsKt.writeSizedInt(buf, IntSize.U_BYTE, this.actor.getActivePokemon().size());
        for (BattleInitializePacket.ActiveBattlePokemonDTO activePokemon : this.actor.getActivePokemon()) {
            buf.writeBoolean(activePokemon != null);
            if (activePokemon != null) activePokemon.saveToBuffer(buf);
        }
        NetExtensionsKt.writeSizedInt(buf, IntSize.U_BYTE, this.actor.getType().ordinal());
    }

    public static MultiBattleActorUpdatePacket read(RegistryFriendlyByteBuf buf) {
        int side = buf.readInt();
        boolean isAlly = buf.readBoolean();

        UUID uuid = buf.readUUID();
        MutableComponent component = (MutableComponent) ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buf);
        String showdownId = buf.readUtf();
        int pokemonCount = NetExtensionsKt.readSizedInt(buf, IntSize.U_BYTE);
        List<BattleInitializePacket.ActiveBattlePokemonDTO> activePokemon = new ArrayList<>();
        for (int i = 0; i < pokemonCount; i++) {
            if (buf.readBoolean()) {
                activePokemon.add(BattleInitializePacket.ActiveBattlePokemonDTO.Companion.loadFromBuffer(buf));
            } else {
                activePokemon.add(null);
            }
        }
        ActorType type = ActorType.values()[NetExtensionsKt.readSizedInt(buf, IntSize.U_BYTE)];

        BattleInitializePacket.BattleActorDTO actor = new BattleInitializePacket.BattleActorDTO(uuid, component, showdownId, activePokemon, type);
        return new MultiBattleActorUpdatePacket(side, isAlly, actor);
    }

    public void handle() {
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (battle == null || Minecraft.getInstance().player == null) return;

        boolean tempSpectating = true;
        for (ClientBattleSide side : battle.getSides()) {
            if (side.getActors().stream().anyMatch(actor -> actor.getUuid() == Minecraft.getInstance().player.getUUID()))
                tempSpectating = false;
        }
        boolean spectating = tempSpectating;

        ClientBattleSide side = this.side == 1 || this.side == 3 ? battle.getSide1() : battle.getSide2();
        int index = this.side == 1 || this.side == 2 ? 0 : 1;

        ClientBattleActor actor = new ClientBattleActor(
            this.actor.getShowdownId(),
            this.actor.getDisplayName(),
            this.actor.getUuid(),
            this.actor.getType()
        );
        actor.getActivePokemon().addAll(this.actor.getActivePokemon().stream().map(pokemon -> new ActiveClientBattlePokemon(
            actor,
            pokemon == null ? null : new ClientBattlePokemon(
                pokemon.getUuid(),
                pokemon.getDisplayName(),
                pokemon.getProperties(),
                pokemon.getAspects(),
                pokemon.getHpValue(),
                pokemon.getMaxHp(),
                this.isAlly && !spectating,
                pokemon.getStatus(),
                pokemon.getStatChanges()
            )
        )).toList());
        side.getActors().set(index, actor);

        actor.setSide(side);
        for (ActiveClientBattlePokemon pokemon : actor.getActivePokemon()) {
            if (pokemon.getBattlePokemon() != null) pokemon.getBattlePokemon().setActor(actor);
        }
    }
}
