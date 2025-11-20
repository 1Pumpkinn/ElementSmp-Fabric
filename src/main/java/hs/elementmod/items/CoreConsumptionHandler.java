package hs.elementmod.items;

import hs.elementmod.ElementMod;
import hs.elementmod.data.PlayerData;
import hs.elementmod.elements.ElementType;
import hs.elementmod.managers.ElementManager;
import hs.elementmod.util.ItemUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

/**
 * Handles consuming Life/Death core items in Fabric
 * Converted from Paper event handling to Fabric
 */
public final class CoreConsumptionHandler {
    private CoreConsumptionHandler() {}

    /**
     * Handles consuming Life/Death core items on right-click
     * Returns true if the event was handled
     */
    public static boolean handleCoreConsume(ServerPlayerEntity player, ItemStack stack, Hand hand) {
        if (stack == null || stack.isEmpty()) return false;
        if (!ItemUtil.isElementItem(stack)) return false;

        ElementType type = ItemUtil.getElementType(stack);
        if (type != ElementType.LIFE && type != ElementType.DEATH) return false;

        ElementMod mod = ElementMod.getInstance();
        ElementManager elementManager = mod.getElementManager();

        PlayerData pd = elementManager.data(player.getUuid());

        // Switch to the core's element
        elementManager.setElement(player, type);

        // Mark that they have consumed this core
        pd.addElementItem(type);
        mod.getDataStore().save(pd);

        // Consume the item
        stack.decrement(1);

        player.sendMessage(
                Text.literal("You consumed the ")
                        .formatted(Formatting.GREEN)
                        .append(Text.literal(ElementCoreItem.getDisplayName(type)))
                        .append(Text.literal("!").formatted(Formatting.GREEN)),
                false
        );

        return true;
    }

    /**
     * Check if a player can consume a core
     */
    public static boolean canConsumeCore(ServerPlayerEntity player, ElementType type) {
        ElementMod mod = ElementMod.getInstance();
        PlayerData pd = mod.getElementManager().data(player.getUuid());

        // Check if already consumed
        if (pd.hasElementItem(type)) {
            player.sendMessage(
                    Text.literal("You have already consumed the " + type.name() + " Element!")
                            .formatted(Formatting.RED),
                    false
            );
            return false;
        }

        // Check if server-wide limit reached (for Life/Death)
        if (type == ElementType.LIFE && mod.getDataStore().isLifeElementCrafted()) {
            player.sendMessage(
                    Text.literal("The Life Element has already been crafted by someone!")
                            .formatted(Formatting.RED),
                    false
            );
            return false;
        }

        if (type == ElementType.DEATH && mod.getDataStore().isDeathElementCrafted()) {
            player.sendMessage(
                    Text.literal("The Death Element has already been crafted by someone!")
                            .formatted(Formatting.RED),
                    false
            );
            return false;
        }

        return true;
    }
}