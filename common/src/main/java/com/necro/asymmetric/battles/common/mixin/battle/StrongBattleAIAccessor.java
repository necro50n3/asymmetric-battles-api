package com.necro.asymmetric.battles.common.mixin.battle;

import com.cobblemon.mod.common.battles.ai.StrongBattleAI;
import com.cobblemon.mod.common.battles.ai.strongBattleAI.ActiveTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StrongBattleAI.class)
public interface StrongBattleAIAccessor {
    @Accessor(value = "activeTracker", remap = false)
    ActiveTracker getActiveTracker();
}
