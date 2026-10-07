package com.necro.asymmetric.battles.neoforge.reloader;

import com.necro.asymmetric.battles.common.api.spawning.BattleSpawnPool;
import com.necro.asymmetric.battles.common.reloader.BattleSpawnReloadListenerImpl;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

public class BattleSpawnReloadListener extends BattleSpawnReloadListenerImpl implements ResourceManagerReloadListener {
    public BattleSpawnReloadListener(String type, Class<? extends BattleSpawnPool> cls) {
        super(type, cls);
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        super.load(manager);
    }
}
