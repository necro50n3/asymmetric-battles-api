package com.necro.asymmetric.battles.neoforge.showdown;

import com.necro.asymmetric.battles.common.showdown.ShowdownLoader;
import net.neoforged.fml.loading.LoadingModList;

public class NeoForgeShowdownLoader extends ShowdownLoader {
    @Override
    protected boolean isMegaShowdownLoaded() {
        return LoadingModList.get().getModFileById("mega_showdown") != null;
    }

    @Override
    protected boolean isGenesisFormsLoaded() {
        return LoadingModList.get().getModFileById("genesisforms") != null;
    }
}
