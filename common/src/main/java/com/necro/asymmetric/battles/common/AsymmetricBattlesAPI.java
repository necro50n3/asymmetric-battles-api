package com.necro.asymmetric.battles.common;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleEndedEvent;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.platform.events.PlatformEvents;
import com.cobblemon.mod.common.platform.events.ServerPlayerEvent;
import com.mojang.logging.LogUtils;
import com.necro.asymmetric.battles.common.api.actor.HordeBattleActor;
import com.necro.asymmetric.battles.common.config.AsymmetricConfig;
import com.necro.asymmetric.battles.common.config.serializer.PrimitiveYamlConfigSerializer;
import com.necro.asymmetric.battles.common.util.IBattleSpawn;
import me.shedaniel.autoconfig.AutoConfig;
import org.slf4j.Logger;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class AsymmetricBattlesAPI {
    public static final String MODID = "asymmetricbattles";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static AsymmetricConfig CONFIG;

    public static void init() {
        LOGGER.info("Initiating {}", MODID);

        AutoConfig.register(AsymmetricConfig.class, PrimitiveYamlConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(AsymmetricConfig.class).getConfig();

        CobblemonEvents.BATTLE_FAINTED.subscribe(Priority.LOWEST, event -> {
            BattlePokemon pokemon = event.getKilled();
            if (!(pokemon.getActor() instanceof HordeBattleActor hordeActor)) return;
            if (!pokemon.getUuid().equals(hordeActor.getLeader().getUuid())) return;
            List<BattlePokemon> remaining = hordeActor.getPokemonList().stream().filter(p -> p.getHealth() > 0).toList();
            if (remaining.isEmpty()) return;
            int random = new Random().nextInt(remaining.size());
            hordeActor.setLeader(remaining.get(random));
        });

        CobblemonEvents.BATTLE_ENDED.subscribe(Priority.LOWEST, (Consumer<BattleEndedEvent>) event -> onBattleEnd(event.getBattle()));
        PlatformEvents.SERVER_PLAYER_LOGOUT.subscribe(Priority.HIGHEST, (Consumer<ServerPlayerEvent.Logout>) event -> onBattleEnd(BattleRegistry.getBattleByParticipatingPlayer(event.getPlayer())));
    }

    private static void onBattleEnd(PokemonBattle battle) {
        if (battle == null) return;
        for (ActiveBattlePokemon active : battle.getActivePokemon()) {
            if (active.getBattlePokemon() == null || !active.isAlive()) continue;
            PokemonEntity pokemon = active.getBattlePokemon().getEntity();
            if (pokemon != null && ((IBattleSpawn) pokemon.getPokemon()).aba_isBattleSpawn()) pokemon.discard();
        }
    }
}
