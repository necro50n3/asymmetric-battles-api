package com.necro.asymmetric.battles.common;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

import static com.cobblemon.mod.common.util.LocalizationUtilsKt.battleLang;

public class AsymmetricBattlesAPI {
    public static final String MODID = "asymmetricbattles";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        LOGGER.info("Initiating {}", MODID);

        CobblemonEvents.THROWN_POKEBALL_HIT.subscribe(Priority.HIGHEST, event -> {
            PokemonBattle battle = event.getPokemon().getBattle();
            if (battle == null || battle.getSide2().getActors().length == 0 || battle.getSide2().getActors()[0].getType() != ActorType.WILD) return;
            int active = battle.getSide2().getActivePokemon().size();
            if (active > 1) {
                Entity entity = event.getPokeBall().getOwner();
                if (entity instanceof Player player) player.displayClientMessage(battleLang("pokeball_not_alone"), false);
                event.cancel();
            }
        });
    }
}
