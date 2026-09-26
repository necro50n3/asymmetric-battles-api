package com.necro.asymmetric.battles.common.mixin.battle;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.battles.BattleSide;
import com.cobblemon.mod.common.battles.Targetable;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.asymmetric.battles.common.util.AsymmetricUtils;
import com.necro.asymmetric.battles.common.util.PokemonLocatorUtils;
import kotlin.collections.CollectionsKt;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
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

    @Override
    public @NotNull List<Targetable> getAdjacent() {
        if (AsymmetricUtils.isAsymmetricBattle(this.getFormat())) return CollectionsKt.filter(
            this.getAllActivePokemon(),
            pokemon -> {
                if (pokemon == this) return false;
                BattlePokemon battlePokemon = ((ActiveBattlePokemon) pokemon).getBattlePokemon();
                return battlePokemon != null && battlePokemon.getHealth() > 0;
            }
        );
        else return Targetable.super.getAdjacent();
    }

    @Inject(method = "getSendOutPosition", at = @At("RETURN"), cancellable = true, remap = false)
    private void getSendOutPositionExtension(CallbackInfoReturnable<Vec3> cir) {
        int pokemonPerSide = this.getFormat().getBattleType().getPokemonPerSide();

        if (pokemonPerSide < 4 || pokemonPerSide > 6) return;
        if (!AsymmetricUtils.isAsymmetricBattle(this.getFormat())) return;
        if (cir.getReturnValue() == null) return;

        List<ActiveBattlePokemon> presentPokemon = this.getSide().getActivePokemon().stream()
            .filter(activePokemon -> {
                BattlePokemon battlePokemon = activePokemon.getBattlePokemon();
                return battlePokemon != null && battlePokemon.getHealth() > 0;
            })
            .sorted(Comparator.comparingInt(activePokemon -> activePokemon.getDigit(true)))
            .toList();

        int presentCount = presentPokemon.size();
        if (presentCount == 0) return;
        int presentIndex = presentPokemon.indexOf((ActiveBattlePokemon) (Object) this);
        if (presentIndex < 0) return;

        Vec3 actorEntityPos = PokemonLocatorUtils.getAveragePosition(this.getSide().getActors());
        Vec3 opposingEntityPos = PokemonLocatorUtils.getAveragePosition(this.getSide().getOppositeSide().getActors());
        if (actorEntityPos == null || opposingEntityPos == null) return;

        Vec3 actorOffset = opposingEntityPos.subtract(actorEntityPos);
        Vec3 forwardVector = new Vec3(actorOffset.x, 0.0, actorOffset.z).normalize();
        if (forwardVector.lengthSqr() == 0.0) return;

        Vec3 orthogonalVector = forwardVector.cross(new Vec3(0.0, 1.0, 0.0));

        double forwardOffset;
        double sideOffset;

        switch (presentCount) {
            case 1 -> {
                forwardOffset = this.getActor().getBattle().isPvW() ? 0.4 : 0.3;

                BattlePokemon battlePokemon = this.getBattlePokemon();
                if (battlePokemon != null) {
                    double width = battlePokemon.getOriginalPokemon().getForm().getHitbox().width();
                    double scale = battlePokemon.getOriginalPokemon().getForm().getBaseScale();
                    sideOffset = -0.3 - width * scale;
                }
                else sideOffset = 0.0;
            }
            case 2 -> {
                forwardOffset = 0.33;
                sideOffset = (presentIndex - 0.5) * 5.0;
            }
            case 3 -> {
                forwardOffset = presentIndex == 1 ? 0.3 : 0.15;
                sideOffset = (presentIndex - 1.0) * 3.5;
            }
            default -> {
                forwardOffset = 0.15;
                sideOffset = (presentIndex - ((presentCount - 1) / 2.0)) * 3.5;
            }
        }

        Vec3 adjustedPosition = cir.getReturnValue().add(actorOffset.scale(forwardOffset)).add(orthogonalVector.scale(sideOffset));
        cir.setReturnValue(adjustedPosition);
    }
}
