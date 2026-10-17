package com.necro.asymmetric.battles.common.reloader;

import com.google.gson.Gson;
import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import com.necro.asymmetric.battles.common.api.spawning.AsymmetricSpawnPool;
import com.necro.asymmetric.battles.common.api.spawning.BattleSpawnPool;
import com.necro.asymmetric.battles.common.api.spawning.GlobalBattleSpawnPool;
import com.necro.asymmetric.battles.common.registry.SpawnPoolRegistry;
import com.necro.asymmetric.battles.common.registry.SpawnPoolTypeRegistry;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public abstract class BattleSpawnReloadListenerImpl {
    protected final String type;
    protected final Gson gson;
    protected final Class<? extends BattleSpawnPool> cls;

    public BattleSpawnReloadListenerImpl(String type) {
        this(type, AsymmetricSpawnPool.GSON, BattleSpawnPool.class);
    }

    public BattleSpawnReloadListenerImpl(String type, Gson gson, Class<? extends BattleSpawnPool> cls) {
        this.type = type;
        this.gson = gson;
        this.cls = cls;
    }

    public void load(@NotNull ResourceManager manager) {
        manager.listResources("battle_spawns/" + this.type, path -> path.toString().endsWith(".json")).forEach((id, resource) -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
                BattleSpawnPool pool = this.gson.fromJson(reader, this.cls);
                SpawnPoolTypeRegistry.register(this.type, pool);
            } catch (Exception e) {
                AsymmetricBattlesAPI.LOGGER.error("Failed to load battle spawn pool {}", id, e);
            }
        });

        SpawnPoolRegistry registry = SpawnPoolTypeRegistry.get(this.type);
        AsymmetricBattlesAPI.LOGGER.info("Loaded {} {} battle spawns.", registry != null ? registry.registry.size() : 0, this.type);

        manager.listResources("global_battle_spawns/" + this.type, path -> path.toString().endsWith(".json")).forEach((id, resource) -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
                GlobalBattleSpawnPool pool = AsymmetricSpawnPool.GSON.fromJson(reader, GlobalBattleSpawnPool.class);
                SpawnPoolTypeRegistry.register(this.type, pool);
            } catch (Exception e) {
                AsymmetricBattlesAPI.LOGGER.error("Failed to load global battle spawn pool {}", id, e);
            }
        });

        AsymmetricBattlesAPI.LOGGER.info("Loaded {} {} global battle spawns.", registry != null ? registry.global.size() : 0, this.type);
    }
}
