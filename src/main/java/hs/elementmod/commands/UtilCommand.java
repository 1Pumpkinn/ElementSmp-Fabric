package hs.elementmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class UtilCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("util")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(UtilCommand::giveUtils));
    }

    private static int giveUtils(CommandContext<ServerCommandSource> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayerEntity player)) {
            context.getSource().sendError(Text.literal("Only players can use this command"));
            return 0;
        }

        player.sendMessage(Text.literal("Utility items will be added in item system implementation")
                .formatted(Formatting.YELLOW), false);

        return 1;
    }
}
