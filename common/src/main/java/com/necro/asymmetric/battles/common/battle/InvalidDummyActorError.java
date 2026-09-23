package com.necro.asymmetric.battles.common.battle;

import com.cobblemon.mod.common.battles.BattleStartError;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class InvalidDummyActorError implements BattleStartError {
    @Override
    public @NotNull MutableComponent getMessageFor(@NotNull Entity entity) {
        return Component.translatable("cobblemon.battle.invalid_dummy");
    }
}
