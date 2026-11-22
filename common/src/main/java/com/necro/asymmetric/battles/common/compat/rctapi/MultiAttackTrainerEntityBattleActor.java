package com.necro.asymmetric.battles.common.compat.rctapi;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.gitlab.srcmc.rctapi.api.battle.BattleManager;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerBag;
import com.gitlab.srcmc.rctapi.api.util.Text;
import com.necro.asymmetric.battles.common.api.actor.MultiAttackBattleActor;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class MultiAttackTrainerEntityBattleActor extends BattleManager.TrainerEntityBattleActor implements MultiAttackBattleActor {
    public MultiAttackTrainerEntityBattleActor(String name, LivingEntity entity, UUID uuid, List<BattlePokemon> pokemonList, TrainerBag bag, @NotNull BattleAI battleAI) {
        super(name, entity, uuid, pokemonList, bag, battleAI);
    }

    public MultiAttackTrainerEntityBattleActor(Text name, LivingEntity entity, UUID uuid, List<BattlePokemon> pokemonList, TrainerBag bag, @NotNull BattleAI battleAI) {
        super(name, entity, uuid, pokemonList, bag, battleAI);
    }

    @Override
    public void onChoiceRequested() {
        this.onMultiChoiceRequested();
    }
}
