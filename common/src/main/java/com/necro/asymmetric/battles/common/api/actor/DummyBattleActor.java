package com.necro.asymmetric.battles.common.api.actor;

import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class DummyBattleActor extends BattleActor {
    public DummyBattleActor(@NotNull UUID uuid) {
        super(uuid, List.of());
    }

    @Override
    public @NotNull ActorType getType() {
        return ActorType.WILD;
    }

    @Override
    public @NotNull MutableComponent getName() {
        return Component.literal("Wild Pokémon");
    }

    @Override
    public @NotNull MutableComponent nameOwned(@NotNull String name) {
        return Component.literal(name);
    }
}
