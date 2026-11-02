package com.necro.asymmetric.battles.common.mixin.battle;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import com.necro.asymmetric.battles.common.util.AsymmetricUtils;
import com.necro.asymmetric.battles.common.util.PokemonLocatorUtils;
import kotlin.Pair;
import kotlin.collections.CollectionsKt;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ActiveBattlePokemon.class)
public abstract class ActiveBattlePokemonMixin implements Targetable {
    @Shadow
    public abstract @NotNull BattleFormat getFormat();

    @Shadow
    public abstract @NotNull Iterable<Targetable> getAllActivePokemon();

    @Shadow
    public abstract @NotNull BattleSide getSide();

    @Shadow
    public abstract @NotNull BattleActor getActor();

    @Shadow
    public abstract @NotNull BattlePokemon getBattlePokemon();

    @Shadow
    public abstract @NotNull PokemonBattle getBattle();

    @Shadow
    public abstract Vec3 getSendOutPosition();

    @Inject(method = "getLetter", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void getCorrectLetter(CallbackInfoReturnable<Character> cir) {
        BattleActor actor = this.getActor();
        BattleSide side = this.getSide();

        List<BattleActor> actors = List.of(side.getActors());
        int actorIndex = actors.indexOf(actor);
        if (actorIndex == -1) return;

        int slotsPerActor = this.getFormat().getBattleType().getSlotsPerActor();
        int activeIndex = Math.max(0, actor.getActivePokemon().indexOf((ActiveBattlePokemon) (Object) this));

        int slotOffset = (actorIndex * slotsPerActor) + activeIndex;
        cir.setReturnValue((char) ('a' + slotOffset));
    }

    @Inject(method = "getAdjacent", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void getAllAdjacent(CallbackInfoReturnable<List<Targetable>> cir) {
        if (!AsymmetricUtils.isAsymmetricBattle(this.getFormat())) return;
        cir.setReturnValue(CollectionsKt.filter(
            this.getAllActivePokemon(),
            pokemon -> {
                if (pokemon == this) return false;
                BattlePokemon battlePokemon = ((ActiveBattlePokemon) pokemon).getBattlePokemon();
                return battlePokemon != null && battlePokemon.getHealth() > 0;
            }
        ));
    }

    @Inject(method = "getPosition", at = @At("RETURN"), cancellable = true, remap = false)
    private void getRaidPosition(CallbackInfoReturnable<Pair<ServerLevel, Vec3>> cir) {
        if (cir.getReturnValue() != null) return;
        else if (!this.getBattle().getFormat().getBattleType().getName().equalsIgnoreCase("raid")) return;
        if (!(this.getActor() instanceof EntityBackedBattleActor<?> entityActor)) return;

        LivingEntity entity = entityActor.getEntity();
        if (entity == null) return;

        Vec3 pos = this.getSendOutPosition();
        if (pos == null) return;
        cir.setReturnValue(new Pair<>((ServerLevel) entity.level(), pos));
    }

    @Inject(method = "getSendOutPosition", at = @At("HEAD"), cancellable = true, remap = false)
    private void getSendOutPositionExtension(CallbackInfoReturnable<Vec3> cir) {
        BattleType type = this.getFormat().getBattleType();
        if (type.getPokemonPerSide() == 1 || type.getName().equalsIgnoreCase("raid")) return;
        int pokemonPerSide = (int) this.getSide().getActivePokemon().stream().filter(ActiveBattlePokemon::hasPokemon).count();

        String pnx = this.getPNX();
        if (pnx.length() < 3) return;
        int slotIndex = pnx.charAt(2) - 'a';
        if (slotIndex < 0 || slotIndex >= pokemonPerSide) return;

        Vec3 actorEntityPos = PokemonLocatorUtils.getAveragePosition(this.getSide().getActors());
        Vec3 opposingEntityPos = PokemonLocatorUtils.getAveragePosition(this.getSide().getOppositeSide().getActors());
        if (actorEntityPos == null || opposingEntityPos == null) return;

        Vec3 actorOffset = opposingEntityPos.subtract(actorEntityPos);
        double actorDistance = actorOffset.length();
        if (actorDistance == 0.0) return;

        BattlePokemon battlePokemon = this.getBattlePokemon();
        float width = battlePokemon.getOriginalPokemon().getForm().getHitbox().width();
        float scale = battlePokemon.getOriginalPokemon().getForm().getBaseScale();
        float pokemonWidth = width * scale;

        double minDistance = 8.0;
        Vec3 basePos = actorEntityPos;
        if (actorDistance < minDistance) {
            Vec3 scaledOffset = actorOffset.scale(minDistance / actorDistance);
            basePos = actorEntityPos.subtract(scaledOffset.subtract(actorOffset));
            actorOffset = scaledOffset;
        }

        Vec3 orthogonalVector = new Vec3(actorOffset.x, 0.0, actorOffset.z).normalize();
        if (orthogonalVector.lengthSqr() == 0.0) return;
        orthogonalVector = orthogonalVector.cross(new Vec3(0.0, 1.0, 0.0));

        double centerIndex = (pokemonPerSide - 1) / 2.0;
        double sideOffset = (slotIndex - centerIndex) * 3.5;

        double distFromCenter = Math.abs(slotIndex - centerIndex);
        double forwardOffset = pokemonPerSide == 1 ? 0.30 : 0.30 - (distFromCenter / centerIndex) * 0.15;

        Vec3 candidatePos = basePos.add(actorOffset.scale(forwardOffset)).add(orthogonalVector.scale(sideOffset));
        Vec3 finalPos = candidatePos;

        if (this.getActor() instanceof EntityBackedBattleActor<?> entityBackedActor) {
            LivingEntity entity = entityBackedActor.getEntity();
            if (entity != null) {
                Level level = entity.level();
                Vec3 eyePos = actorEntityPos.add(0.0, entity.getEyeHeight(), 0.0);
                Vec3 targetRayPos = new Vec3(candidatePos.x, entity.getY() + entity.getEyeHeight(), candidatePos.z);
                HitResult hitResult = level.clip(new ClipContext(eyePos, targetRayPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));

                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    if (hitResult.getLocation().distanceTo(actorEntityPos) < pokemonWidth) finalPos = basePos;
                    else finalPos = new Vec3(hitResult.getLocation().x, candidatePos.y, hitResult.getLocation().z);
                }
            }
        }

        AsymmetricBattlesAPI.LOGGER.info("pnx={} counted={} base={} cand={} final={} fellBack={}", pnx, pokemonPerSide, basePos, candidatePos, finalPos, finalPos == basePos);

        cir.setReturnValue(finalPos);
    }
}
