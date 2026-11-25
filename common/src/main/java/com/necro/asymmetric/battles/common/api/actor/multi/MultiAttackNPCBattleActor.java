package com.necro.asymmetric.battles.common.api.actor.multi;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.api.storage.party.PartyStore;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.battles.ai.StrongBattleAI;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.npc.NPCBattleActor;
import com.cobblemon.mod.common.entity.npc.NPCEntity;
import com.necro.asymmetric.battles.common.api.actor.MultiAttackBattleActor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MultiAttackNPCBattleActor extends NPCBattleActor implements MultiAttackBattleActor {
    private final List<List<ShowdownActionResponse>> multiAttackResponses;

    public MultiAttackNPCBattleActor(NPCEntity npc, List<BattlePokemon> pokemonList, int skill, @NotNull BattleAI battleAI) {
        super(npc, pokemonList, skill, battleAI);
        this.multiAttackResponses = new ArrayList<>();
    }

    public MultiAttackNPCBattleActor(NPCEntity npc, PartyStore party, int skill) {
        this(npc, party.toBattleTeam(false, npc.getNpc().getAutoHealParty(), null), skill, new StrongBattleAI(skill));
    }

    @Override
    public void onChoiceRequested() {
        this.onMultiChoiceRequested();
    }

    @Override
    public List<List<ShowdownActionResponse>> getMultiAttackResponses() {
        return this.multiAttackResponses;
    }
}
