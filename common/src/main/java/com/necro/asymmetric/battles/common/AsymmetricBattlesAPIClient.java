package com.necro.asymmetric.battles.common;

import com.cobblemon.mod.common.api.gui.ColourLibrary;
import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.client.gui.interact.battleRequest.BattleConfigureGUI;
import com.cobblemon.mod.common.net.messages.client.PlayerInteractOptionsPacket.Options;
import com.cobblemon.mod.common.net.messages.server.BattleChallengeResponsePacket;
import com.necro.asymmetric.battles.common.api.AsymmetricBattleFormats;
import com.necro.asymmetric.battles.common.battle.AsymmetricOptions;
import kotlin.Unit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

import static com.cobblemon.mod.common.util.LocalizationUtilsKt.lang;
import static com.cobblemon.mod.common.util.MiscUtilsKt.cobblemonResource;

public class AsymmetricBattlesAPIClient {
    public static void init() {
        Map<Options, BattleConfigureGUI.BattleTypeTile> battleRequestMap = BattleConfigureGUI.Companion.getBattleRequestMap();
        battleRequestMap.put(AsymmetricOptions.QUADRUPLE_BATTLE, createTile("quadruple", AsymmetricOptions.QUADRUPLE_BATTLE, AsymmetricBattleFormats.GEN_9_QUADRUPLES));
        battleRequestMap.put(AsymmetricOptions.PENTUPLE_BATTLE, createTile("pentuple", AsymmetricOptions.PENTUPLE_BATTLE, AsymmetricBattleFormats.GEN_9_PENTUPLES));
        battleRequestMap.put(AsymmetricOptions.SEXTUPLE_BATTLE, createTile("sextuple", AsymmetricOptions.SEXTUPLE_BATTLE, AsymmetricBattleFormats.GEN_9_SEXTUPLES));
    }

    private static BattleConfigureGUI.BattleTypeTile createTile(String name, Options options, BattleFormat format) {
        return new BattleConfigureGUI.BattleTypeTile(
            options,
            format,
            ResourceLocation.fromNamespaceAndPath(AsymmetricBattlesAPI.MODID, String.format("textures/gui/interact/request/battle_request_%s.png", name)),
            cobblemonResource("textures/gui/interact/request/battle_request_overlay.png"),
            lang("ui.challenge.challenge_title").withStyle(ChatFormatting.BOLD),
            Component.translatable(String.format("asymmetricbattles.battle.types.%ss", name)).withStyle(ChatFormatting.BOLD),
            lang("ui.challenge.challenge").withStyle(ChatFormatting.BOLD),
            ColourLibrary.SIDE_1_BATTLE_COLOUR,
            (packet, battleFormat) -> {
                BattleConfigureGUI.Companion.sendBattleRequest(battleFormat, packet);
                return Unit.INSTANCE;
            },
            (packet, requestID, accept) -> {
                new BattleChallengeResponsePacket(packet.getNumericTargetId(), requestID, packet.getSelectedPokemonId(), accept).sendToServer();
                return Unit.INSTANCE;
            }
        );
    }
}
