package com.necro.asymmetric.battles.fabric.compat.modmenu;

import com.necro.asymmetric.battles.common.AsymmetricBattlesAPI;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public class AsymmetricModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ConfigurationScreen.ConfigurationSectionScreen(
            parent,
            ModConfig.Type.COMMON,
            ModConfigs.getModConfigs(AsymmetricBattlesAPI.MODID).getFirst(),
            Component.translatable("asymmetricbattles.mod.title")
        );
    }
}
