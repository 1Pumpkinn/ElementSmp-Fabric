package hs.elementmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import hs.elementmod.elements.ElementType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ElementInfoCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("elements")
                .executes(ElementInfoCommand::showUsage)
                .then(CommandManager.argument("element", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (ElementType type : ElementType.values()) {
                                builder.suggest(type.name().toLowerCase());
                            }
                            return builder.buildFuture();
                        })
                        .executes(ElementInfoCommand::showElementInfo)));
    }

    private static int showUsage(CommandContext<ServerCommandSource> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayerEntity player)) {
            return 0;
        }

        player.sendMessage(Text.literal("Usage: /elements <element>")
                .formatted(Formatting.YELLOW), false);
        return 1;
    }

    private static int showElementInfo(CommandContext<ServerCommandSource> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayerEntity player)) {
            return 0;
        }

        String elementStr = StringArgumentType.getString(context, "element");

        try {
            ElementType type = ElementType.valueOf(elementStr.toUpperCase());

            player.sendMessage(Text.empty(), false);
            player.sendMessage(Text.literal("✦ " + type.name() + " ELEMENT ✦")
                    .formatted(Formatting.GOLD, Formatting.BOLD), false);
            player.sendMessage(Text.empty(), false);

            // Add element-specific information here

            return 1;
        } catch (IllegalArgumentException e) {
            player.sendMessage(Text.literal("❌ Unknown element: " + elementStr)
                    .formatted(Formatting.RED), false);
            return 0;
        }
    }
}