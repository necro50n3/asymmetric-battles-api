package com.necro.asymmetric.battles.common.api.spawning;

import com.bedrockk.molang.Expression;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition;
import com.cobblemon.mod.common.api.molang.ExpressionLike;
import com.cobblemon.mod.common.api.npc.NPCClass;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.spawning.MoonPhaseRange;
import com.cobblemon.mod.common.api.spawning.TimeRange;
import com.cobblemon.mod.common.api.spawning.condition.CompositeSpawningCondition;
import com.cobblemon.mod.common.api.spawning.condition.SpawningCondition;
import com.cobblemon.mod.common.api.spawning.detail.PossibleHeldItem;
import com.cobblemon.mod.common.api.spawning.multiplier.WeightMultiplier;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePositionType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.adapters.*;
import com.cobblemon.mod.common.util.math.DoubleRange;
import com.google.common.collect.Sets;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.mojang.datafixers.util.Either;
import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import com.necro.asymmetric.battles.common.util.DoubleRangeAdapter;
import com.necro.asymmetric.battles.common.util.IBattleSpawn;
import kotlin.ranges.IntRange;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;

public class BattleSpawnDetail extends BattleSpawnTarget implements JsonSerializer<BattleSpawnDetail>, JsonDeserializer<BattleSpawnDetail> {
    public static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .setLenient()
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition.class, Biome.class).getType(), BiomeLikeConditionAdapter.INSTANCE)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition.class, Block.class).getType(), BlockLikeConditionAdapter.INSTANCE)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition.class, Fluid.class).getType(), FluidLikeConditionAdapter.INSTANCE)
        .registerTypeAdapter(
            TypeToken.getParameterized(Either.class, ResourceLocation.class, TypeToken.getParameterized(TagKey.class, Structure.class).getType()).getType(),
            new EitherIdentifierOrTagAdapter<>(Registries.STRUCTURE)
        )
        .registerTypeAdapter(SpawnablePositionType.class, RegisteredSpawnablePositionAdapter.INSTANCE)
        .registerTypeAdapter(ResourceLocation.class, IdentifierAdapter.INSTANCE)
        .registerTypeAdapter(SpawningCondition.class, SpawningConditionAdapter.INSTANCE)
        .registerTypeAdapter(TimeRange.class, TimeRange.Companion.getAdapter())
        .registerTypeAdapter(MoonPhaseRange.class, new IntRangesAdapter<>(MoonPhaseRange.Companion.getMoonPhaseRanges(), MoonPhaseRange::new))
        .registerTypeAdapter(PokemonProperties.class, PokemonPropertiesAdapterKt.getPokemonPropertiesShortAdapter())
        .registerTypeAdapter(CompoundTag .class, NbtCompoundAdapter.INSTANCE)
        .registerTypeAdapter(IntRange.class, IntRangeAdapter.INSTANCE)
        .registerTypeAdapter(DoubleRange.class, DoubleRangeAdapter.INSTANCE)
        .registerTypeAdapter(PossibleHeldItem.class, PossibleHeldItemAdapter.INSTANCE)
        .registerTypeAdapter(NPCClass.class, NPCClassReferenceAdapter.INSTANCE)
        .registerTypeAdapter(Expression.class, ExpressionAdapter.INSTANCE)
        .registerTypeAdapter(ExpressionLike.class, ExpressionLikeAdapter.INSTANCE)
        .create();

    @SerializedName(value = "level", alternate = { "levelRange" })
    public IntRange levelRange = null;
    @SerializedName(value = "offset", alternate = { "levelOffset", "levelRangeOffset" })
    public IntRange levelRangeOffset = new IntRange(-5, 0);
    @SerializedName(value = "relativeOffset", alternate = { "levelRelativeOffset", "levelRangeRelativeOffset" })
    public DoubleRange levelRangeRelativeOffset = null;

    @SerializedName(value = "conditions", alternate = { "condition" })
    public List<SpawningCondition<?>> conditions = List.of();
    @SerializedName(value = "anticonditions", alternate = { "anticondition" })
    public List<SpawningCondition<?>> anticonditions = List.of();
    public CompositeSpawningCondition compositeCondition = null;

    public double weight = 1.0;
    @SerializedName(value = "weightMultipliers", alternate = { "weightMultiplier" })
    public List<WeightMultiplier> weightMultipliers = List.of();

    public List<String> neededInstalledMods = List.of();
    public List<String> neededUninstalledMods = List.of();

    public static BattleSpawnDetail basic(PokemonProperties properties, IntRange levelRangeOffset, double weight) {
        BattleSpawnDetail detail = new BattleSpawnDetail();
        detail.pokemon = properties.asString(" ");
        detail.properties = properties;
        detail.properties.setOriginalString(detail.pokemon);
        detail.species = properties.getSpecies() != null && properties.getSpecies().contains(":") ? properties.getSpecies().split(":")[1] : properties.getSpecies();
        detail.levelRangeOffset = levelRangeOffset;
        detail.weight = weight;
        return detail;
    }

    public boolean isSatisfiedBy(BattleSpawnablePosition spawnablePosition) {
        if (!this.conditions.isEmpty() && this.conditions.stream().noneMatch(condition -> condition.isSatisfiedBy(spawnablePosition))) return false;
        else if (!this.anticonditions.isEmpty() && this.anticonditions.stream().anyMatch(condition -> condition.isSatisfiedBy(spawnablePosition))) return false;
        else if (this.compositeCondition != null && !this.compositeCondition.satisfiedBy(spawnablePosition)) return false;
        return true;
    }

    public boolean isValid() {
        boolean isValid = true;
        if (this.pokemon.isBlank()) isValid = false;
        else if (!this.conditions.isEmpty() && this.conditions.stream().anyMatch(condition -> !condition.isValid())) isValid = false;
        else if (!this.anticonditions.isEmpty() && this.anticonditions.stream().anyMatch(condition -> !condition.isValid())) isValid = false;
        isValid = isValid && this.isModDependencySatisfied();
        if (!isValid) AsymmetricBattlesAPI.LOGGER.error("Unable to parse a spawn due to a syntax error for {}", this.pokemon);
        return isValid;
    }

    private boolean isModDependencySatisfied() {
        if (!this.neededInstalledMods.isEmpty() && this.neededInstalledMods.stream().anyMatch(mod -> !Cobblemon.implementation.isModInstalled(mod))) return false;
        else return this.neededUninstalledMods.isEmpty() || this.neededUninstalledMods.stream().noneMatch(mod -> Cobblemon.implementation.isModInstalled(mod));
    }

    public double getWeight(SpawnablePosition spawnablePosition) {
        double weight = this.weight;
        for (WeightMultiplier weightMultiplier : this.weightMultipliers) {
            if (!weightMultiplier.getConditions().isEmpty() && weightMultiplier.getConditions().stream().noneMatch(condition -> condition.isSatisfiedBy(spawnablePosition))) continue;
            if (!weightMultiplier.getAnticonditions().isEmpty() && weightMultiplier.getAnticonditions().stream().anyMatch(condition -> condition.isSatisfiedBy(spawnablePosition))) continue;
            weight *= weightMultiplier.getMultiplier();
        }
        return weight;
    }

    public IntRange getLevelRange(int level) {
        if (this.levelRange != null) return this.levelRange;
        else if (this.levelRangeRelativeOffset != null) return new IntRange(clampLevel((int) (level * this.levelRangeRelativeOffset.getStart())), clampLevel((int) (level * this.levelRangeRelativeOffset.getEndInclusive())));
        return new IntRange(clampLevel(level + this.levelRangeOffset.getStart()), clampLevel(level + this.levelRangeOffset.getEndInclusive()));
    }

    private static int clampLevel(int level) {
        return Math.clamp(level, 1, 100);
    }

    public Pokemon create(int baseLevel, ServerPlayer player) {
        Pokemon spawn = this.properties().create(player);
        spawn.setForcedAspects(new HashSet<>(Sets.union(this.properties().getAspects(), spawn.getForcedAspects())));
        spawn.updateAspects();
        spawn.updateForm();
        ((IBattleSpawn) spawn).aba_setBattleSpawn();
        IntRange levelRange = this.getLevelRange(baseLevel);
        spawn.setLevel(player.getRandom().nextInt(levelRange.getStart(), levelRange.getEndInclusive() + 1));
        return spawn;
    }

    @Override
    public BattleSpawnDetail deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return GSON.fromJson(json, BattleSpawnDetail.class);
    }

    @Override
    public JsonElement serialize(BattleSpawnDetail src, Type typeOfSrc, JsonSerializationContext context) {
        return GSON.toJsonTree(src);
    }
}
