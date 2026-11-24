package com.necro.asymmetric.battles.common.command;

import com.cobblemon.mod.common.battles.TeamManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class MultiBattleCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("multibattle")
            .requires(CommandSourceStack::isPlayer)
            .then(Commands.literal("team")
                .then(Commands.literal("create")
                    .executes(context -> createTeam(context.getSource().getPlayerOrException()))
                )
                .then(Commands.literal("disband")
                    .executes(context -> disbandTeam(context.getSource().getPlayerOrException()))
                )
            )
        );
    }

    private static int createTeam(ServerPlayer player) {
        if (TeamManager.INSTANCE.getTeam(player) != null) return 0;
        TeamManager.INSTANCE.createTeam(player);
        return 1;
    }

    private static int disbandTeam(ServerPlayer player) {
        TeamManager.MultiBattleTeam team = TeamManager.INSTANCE.getTeam(player);
        if (team == null) return 0;
        TeamManager.INSTANCE.disbandTeam(team);
        return 1;
    }
}
