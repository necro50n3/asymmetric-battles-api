package com.necro.asymmetric.battles.fabric.reloader;

import com.necro.asymmetric.battles.common.api.spawning.BattleSpawnPool;
import com.necro.asymmetric.battles.common.reloader.BattleSpawnReloadListenerImpl;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

public class BattleSpawnReloadListener extends BattleSpawnReloadListenerImpl implements SimpleSynchronousResourceReloadListener {
    private final ResourceLocation id;

    public BattleSpawnReloadListener(ResourceLocation id, String type, Class<? extends BattleSpawnPool> cls) {
        super(type, cls);
        this.id = id;
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        super.load(manager);
    }

    @Override
    public ResourceLocation getFabricId() {
        return this.id;
    }
}
