package com.necro.asymmetric.battles.common.reloader;

import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import com.necro.asymmetric.battles.common.api.spawning.BattleSpawnPool;
import com.necro.asymmetric.battles.common.registry.SpawnRegistry;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public abstract class BattleSpawnReloadListenerImpl {
    protected final String type;

    protected BattleSpawnReloadListenerImpl(String type) {
        this.type = type;
    }

    public void load(@NotNull ResourceManager manager) {
        manager.listResources("battle_spawns/" + this.type, path -> path.toString().endsWith(".json")).forEach((id, resource) -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
                BattleSpawnPool pool = BattleSpawnPool.GSON.fromJson(reader, BattleSpawnPool.class);
                SpawnRegistry.register(this.type, pool);
            } catch (Exception e) {
                AsymmetricBattlesAPI.LOGGER.error("Failed to load status effect {}", id, e);
            }
        });
    }
}
