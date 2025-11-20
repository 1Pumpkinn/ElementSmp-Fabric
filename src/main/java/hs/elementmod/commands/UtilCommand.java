package hs.elementmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import hs.elementmod.items.ModItems;
import net.minecraft.item.ItemStack;
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

        // Give all utility items
        player.giveItemStack(new ItemStack(ModItems.UPGRADER_1, 1));
        player.giveItemStack(new ItemStack(ModItems.UPGRADER_2, 1));
        player.giveItemStack(new ItemStack(ModItems.REROLLER, 1));
        player.giveItemStack(new ItemStack(ModItems.ADVANCED_REROLLER, 1));
        player.giveItemStack(new ItemStack(ModItems.LIFE_CORE, 1));
        player.giveItemStack(new ItemStack(ModItems.DEATH_CORE, 1));

        player.sendMessage(
                Text.literal("✦ Gave you all utility items! ✦")
                        .formatted(Formatting.GREEN, Formatting.BOLD),
                false
        );

        player.sendMessage(
                Text.literal("• Upgrader I & II - Right-click to upgrade")
                        .formatted(Formatting.GRAY),
                false
        );

        player.sendMessage(
                Text.literal("• Reroller & Advanced Reroller - Right-click to reroll element")
                        .formatted(Formatting.GRAY),
                false
        );

        player.sendMessage(
                Text.literal("• Life & Death Cores - Right-click to consume")
                        .formatted(Formatting.GRAY),
                false
        );

        return 1;
    }
}