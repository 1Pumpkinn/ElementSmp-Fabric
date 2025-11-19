package hs.elementmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import hs.elementmod.ElementMod;
import hs.elementmod.managers.TrustManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class TrustCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("trust")
                .then(CommandManager.literal("list")
                        .executes(TrustCommand::listTrusted))
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(TrustCommand::addTrust)))
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(TrustCommand::removeTrust)))
                .then(CommandManager.literal("accept")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(TrustCommand::acceptTrust)))
                .then(CommandManager.literal("deny")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(TrustCommand::denyTrust))));
    }

    private static int listTrusted(CommandContext<ServerCommandSource> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayerEntity player)) {
            context.getSource().sendError(Text.literal("Players only"));
            return 0;
        }

        TrustManager trustManager = ElementMod.getInstance().getTrustManager();
        List<String> names = trustManager.getTrustedNames(player.getUuid(),
                context.getSource().getServer());

        if (names.isEmpty()) {
            player.sendMessage(Text.literal("Trusted: ").formatted(Formatting.AQUA)
                    .append(Text.literal("(none)").formatted(Formatting.WHITE)), false);
        } else {
            player.sendMessage(Text.literal("Trusted: ").formatted(Formatting.AQUA)
                    .append(Text.literal(String.join(", ", names)).formatted(Formatting.WHITE)), false);
        }

        return 1;
    }

    private static int addTrust(CommandContext<ServerCommandSource> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayerEntity player)) {
            context.getSource().sendError(Text.literal("Players only"));
            return 0;
        }

        try {
            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

            if (target.equals(player)) {
                player.sendMessage(Text.literal("You cannot trust yourself")
                        .formatted(Formatting.RED), false);
                return 0;
            }

            TrustManager trustManager = ElementMod.getInstance().getTrustManager();
            trustManager.addPending(target.getUuid(), player.getUuid());

            // Fixed ClickEvent: use the RunCommand record
            Text acceptButton = Text.literal("[ACCEPT]")
                    .formatted(Formatting.GREEN)
                    .styled(style -> style.withClickEvent(
                            new ClickEvent.RunCommand("/trust accept " + player.getName().getString())
                    ));

            Text denyButton = Text.literal("[DENY]")
                    .formatted(Formatting.RED)
                    .styled(style -> style.withClickEvent(
                            new ClickEvent.RunCommand("/trust deny " + player.getName().getString())
                    ));

            target.sendMessage(Text.literal(player.getName().getString() + " wants to trust with you. ")
                    .formatted(Formatting.GOLD)
                    .append(acceptButton)
                    .append(Text.literal(" "))
                    .append(denyButton), false);

            player.sendMessage(Text.literal("Sent trust request to " + target.getName().getString())
                    .formatted(Formatting.GREEN), false);

            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int removeTrust(CommandContext<ServerCommandSource> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayerEntity player)) {
            context.getSource().sendError(Text.literal("Players only"));
            return 0;
        }

        try {
            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

            TrustManager trustManager = ElementMod.getInstance().getTrustManager();
            trustManager.removeMutualTrust(player.getUuid(), target.getUuid());

            player.sendMessage(Text.literal("Removed mutual trust")
                    .formatted(Formatting.YELLOW), false);

            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int acceptTrust(CommandContext<ServerCommandSource> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayerEntity player)) {
            context.getSource().sendError(Text.literal("Players only"));
            return 0;
        }

        try {
            ServerPlayerEntity from = EntityArgumentType.getPlayer(context, "player");

            TrustManager trustManager = ElementMod.getInstance().getTrustManager();

            if (!trustManager.hasPending(player.getUuid(), from.getUuid())) {
                player.sendMessage(Text.literal("No pending request from that player")
                        .formatted(Formatting.YELLOW), false);
                return 0;
            }

            trustManager.clearPending(player.getUuid(), from.getUuid());
            trustManager.addMutualTrust(player.getUuid(), from.getUuid());

            player.sendMessage(Text.literal("You are now mutually trusted")
                    .formatted(Formatting.GREEN), false);

            from.sendMessage(Text.literal(player.getName().getString() + " accepted your trust request")
                    .formatted(Formatting.GREEN), false);

            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int denyTrust(CommandContext<ServerCommandSource> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayerEntity player)) {
            context.getSource().sendError(Text.literal("Players only"));
            return 0;
        }

        try {
            ServerPlayerEntity from = EntityArgumentType.getPlayer(context, "player");

            TrustManager trustManager = ElementMod.getInstance().getTrustManager();

            if (trustManager.hasPending(player.getUuid(), from.getUuid())) {
                trustManager.clearPending(player.getUuid(), from.getUuid());

                player.sendMessage(Text.literal("Denied trust request")
                        .formatted(Formatting.YELLOW), false);

                from.sendMessage(Text.literal(player.getName().getString() + " denied your trust request")
                        .formatted(Formatting.RED), false);
            } else {
                player.sendMessage(Text.literal("No pending request from that player")
                        .formatted(Formatting.YELLOW), false);
            }

            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
}
