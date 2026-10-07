package com.necro.asymmetric.battles.fabric.reloader;

import com.google.gson.Gson;
import com.necro.asymmetric.battles.common.api.spawning.BattleSpawnPool;
import com.necro.asymmetric.battles.common.reloader.BattleSpawnReloadListenerImpl;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

public class BattleSpawnReloadListener extends BattleSpawnReloadListenerImpl implements SimpleSynchronousResourceReloadListener {
    private final ResourceLocation id;

    public BattleSpawnReloadListener(ResourceLocation id, String type) {
        super(type);
        this.id = id;
    }

    public BattleSpawnReloadListener(ResourceLocation id, String type, Gson gson, Class<? extends BattleSpawnPool> cls) {
        super(type, gson, cls);
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
