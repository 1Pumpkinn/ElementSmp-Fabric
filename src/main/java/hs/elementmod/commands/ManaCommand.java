package hs.elementmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import hs.elementmod.ElementMod;
import hs.elementmod.data.PlayerData;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ManaCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("mana")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("reset")
                        .executes(ManaCommand::resetSelf)
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(ManaCommand::resetPlayer)))
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                .executes(ManaCommand::setSelf)
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .executes(ManaCommand::setPlayer)))));
    }

    private static int resetSelf(CommandContext<ServerCommandSource> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayerEntity player)) {
            context.getSource().sendError(Text.literal("Only players can use this command"));
            return 0;
        }

        int maxMana = ElementMod.getInstance().getConfigManager().getMaxMana();
        PlayerData pd = ElementMod.getInstance().getManaManager().get(player.getUuid());
        pd.setMana(maxMana);
        ElementMod.getInstance().getDataStore().save(pd);

        player.sendMessage(Text.literal("Mana reset to " + maxMana)
                .formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int resetPlayer(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
            int maxMana = ElementMod.getInstance().getConfigManager().getMaxMana();

            PlayerData pd = ElementMod.getInstance().getManaManager().get(target.getUuid());
            pd.setMana(maxMana);
            ElementMod.getInstance().getDataStore().save(pd);

            context.getSource().sendFeedback(
                    () -> Text.literal("Reset mana for " + target.getName().getString())
                            .formatted(Formatting.GREEN),
                    true
            );

            target.sendMessage(Text.literal("Your mana has been reset")
                    .formatted(Formatting.GREEN), false);

            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int setSelf(CommandContext<ServerCommandSource> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayerEntity player)) {
            context.getSource().sendError(Text.literal("Only players can use this command"));
            return 0;
        }

        int amount = IntegerArgumentType.getInteger(context, "amount");
        PlayerData pd = ElementMod.getInstance().getManaManager().get(player.getUuid());
        pd.setMana(amount);
        ElementMod.getInstance().getDataStore().save(pd);

        player.sendMessage(Text.literal("Mana set to " + amount)
                .formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int setPlayer(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
            int amount = IntegerArgumentType.getInteger(context, "amount");

            PlayerData pd = ElementMod.getInstance().getManaManager().get(target.getUuid());
            pd.setMana(amount);
            ElementMod.getInstance().getDataStore().save(pd);

            context.getSource().sendFeedback(
                    () -> Text.literal("Set mana to " + amount + " for " + target.getName().getString())
                            .formatted(Formatting.GREEN),
                    true
            );

            target.sendMessage(Text.literal("Your mana has been set to " + amount)
                    .formatted(Formatting.GREEN), false);

            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
}