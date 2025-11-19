package hs.elementmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import hs.elementmod.ElementMod;
import hs.elementmod.elements.ElementType;
import hs.elementmod.managers.ElementManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ElementCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("element")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("element", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            for (ElementType type : ElementType.values()) {
                                                builder.suggest(type.name().toLowerCase());
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(ElementCommand::setElement))))
                .then(CommandManager.literal("debug")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(ElementCommand::debugElement))));
    }

    private static int setElement(CommandContext<ServerCommandSource> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
        String elementStr = StringArgumentType.getString(context, "element");

        ElementType type;
        try {
            type = ElementType.valueOf(elementStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            context.getSource().sendError(Text.literal("Invalid element: " + elementStr));
            return 0;
        }
        ElementMod.getInstance().getElementManager().setElement(target, type);

        context.getSource().sendFeedback(
                () -> Text.literal("Set ")
                        .append(Text.literal(target.getName().getString()).formatted(Formatting.AQUA))
                        .append(Text.literal("'s element to "))
                        .append(Text.literal(type.name()).formatted(Formatting.GOLD)),
                true
        );
        target.sendMessage(Text.literal("Your element has been set to ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(type.name()).formatted(Formatting.AQUA))
                .append(Text.literal(" by an admin").formatted(Formatting.GREEN)), false);
        return 1;
    }


    private static int debugElement(CommandContext<ServerCommandSource> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
        ElementType element = ElementMod.getInstance().getElementManager().getPlayerElement(target);

        context.getSource().sendFeedback(
                () -> Text.literal("=== Element Debug for " + target.getName().getString() + " ===")
                        .formatted(Formatting.GOLD),
                false
        );

        context.getSource().sendFeedback(
                () -> Text.literal("Element: " + (element != null ? element.name() : "null"))
                        .formatted(Formatting.YELLOW),
                false
        );

        return 1;
    }
}

