package com.necro.asymmetric.battles.common;

import com.necro.asymmetric.battles.common.compat.ModCompat;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public abstract class AsymmetricBattlesAPIMixinPluginImpl implements IMixinConfigPlugin {
    protected abstract String mixin(String pkg);

    protected final Map<String, Supplier<Boolean>> MIXINS = Map.of(
        mixin("msd.StatChangeRendererMixin"), () -> this.isModLoaded(ModCompat.MEGA_SHOWDOWN.getModid())
    );

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (MIXINS.containsKey(mixinClassName)) return MIXINS.get(mixinClassName).get();
        return true;
    }

    protected abstract boolean isModLoaded(String... mods);

    protected abstract boolean isModAndNewerThan(String mod, String version);

    protected abstract boolean isModAndOlderThan(String mod, String version);

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
