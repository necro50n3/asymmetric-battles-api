package com.necro.asymmetric.battles.fabric.showdown;

import com.necro.asymmetric.battles.common.showdown.ShowdownLoader;
import net.fabricmc.loader.api.FabricLoader;

public class FabricShowdownLoader extends ShowdownLoader {
    @Override
    protected boolean isMegaShowdownLoaded() {
        return FabricLoader.getInstance().isModLoaded("mega_showdown");
    }

    @Override
    protected boolean isGenesisFormsLoaded() {
        return FabricLoader.getInstance().isModLoaded("genesisforms");
    }
}
