package com.necro.asymmetric.battles.neoforge.reloader;

import com.google.gson.Gson;
import com.necro.asymmetric.battles.common.api.spawning.BattleSpawnPool;
import com.necro.asymmetric.battles.common.reloader.BattleSpawnReloadListenerImpl;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

public class BattleSpawnReloadListener extends BattleSpawnReloadListenerImpl implements ResourceManagerReloadListener {
    public BattleSpawnReloadListener(String type) {
        super(type);
    }

    public BattleSpawnReloadListener(String type, Gson gson, Class<? extends BattleSpawnPool> cls) {
        super(type, gson, cls);
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        super.load(manager);
    }
}
