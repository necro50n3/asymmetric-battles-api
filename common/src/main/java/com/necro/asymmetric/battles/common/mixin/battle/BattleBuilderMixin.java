package com.necro.asymmetric.battles.common.mixin.battle;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.storage.party.PartyStore;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.necro.asymmetric.battles.common.api.AsymmetricBattleBuilder;
import com.necro.asymmetric.battles.common.api.AsymmetricBattleFormats;
import com.necro.asymmetric.battles.common.api.BattleParticipant;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Mixin(BattleBuilder.class)
public class BattleBuilderMixin {
//    @ModifyVariable(
//        method = "pve(Lnet/minecraft/server/level/ServerPlayer;Lcom/cobblemon/mod/common/entity/pokemon/PokemonEntity;Ljava/util/UUID;Lcom/cobblemon/mod/common/battles/BattleFormat;ZZFLcom/cobblemon/mod/common/api/storage/party/PartyStore;)Lcom/cobblemon/mod/common/battles/BattleStartResult;",
//        at = @At("HEAD"),
//        argsOnly = true,
//        remap = false
//    )
//    private BattleFormat modifyBattleFormat(BattleFormat battleFormat) {
//        return BattleFormat.Companion.getGEN_9_DOUBLES();
//        return AsymmetricBattleFormats.GEN_9_SEXTUPLES;
//    }

    @Inject(method = "pve*", at = @At("HEAD"), remap = false, cancellable = true)
    private void pveMulti(ServerPlayer player, PokemonEntity pokemonEntity, UUID leadingPokemon, BattleFormat battleFormat, boolean cloneParties, boolean healFirst, float fleeDistance, PartyStore party, CallbackInfoReturnable<BattleStartResult> cir) {
//        cir.setReturnValue(AsymmetricBattleBuilder.multiBattle(
//            BattleParticipant.player(player, leadingPokemon),
//            BattleParticipant.wild(pokemonEntity))
//        );

        List<PokemonEntity> entities = pokemonEntity.level().getNearbyEntities(
            PokemonEntity.class,
            TargetingConditions.DEFAULT,
            pokemonEntity,
            pokemonEntity.getBoundingBox().inflate(8, 2, 8)
        );
        List<PokemonEntity> horde = new ArrayList<>();
        horde.add(pokemonEntity);
        if (!entities.isEmpty()) horde.addAll(entities.subList(0, Math.min(entities.size(), 5)));

        cir.setReturnValue(AsymmetricBattleBuilder.hordeBattle(BattleParticipant.player(player, leadingPokemon), BattleParticipant.horde(horde)));
    }

    @WrapOperation(method = { "pvp1v1*", "pvp2v2*", "pve*", "pvn*" }, at = @At(value = "INVOKE", target = "Ljava/util/Collection;add(Ljava/lang/Object;)Z"), remap = false)
    private static boolean cancelInsufficientPokemonError(Collection<BattleStartError> set, Object error, Operation<Boolean> original) {
        if (aba_shouldCancel(error)) return false;
        return original.call(set, error);
    }

    @Unique
    private static boolean aba_shouldCancel(Object error) {
        return error instanceof InsufficientPokemonError;
    }
}
