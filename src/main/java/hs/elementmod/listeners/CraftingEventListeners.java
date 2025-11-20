package hs.elementmod.listeners;

import hs.elementmod.ElementMod;
import hs.elementmod.data.PlayerData;
import hs.elementmod.elements.ElementType;
import hs.elementmod.items.*;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;


/**
 * Handles crafting-related events in Fabric
 */
public class CraftingEventListeners {

    public static void register() {
        ElementMod.LOGGER.info("Crafting event listeners registered (requires mixin implementation)");
    }

    /** Handle crafting result; called from a mixin */
    public static boolean handleCrafting(ServerPlayerEntity player, ItemStack result, ScreenHandler handler) {
        if (result.isEmpty()) return false;
        if (handleUpgraderCrafting(player, result, handler)) return true;
        if (handleElementCoreCrafting(player, result, handler)) return true;
        return false;
    }

    /** Handle crafting with upgraders */
    private static boolean handleUpgraderCrafting(ServerPlayerEntity player, ItemStack result, ScreenHandler handler) {
        int upgraderLevel = 0;
        Slot upgraderSlot = null;

        for (Slot slot : handler.slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            if (Upgrader1Item.isUpgrader1(stack)) {
                upgraderLevel = 1;
                upgraderSlot = slot;
                break;
            } else if (Upgrader2Item.isUpgrader2(stack)) {
                upgraderLevel = 2;
                upgraderSlot = slot;
                break;
            }
        }

        if (upgraderLevel == 0) return false;

        ElementMod mod = ElementMod.getInstance();
        PlayerData pd = mod.getElementManager().data(player.getUuid());
        ElementType currentElement = pd.getCurrentElement();

        if (currentElement == null) {
            player.sendMessage(Text.literal("You need an element before you can upgrade!").formatted(Formatting.RED), false);
            return false;
        }

        int currentLevel = pd.getUpgradeLevel(currentElement);
        if (currentLevel >= upgraderLevel) {
            player.sendMessage(Text.literal("You already have this upgrade level or higher!").formatted(Formatting.YELLOW), false);
            return false;
        }

        pd.setUpgradeLevel(currentElement, upgraderLevel);
        mod.getDataStore().save(pd);

        if (upgraderSlot != null) upgraderSlot.getStack().decrement(1);

        ServerWorld world = (ServerWorld) player.getEntityWorld();
        world.playSound(
                player,                    // optional excluded player, null for all
                player.getBlockPos(),      // position
                SoundEvents.UI_TOAST_IN,   // sound
                SoundCategory.PLAYERS,
                1.0f,                      // volume
                1.2f                       // pitch
        );



        player.sendMessage(Text.literal("Upgraded to Level " + (upgraderLevel == 1 ? "I" : "II") + "!").formatted(Formatting.GREEN), false);

        mod.getElementManager().applyUpsides(player);
        return true;
    }

    /** Handle crafting element cores (Life/Death) */
    private static boolean handleElementCoreCrafting(ServerPlayerEntity player, ItemStack result, ScreenHandler handler) {
        ElementType coreType = null;

        if (ElementCoreItem.isCore(result, ElementType.LIFE)) coreType = ElementType.LIFE;
        else if (ElementCoreItem.isCore(result, ElementType.DEATH)) coreType = ElementType.DEATH;

        if (coreType == null) return false;

        ElementMod mod = ElementMod.getInstance();
        PlayerData pd = mod.getElementManager().data(player.getUuid());

        // Already crafted globally
        if ((coreType == ElementType.LIFE && mod.getDataStore().isLifeElementCrafted()) ||
                (coreType == ElementType.DEATH && mod.getDataStore().isDeathElementCrafted())) {
            player.sendMessage(Text.literal("This element has already been crafted by someone!").formatted(Formatting.RED), false);
            return false;
        }

        // Player already has it
        if (pd.hasElementItem(coreType)) {
            player.sendMessage(Text.literal("You have already crafted the " + coreType.name() + " Element!").formatted(Formatting.RED), false);
            return false;
        }

        pd.addElementItem(coreType);
        pd.setCurrentElementUpgradeLevel(0);

        if (coreType == ElementType.LIFE) mod.getDataStore().setLifeElementCrafted(true);
        else if (coreType == ElementType.DEATH) mod.getDataStore().setDeathElementCrafted(true);

        mod.getDataStore().save(pd);

        ServerWorld world = (ServerWorld) player.getEntityWorld();
        world.playSound(
                player,                    // excluded player (only this one hears it)
                player.getBlockPos(),      // position
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                SoundCategory.PLAYERS,
                1.0f,                      // volume
                1.0f                       // pitch
        );

        // Broadcast message to all players
        String elementName = coreType == ElementType.LIFE ? "Life" : "Death";
        String emoji = coreType == ElementType.LIFE ? "🌟" : "💀";

        mod.getServer().getPlayerManager().broadcast(
                Text.literal(emoji + " " + player.getName().getString() + " has crafted the " + elementName + " Element! " + emoji)
                        .formatted(coreType == ElementType.LIFE ? Formatting.LIGHT_PURPLE : Formatting.DARK_PURPLE),
                false
        );

        return true;
    }

    /** Remove one item from each slot in the crafting grid */
    private static void consumeIngredients(ScreenHandler handler) {
        for (Slot slot : handler.slots) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) stack.decrement(1);
        }
    }
}
