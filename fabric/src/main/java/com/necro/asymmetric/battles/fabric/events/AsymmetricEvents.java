package com.necro.asymmetric.battles.fabric.events;

import com.mojang.brigadier.CommandDispatcher;
import com.necro.asymmetric.battles.common.api.AsymmetricAPI;
import com.necro.asymmetric.battles.common.command.MultiBattleCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;

public class AsymmetricEvents {
    public static void onServerStarted(MinecraftServer server) {
        AsymmetricAPI.onServerStart();
    }

    @SuppressWarnings("unused")
    public static void onCommandRegistration(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        MultiBattleCommand.register(dispatcher);
    }
}
