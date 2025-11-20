package hs.elementmod.listeners;

import hs.elementmod.ElementMod;
import hs.elementmod.data.PlayerData;
import hs.elementmod.elements.ElementType;
import hs.elementmod.items.*;
import hs.elementmod.items.custom.AdvancedRerollerItem;
import hs.elementmod.items.custom.RerollerItem;
import hs.elementmod.items.custom.Upgrader1Item;
import hs.elementmod.items.custom.Upgrader2Item;
import hs.elementmod.util.ItemUtil;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles right-click events for all custom items
 */
public class ItemEventListeners {
    private static final Random random = new Random();

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            // Only run server-side
            if (world.isClient()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            ServerWorld serverWorld = (ServerWorld) player.getEntityWorld();

            // Handle each item type
            if (handleUpgrader(serverPlayer, stack, serverWorld)) return ActionResult.SUCCESS;
            if (handleReroller(serverPlayer, stack, serverWorld)) return ActionResult.SUCCESS;
            if (handleAdvancedReroller(serverPlayer, stack, serverWorld)) return ActionResult.SUCCESS;
            if (handleCoreConsumption(serverPlayer, stack, serverWorld)) return ActionResult.SUCCESS;

            return ActionResult.PASS;
        });

                // Event listeners registered (quiet)
    }

    /**
     * Handle upgrader items (Upgrader I & II)
     */
    private static boolean handleUpgrader(ServerPlayerEntity player, ItemStack stack, ServerWorld world) {
        int upgraderLevel = 0;

        if (Upgrader1Item.isUpgrader1(stack)) {
            upgraderLevel = 1;
        } else if (Upgrader2Item.isUpgrader2(stack)) {
            upgraderLevel = 2;
        }

        if (upgraderLevel == 0) return false;

        ElementMod mod = ElementMod.getInstance();
        PlayerData pd = mod.getElementManager().data(player.getUuid());
        ElementType currentElement = pd.getCurrentElement();

        // If player is not sneaking: use ability 1 instead of upgrading
        if (!player.isSneaking()) {
            boolean used = mod.getElementManager().useAbility1(player);
            if (used) {
                // play a small feedback sound
                world.playSound(
                        null,
                        player.getBlockPos(),
                        SoundEvents.ENTITY_PLAYER_LEVELUP,
                        SoundCategory.PLAYERS,
                        1.0f,
                        1.0f
                );
                return true;
            }

            // If ability failed (no element, not enough mana, etc.), don't proceed to upgrade unless sneaking
            return false;
        }

        // Sneaking + right-click = perform upgrade as before
        // Check if player has an element
        if (currentElement == null) {
            player.sendMessage(
                    Text.literal("❌ You need an element before you can upgrade!")
                            .formatted(Formatting.RED),
                    false
            );
            return false;
        }

        // Check current upgrade level
        int currentLevel = pd.getUpgradeLevel(currentElement);

        if (currentLevel >= upgraderLevel) {
            player.sendMessage(
                    Text.literal("⚠ You already have this upgrade level or higher!")
                            .formatted(Formatting.YELLOW),
                    false
            );
            return false;
        }

        // Apply upgrade
        pd.setUpgradeLevel(currentElement, upgraderLevel);
        mod.getDataStore().save(pd);

        // Consume item
        stack.decrement(1);

        // Play success sound
        world.playSound(
                null,
                player.getBlockPos(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                SoundCategory.PLAYERS,
                1.0f,
                1.2f
        );

        // Send success message
        String levelText = upgraderLevel == 1 ? "I" : "II";
        player.sendMessage(
                Text.literal("✦ Successfully upgraded to Level " + levelText + "! ✦")
                        .formatted(Formatting.GREEN, Formatting.BOLD),
                false
        );

        player.sendMessage(
                Text.literal("You can now use " + (upgraderLevel == 1 ? "basic" : "advanced") + " abilities!")
                        .formatted(Formatting.AQUA),
                false
        );

        // Reapply element upsides with new level
        mod.getElementManager().applyUpsides(player);

        return true;
    }

    /**
     * Handle basic reroller item
     */
    private static boolean handleReroller(ServerPlayerEntity player, ItemStack stack, ServerWorld world) {
        if (!RerollerItem.isReroller(stack)) return false;

        ElementMod mod = ElementMod.getInstance();
        PlayerData pd = mod.getElementManager().data(player.getUuid());

        // Get list of basic elements (exclude Life and Death)
        List<ElementType> basicElements = new ArrayList<>();
        for (ElementType type : ElementType.values()) {
            if (type != ElementType.LIFE && type != ElementType.DEATH) {
                basicElements.add(type);
            }
        }

        if (basicElements.isEmpty()) {
            player.sendMessage(
                    Text.literal("❌ No elements available to reroll!")
                            .formatted(Formatting.RED),
                    false
            );
            return false;
        }

        // Select random element
        ElementType newElement = basicElements.get(random.nextInt(basicElements.size()));
        ElementType oldElement = pd.getCurrentElement();

        // Clear old element effects
        if (oldElement != null && mod.getElementManager().get(oldElement) != null) {
            mod.getElementManager().get(oldElement).clearEffects(player);
        }

        // Set new element and reset upgrade level
        pd.setCurrentElement(newElement);
        pd.setCurrentElementUpgradeLevel(0);
        mod.getDataStore().save(pd);

        // Consume item
        stack.decrement(1);

        // Play reroll sound
        world.playSound(
                null,
                player.getBlockPos(),
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
        );

        // Send success message
        player.sendMessage(
                Text.literal("⚡ Your element has been rerolled to ")
                        .formatted(Formatting.GOLD, Formatting.BOLD)
                        .append(Text.literal(newElement.name())
                                .formatted(Formatting.AQUA, Formatting.BOLD))
                        .append(Text.literal("! ⚡")
                                .formatted(Formatting.GOLD, Formatting.BOLD)),
                false
        );

        if (oldElement != null) {
            player.sendMessage(
                    Text.literal("Previous element: ")
                            .formatted(Formatting.GRAY)
                            .append(Text.literal(oldElement.name())
                                    .formatted(Formatting.DARK_GRAY)),
                    false
            );
        }

        // Apply new element upsides
        mod.getElementManager().applyUpsides(player);

        return true;
    }

    /**
     * Handle advanced reroller item
     */
    private static boolean handleAdvancedReroller(ServerPlayerEntity player, ItemStack stack, ServerWorld world) {
        if (!AdvancedRerollerItem.isAdvancedReroller(stack)) return false;

        ElementMod mod = ElementMod.getInstance();
        PlayerData pd = mod.getElementManager().data(player.getUuid());

        // Get list of all elements the player has access to
        List<ElementType> availableElements = new ArrayList<>();
        for (ElementType type : ElementType.values()) {
            // Include Life/Death only if player has unlocked them
            if (type == ElementType.LIFE && !pd.hasElementItem(ElementType.LIFE)) {
                continue;
            }
            if (type == ElementType.DEATH && !pd.hasElementItem(ElementType.DEATH)) {
                continue;
            }
            availableElements.add(type);
        }

        if (availableElements.isEmpty()) {
            player.sendMessage(
                    Text.literal("❌ No elements available to reroll!")
                            .formatted(Formatting.RED),
                    false
            );
            return false;
        }

        // Select random element
        ElementType newElement = availableElements.get(random.nextInt(availableElements.size()));
        ElementType oldElement = pd.getCurrentElement();

        // Clear old element effects
        if (oldElement != null && mod.getElementManager().get(oldElement) != null) {
            mod.getElementManager().get(oldElement).clearEffects(player);
        }

        // Set new element and reset upgrade level
        pd.setCurrentElement(newElement);
        pd.setCurrentElementUpgradeLevel(0);
        mod.getDataStore().save(pd);

        // Consume item
        stack.decrement(1);

        // Play reroll sound (lower pitch for advanced)
        world.playSound(
                null,
                player.getBlockPos(),
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                SoundCategory.PLAYERS,
                1.0f,
                0.8f
        );

        // Send success message
        player.sendMessage(
                Text.literal("⚡ Your element has been rerolled to ")
                        .formatted(Formatting.DARK_PURPLE, Formatting.BOLD)
                        .append(Text.literal(newElement.name())
                                .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                        .append(Text.literal("! ⚡")
                                .formatted(Formatting.DARK_PURPLE, Formatting.BOLD)),
                false
        );

        if (oldElement != null) {
            player.sendMessage(
                    Text.literal("Previous element: ")
                            .formatted(Formatting.GRAY)
                            .append(Text.literal(oldElement.name())
                                    .formatted(Formatting.DARK_GRAY)),
                    false
            );
        }

        // Apply new element upsides
        mod.getElementManager().applyUpsides(player);

        return true;
    }

    /**
     * Handle element core consumption (Life/Death)
     */
    private static boolean handleCoreConsumption(ServerPlayerEntity player, ItemStack stack, ServerWorld world) {
        if (stack == null || stack.isEmpty()) return false;
        if (!ItemUtil.isElementItem(stack)) return false;

        ElementType type = ItemUtil.getElementType(stack);
        if (type != ElementType.LIFE && type != ElementType.DEATH) return false;

        ElementMod mod = ElementMod.getInstance();
        PlayerData pd = mod.getElementManager().data(player.getUuid());

        // Check if player already has this element
        if (pd.hasElementItem(type)) {
            player.sendMessage(
                    Text.literal("❌ You have already consumed the " + type.name() + " Element!")
                            .formatted(Formatting.RED),
                    false
            );
            return false;
        }

        // Check if element has been crafted globally
        if ((type == ElementType.LIFE && mod.getDataStore().isLifeElementCrafted()) ||
                (type == ElementType.DEATH && mod.getDataStore().isDeathElementCrafted())) {
            player.sendMessage(
                    Text.literal("❌ The " + type.name() + " Element has already been crafted by someone else!")
                            .formatted(Formatting.RED),
                    false
            );
            return false;
        }

        // Clear old element effects
        ElementType oldElement = pd.getCurrentElement();
        if (oldElement != null && mod.getElementManager().get(oldElement) != null) {
            mod.getElementManager().get(oldElement).clearEffects(player);
        }

        // Set new element and unlock it permanently
        pd.setCurrentElement(type);
        pd.setCurrentElementUpgradeLevel(0);
        pd.addElementItem(type);
        mod.getDataStore().save(pd);

        // Consume item
        stack.decrement(1);

        // Play dramatic sound
        world.playSound(
                null,
                player.getBlockPos(),
                SoundEvents.ITEM_TOTEM_USE,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
        );

        // Send success message
        String emoji = type == ElementType.LIFE ? "💚" : "💀";
        Formatting color = type == ElementType.LIFE ? Formatting.GREEN : Formatting.DARK_GRAY;

        player.sendMessage(
                Text.literal(emoji + " You have consumed the ")
                        .formatted(color)
                        .append(Text.literal(type.name() + " Core")
                                .formatted(color, Formatting.BOLD))
                        .append(Text.literal("! " + emoji)
                                .formatted(color)),
                false
        );

        player.sendMessage(
                Text.literal("The " + type.name() + " element is now permanently unlocked!")
                        .formatted(Formatting.YELLOW),
                false
        );

        // Apply new element upsides
        mod.getElementManager().applyUpsides(player);

        return true;
    }
}