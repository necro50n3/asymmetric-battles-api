package com.necro.asymmetric.battles.common.api.actor;

import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Placeholder {@link BattleActor} used to represent an open/unfilled slot in Multi Battles.
 * <p>
 * <b>Usage Constraints:</b>
 * <ul>
 *   <li>Only valid in secondary ally positions (Slot 3 / Showdown {@code p3} and Slot 4 / Showdown {@code p4}).</li>
 *   <li>Cannot be placed in primary battle slots (Slot 1 or Slot 2).</li>
 * </ul>
 * <p>
 * When a battle starts with dummy actors, it allows asymmetric 1v1, 1v2, or 2v1 matchups under
 * the Multi Battle format while reserving slots for late-joining participants via
 * {@link com.necro.asymmetric.battles.common.api.AsymmetricAPI#setMultiBattleActor}.
 */
public class DummyBattleActor extends BattleActor {

    /**
     * Constructs a new dummy actor placeholder.
     *
     * @param uuid The unique identifier assigned to this placeholder actor.
     */
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
