package com.necro.asymmetric.battles.common.battle;

import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.battles.ChallengeManager;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MultiBattleJoin extends ChallengeManager.BattleChallenge {
    private final ServerPlayer sender;
    private final ServerPlayer receiver;
    private final UUID selectedPokemonId;
    private final int expiryTime;

    public MultiBattleJoin(ServerPlayer sender, ServerPlayer receiver, UUID selectedPokemonId, int expiryTime) {
        this.sender = sender;
        this.receiver = receiver;
        this.selectedPokemonId = selectedPokemonId;
        this.expiryTime = expiryTime;
    }

    public MultiBattleJoin(ServerPlayer sender, ServerPlayer receiver, UUID selectedPokemonId) {
        this(sender, receiver, selectedPokemonId, 20);
    }

    @Override
    public @NotNull ServerPlayer getSender() {
        return this.sender;
    }

    @Override
    public @NotNull ServerPlayer getReceiver() {
        return this.receiver;
    }

    @Override
    public @NotNull UUID getSelectedPokemonId() {
        return this.selectedPokemonId;
    }

    @Override
    public @NotNull BattleFormat getBattleFormat() {
        return BattleFormat.Companion.getGEN_9_MULTI();
    }

    @Override
    public @NotNull String getKey() {
        return "join";
    }

    @Override
    public int getExpiryTime() {
        return this.expiryTime;
    }
}
