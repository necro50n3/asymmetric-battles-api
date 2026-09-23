package com.necro.asymmetric.battles.common.compat.rctapi;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.gitlab.srcmc.rctapi.api.ai.RCTBattleAI;
import com.gitlab.srcmc.rctapi.api.battle.BattleManager;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerBag;
import com.necro.asymmetric.battles.common.api.BattleParticipant;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.UUID;

public class RCTBattleParticipant {
    public static BattleParticipant trainer(String name, LivingEntity entity, UUID uuid, List<BattlePokemon> pokemonList, TrainerBag bag, BattleAI artificialDecider) {
        return () -> new BattleManager.TrainerEntityBattleActor(name, entity, uuid, pokemonList, bag, artificialDecider);
    }

    public static BattleParticipant trainer(String name, LivingEntity entity, UUID uuid, List<BattlePokemon> pokemonList, TrainerBag bag) {
        return trainer(name, entity, uuid, pokemonList, bag, new RCTBattleAI());
    }

    public static BattleParticipant trainer(String name, LivingEntity entity, UUID uuid, List<BattlePokemon> pokemonList, BattleAI artificialDecider) {
        return trainer(name, entity, uuid, pokemonList, new TrainerBag(), artificialDecider);
    }

    public static BattleParticipant trainer(String name, LivingEntity entity, UUID uuid, List<BattlePokemon> pokemonList) {
        return trainer(name, entity, uuid, pokemonList, new TrainerBag());
    }
}
