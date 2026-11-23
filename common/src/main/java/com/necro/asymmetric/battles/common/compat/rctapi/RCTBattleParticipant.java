package com.necro.asymmetric.battles.common.compat.rctapi;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.properties.BattleCloneProperty;
import com.cobblemon.mod.common.pokemon.properties.UncatchableProperty;
import com.gitlab.srcmc.rctapi.api.ai.RCTBattleAI;
import com.gitlab.srcmc.rctapi.api.battle.BattleManager;
import com.gitlab.srcmc.rctapi.api.battle.BattleManager.TrainerEntityBattleActor;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerBag;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerNPC;
import com.gitlab.srcmc.rctapi.api.util.Text;
import com.necro.asymmetric.battles.common.api.BattleParticipant;
import com.necro.asymmetric.battles.common.api.actor.multi.MultiAttackNPCBattleActor;
import com.necro.asymmetric.battles.common.util.MultiAttackHelper;
import kotlin.Unit;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Functional factory and conversion layer transforming in-game entities into RCT API {@link TrainerEntityBattleActor} instances.
 */
public class RCTBattleParticipant {
    /**
     * Creates an RCT {@link TrainerEntityBattleActor} participant.
     * Will automatically convert the Trainer to a {@link MultiAttackTrainerEntityBattleActor} if valid.
     *
     * @param name              The display name of the trainer.
     * @param entity            The trainer entity.
     * @param uuid              The unique identifier of the trainer actor.
     * @param pokemonList       The list of battle Pokémon in the trainer's party.
     * @param bag               The item inventory bag available to the trainer during battle.
     * @param artificialDecider The AI controller determining move selections.
     * @return A participant supplying a {@link TrainerEntityBattleActor}.
     */
    public static BattleParticipant<TrainerEntityBattleActor> trainer(String name, LivingEntity entity, UUID uuid, List<BattlePokemon> pokemonList, TrainerBag bag, BattleAI artificialDecider) {
        boolean canMultiAttack = pokemonList.stream().anyMatch(pokemon -> pokemon != null && MultiAttackHelper.canMultiAttack(pokemon.getEffectedPokemon()));
        if (canMultiAttack) return () -> new MultiAttackTrainerEntityBattleActor(name, entity, uuid, pokemonList, bag, artificialDecider);
        else return () -> new TrainerEntityBattleActor(name, entity, uuid, pokemonList, bag, artificialDecider);
    }

    /**
     * Creates an RCT {@link TrainerEntityBattleActor} participant.
     * Will automatically convert the Trainer to a {@link MultiAttackTrainerEntityBattleActor} if valid.
     *
     * @param name              The display name of the trainer.
     * @param entity            The trainer entity.
     * @param uuid              The unique identifier of the trainer actor.
     * @param pokemonList       The list of battle Pokémon in the trainer's party.
     * @param bag               The item inventory bag available to the trainer during battle.
     * @param artificialDecider The AI controller determining move selections.
     * @return A participant supplying a {@link TrainerEntityBattleActor}.
     */
    public static BattleParticipant<TrainerEntityBattleActor> trainer(Text name, LivingEntity entity, UUID uuid, List<BattlePokemon> pokemonList, TrainerBag bag, BattleAI artificialDecider) {
        boolean canMultiAttack = pokemonList.stream().anyMatch(pokemon -> pokemon != null && MultiAttackHelper.canMultiAttack(pokemon.getEffectedPokemon()));
        if (canMultiAttack) return () -> new MultiAttackTrainerEntityBattleActor(name, entity, uuid, pokemonList, bag, artificialDecider);
        else return () -> new TrainerEntityBattleActor(name, entity, uuid, pokemonList, bag, artificialDecider);
    }

    /**
     * Creates an RCT {@link TrainerEntityBattleActor} participant.
     * Will automatically convert the Trainer to a {@link MultiAttackTrainerEntityBattleActor} if valid.
     *
     * @param name              The display name of the trainer.
     * @param entity            The trainer entity.
     * @param uuid              The unique identifier of the trainer actor.
     * @param pokemonList       The list of battle Pokémon in the trainer's party.
     * @param bag               The item inventory bag available to the trainer during battle.
     * @return A participant supplying a {@link TrainerEntityBattleActor}.
     */
    public static BattleParticipant<TrainerEntityBattleActor> trainer(String name, LivingEntity entity, UUID uuid, List<BattlePokemon> pokemonList, TrainerBag bag) {
        return trainer(name, entity, uuid, pokemonList, bag, new RCTBattleAI());
    }

    /**
     * Creates an RCT {@link TrainerEntityBattleActor} participant.
     * Will automatically convert the Trainer to a {@link MultiAttackTrainerEntityBattleActor} if valid.
     *
     * @param name              The display name of the trainer.
     * @param entity            The trainer entity.
     * @param uuid              The unique identifier of the trainer actor.
     * @param pokemonList       The list of battle Pokémon in the trainer's party.
     * @param bag               The item inventory bag available to the trainer during battle.
     * @return A participant supplying a {@link TrainerEntityBattleActor}.
     */
    public static BattleParticipant<TrainerEntityBattleActor> trainer(Text name, LivingEntity entity, UUID uuid, List<BattlePokemon> pokemonList, TrainerBag bag) {
        return trainer(name, entity, uuid, pokemonList, bag, new RCTBattleAI());
    }

    /**
     * Creates an RCT {@link TrainerEntityBattleActor} participant.
     * Will automatically convert the Trainer to a {@link MultiAttackTrainerEntityBattleActor} if valid.
     *
     * @param name              The display name of the trainer.
     * @param entity            The trainer entity.
     * @param uuid              The unique identifier of the trainer actor.
     * @param pokemonList       The list of battle Pokémon in the trainer's party.
     * @param artificialDecider The AI controller determining move selections.
     * @return A participant supplying a {@link TrainerEntityBattleActor}.
     */
    public static BattleParticipant<TrainerEntityBattleActor> trainer(String name, LivingEntity entity, UUID uuid, List<BattlePokemon> pokemonList, BattleAI artificialDecider) {
        return trainer(name, entity, uuid, pokemonList, new TrainerBag(), artificialDecider);
    }

    /**
     * Creates an RCT {@link TrainerEntityBattleActor} participant.
     * Will automatically convert the Trainer to a {@link MultiAttackTrainerEntityBattleActor} if valid.
     *
     * @param name              The display name of the trainer.
     * @param entity            The trainer entity.
     * @param uuid              The unique identifier of the trainer actor.
     * @param pokemonList       The list of battle Pokémon in the trainer's party.
     * @param artificialDecider The AI controller determining move selections.
     * @return A participant supplying a {@link TrainerEntityBattleActor}.
     */
    public static BattleParticipant<TrainerEntityBattleActor> trainer(Text name, LivingEntity entity, UUID uuid, List<BattlePokemon> pokemonList, BattleAI artificialDecider) {
        return trainer(name, entity, uuid, pokemonList, new TrainerBag(), artificialDecider);
    }

    /**
     * Creates an RCT {@link TrainerEntityBattleActor} participant.
     * Will automatically convert the Trainer to a {@link MultiAttackTrainerEntityBattleActor} if valid.
     *
     * @param name              The display name of the trainer.
     * @param entity            The trainer entity.
     * @param uuid              The unique identifier of the trainer actor.
     * @param pokemonList       The list of battle Pokémon in the trainer's party.
     * @return A participant supplying a {@link TrainerEntityBattleActor}.
     */
    public static BattleParticipant<TrainerEntityBattleActor> trainer(String name, LivingEntity entity, UUID uuid, List<BattlePokemon> pokemonList) {
        return trainer(name, entity, uuid, pokemonList, new TrainerBag());
    }

    /**
     * Creates an RCT {@link TrainerEntityBattleActor} participant.
     * Will automatically convert the Trainer to a {@link MultiAttackTrainerEntityBattleActor} if valid.
     *
     * @param name              The display name of the trainer.
     * @param entity            The trainer entity.
     * @param uuid              The unique identifier of the trainer actor.
     * @param pokemonList       The list of battle Pokémon in the trainer's party.
     * @return A participant supplying a {@link TrainerEntityBattleActor}.
     */
    public static BattleParticipant<TrainerEntityBattleActor> trainer(Text name, LivingEntity entity, UUID uuid, List<BattlePokemon> pokemonList) {
        return trainer(name, entity, uuid, pokemonList, new TrainerBag());
    }

    /**
     * Creates an RCT {@link TrainerEntityBattleActor} participant.
     * Will automatically convert the Trainer to a {@link MultiAttackTrainerEntityBattleActor} if valid.
     *
     * @param trainer           The Trainer NPC data.
     * @return A participant supplying a {@link TrainerEntityBattleActor}.
     */
    public static BattleParticipant<TrainerEntityBattleActor> trainer(TrainerNPC trainer) {
        List<BattlePokemon> party = new ArrayList<>();
        for (Pokemon p : trainer.getTeam()) {
            if (p.isFainted()) continue;
            Pokemon pokemon = p.clone(true, null);
            BattleCloneProperty.INSTANCE.isBattleClone().apply(pokemon);
            UncatchableProperty.INSTANCE.uncatchable().apply(pokemon);
            party.add(new BattlePokemon(p, pokemon, entity -> {
                entity.recallWithAnimation();
                return Unit.INSTANCE;
            }));
        }
        return trainer(trainer.getName(), trainer.getEntity(), trainer.getEntity().getUUID(), party, trainer.getBag().clone(), trainer.getBattleAI());
    }
}
