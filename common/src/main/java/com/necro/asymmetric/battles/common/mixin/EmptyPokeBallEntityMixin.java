package com.necro.asymmetric.battles.common.mixin;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokeball.ThrownPokeballHitEvent;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BattleCaptureAction;
import com.cobblemon.mod.common.battles.ForcePassActionResponse;
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonServerDelegate;
import com.cobblemon.mod.common.net.messages.client.battle.BattleCaptureStartPacket;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.necro.asymmetric.battles.common.actor.DummyBattleActor;
import kotlin.Unit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Set;

import static com.cobblemon.mod.common.util.LocalizationUtilsKt.lang;

@Mixin(EmptyPokeBallEntity.class)
public abstract class EmptyPokeBallEntityMixin extends ThrowableItemProjectile {
    public EmptyPokeBallEntityMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract PokeBall getPokeBall();

    @Shadow
    public abstract Set<String> getAspects();

    @Shadow
    public abstract void setCapturingPokemon(@Nullable PokemonEntity _set___);

    @Shadow
    protected abstract void drop();

    @Shadow
    protected abstract void attemptCatch(PokemonEntity pokemonEntity);

    @Unique
    private boolean aba_bypassChecks = false;

    @WrapOperation(
        method = "onHitEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V",
            ordinal = 1
        ),
        remap = false
    )
    private void bypassSend(LivingEntity instance, Component component, Operation<Void> original, @Local(argsOnly = true) EntityHitResult hitResult) {
        this.aba_bypassChecks = false;

        PokemonEntity pokemonEntity = (PokemonEntity) hitResult.getEntity();
        PokemonBattle battle = ((PokemonServerDelegate) pokemonEntity.getDelegate()).getBattle();
        if (battle == null) {
            original.call(instance, component);
            return;
        }
        BattleActor hitActor = null;
        for (BattleActor actor : battle.getActors()) {
            if (actor.isForPokemon(pokemonEntity)) {
                hitActor = actor;
                break;
            }
        }
        if (hitActor == null || hitActor.getType() != ActorType.WILD || hitActor instanceof DummyBattleActor) {
            original.call(instance, component);
            return;
        }

        long pokemonCount = hitActor.getPokemonList().stream().filter(pokemon -> pokemon.getHealth() > 0).count();
        long sideCount = hitActor.getSide().getActivePokemon().stream().filter(pokemon -> pokemon.getBattlePokemon() != null).count();

        if (pokemonCount > 1 || sideCount > 1) {
            original.call(instance, component);
            return;
        }

        this.aba_bypassChecks = true;

        List<ActiveBattlePokemon> activePokemon = hitActor.getActivePokemon().stream().filter(pokemon -> {
            if (pokemon.getBattlePokemon() == null) return false;
            return pokemon.getBattlePokemon().getEffectedPokemon().getEntity() == pokemonEntity;
        }).toList();
        ActiveBattlePokemon hitBattlePokemon = activePokemon.getFirst();
        assert this.getOwner() != null;
        BattleActor throwerActor = battle.getActor(this.getOwner().getUUID());
        assert throwerActor != null;

        BattleCaptureAction action = new BattleCaptureAction(battle, hitBattlePokemon, (EmptyPokeBallEntity) (Object) this);
        battle.getCaptureActions().add(action);
        action.attach();

        battle.broadcastChatMessage(
            lang(
                "capture.attempted_capture",
                throwerActor.getName(),
                this.getPokeBall().item().getDescription(),
                pokemonEntity.getExposedSpecies().getTranslatedName()
            ).withStyle(ChatFormatting.YELLOW)
        );
        battle.sendUpdate(new BattleCaptureStartPacket(this.getPokeBall().getName(), this.getAspects(), hitBattlePokemon.getPNX()));
        throwerActor.forceChoose(new ForcePassActionResponse());

        this.setCapturingPokemon(pokemonEntity);
        this.entityData.set(EmptyPokeBallEntity.Companion.getHIT_VELOCITY(), this.getDeltaMovement().normalize());
        this.entityData.set(EmptyPokeBallEntity.Companion.getHIT_TARGET_POSITION(), hitResult.getLocation());
        CobblemonEvents.THROWN_POKEBALL_HIT.postThen(
            new ThrownPokeballHitEvent((EmptyPokeBallEntity) (Object) this, pokemonEntity),
            event -> {
                this.drop();
                return Unit.INSTANCE;
            },
            event -> {
                this.attemptCatch(pokemonEntity);
                return Unit.INSTANCE;
            }
        );
    }

    @WrapOperation(
        method = "onHitEntity",
        at = @At(
            value = "INVOKE",
            target = "Lcom/cobblemon/mod/common/entity/pokeball/EmptyPokeBallEntity;drop()V",
            ordinal = 4
        ),
        remap = false
    )
    private void bypassDrop(EmptyPokeBallEntity instance, Operation<Void> original) {
        if (!this.aba_bypassChecks) original.call(instance);
        this.aba_bypassChecks = false;
    }
}
