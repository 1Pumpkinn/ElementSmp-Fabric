package hs.elementmod.listeners;

import hs.elementmod.ElementMod;
import hs.elementmod.data.PlayerData;
import hs.elementmod.elements.ElementType;
import hs.elementmod.items.*;
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
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemEventListeners {
    private static final Random random = new Random();

    /** Call this during server initialization */
    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {

            // Only run server-side
            if (world.isClient()) {
                return ActionResult.PASS;
            }

            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }

            ItemStack stack = player.getStackInHand(hand);

            ServerWorld serverWorld = (ServerWorld) player.getEntityWorld();

            if (handleUpgrader(serverPlayer, stack, hand, serverWorld)) return ActionResult.SUCCESS;
            if (handleReroller(serverPlayer, stack, hand, serverWorld)) return ActionResult.SUCCESS;
            if (handleAdvancedReroller(serverPlayer, stack, hand, serverWorld)) return ActionResult.SUCCESS;
            if (handleCoreConsumption(serverPlayer, stack, hand, serverWorld)) return ActionResult.SUCCESS;

            return ActionResult.PASS;
        });
    }

    /** Handle upgrader items */
    private static boolean handleUpgrader(ServerPlayerEntity player, ItemStack stack, Hand hand, ServerWorld world) {
        int upgraderLevel = 0;

        if (Upgrader1Item.isUpgrader1(stack)) upgraderLevel = 1;
        else if (Upgrader2Item.isUpgrader2(stack)) upgraderLevel = 2;

        if (upgraderLevel == 0) return false;

        ElementMod mod = ElementMod.getInstance();
        PlayerData pd = mod.getElementManager().data(player.getUuid());
        ElementType currentElement = pd.getCurrentElement();

        if (currentElement == null) {
            player.sendMessage(Text.literal("You need an element before you can upgrade!")
                    .formatted(Formatting.RED), false);
            return false;
        }

        int currentLevel = pd.getUpgradeLevel(currentElement);

        if (currentLevel >= upgraderLevel) {
            player.sendMessage(Text.literal("You already have this upgrade level or higher!")
                    .formatted(Formatting.YELLOW), false);
            return false;
        }

        pd.setUpgradeLevel(currentElement, upgraderLevel);
        mod.getDataStore().save(pd);

        stack.decrement(1);

        world.playSound(
                null,
                player.getBlockPos(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                SoundCategory.PLAYERS,
                1.0f,
                1.2f
        );

        player.sendMessage(
                Text.literal("✦ Upgraded to Level " + (upgraderLevel == 1 ? "I" : "II") + "! ✦")
                        .formatted(Formatting.GREEN, Formatting.BOLD),
                false
        );

        mod.getElementManager().applyUpsides(player);
        return true;
    }

    /** Handle reroller item */
    private static boolean handleReroller(ServerPlayerEntity player, ItemStack stack, Hand hand, ServerWorld world) {
        if (!RerollerItem.isReroller(stack)) return false;

        ElementMod mod = ElementMod.getInstance();
        PlayerData pd = mod.getElementManager().data(player.getUuid());

        List<ElementType> basicElements = new ArrayList<>();
        for (ElementType type : ElementType.values()) {
            if (type != ElementType.LIFE && type != ElementType.DEATH) {
                basicElements.add(type);
            }
        }

        if (basicElements.isEmpty()) {
            player.sendMessage(Text.literal("No elements available to reroll!")
                    .formatted(Formatting.RED), false);
            return false;
        }

        ElementType newElement = basicElements.get(random.nextInt(basicElements.size()));
        ElementType oldElement = pd.getCurrentElement();

        if (oldElement != null) mod.getElementManager().get(oldElement).clearEffects(player);

        pd.setCurrentElement(newElement);
        pd.setCurrentElementUpgradeLevel(0);
        mod.getDataStore().save(pd);

        stack.decrement(1);

        world.playSound(
                null,
                player.getBlockPos(),
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
        );

        player.sendMessage(
                Text.literal("✦ Your element has been rerolled to ")
                        .formatted(Formatting.GOLD)
                        .append(Text.literal(newElement.name()).formatted(Formatting.AQUA))
                        .append(Text.literal("! ✦").formatted(Formatting.GOLD)),
                false
        );

        mod.getElementManager().applyUpsides(player);
        return true;
    }

    /** Handle advanced reroller item */
    private static boolean handleAdvancedReroller(ServerPlayerEntity player, ItemStack stack, Hand hand, ServerWorld world) {
        if (!AdvancedRerollerItem.isAdvancedReroller(stack)) return false;

        ElementMod mod = ElementMod.getInstance();
        PlayerData pd = mod.getElementManager().data(player.getUuid());

        List<ElementType> availableElements = new ArrayList<>();
        for (ElementType type : ElementType.values()) {
            if (type == ElementType.LIFE && pd.hasElementItem(ElementType.LIFE)) {
                availableElements.add(type);
            } else if (type == ElementType.DEATH && pd.hasElementItem(ElementType.DEATH)) {
                availableElements.add(type);
            } else if (type != ElementType.LIFE && type != ElementType.DEATH) {
                availableElements.add(type);
            }
        }

        if (availableElements.isEmpty()) {
            player.sendMessage(Text.literal("No elements available to reroll!")
                    .formatted(Formatting.RED), false);
            return false;
        }

        ElementType newElement = availableElements.get(random.nextInt(availableElements.size()));
        ElementType oldElement = pd.getCurrentElement();

        if (oldElement != null) mod.getElementManager().get(oldElement).clearEffects(player);

        pd.setCurrentElement(newElement);
        pd.setCurrentElementUpgradeLevel(0);
        mod.getDataStore().save(pd);

        stack.decrement(1);

        world.playSound(
                null,
                player.getBlockPos(),
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                SoundCategory.PLAYERS,
                1.0f,
                0.8f
        );

        player.sendMessage(
                Text.literal("✦ Your element has been rerolled to ")
                        .formatted(Formatting.DARK_PURPLE)
                        .append(Text.literal(newElement.name()).formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal("! ✦").formatted(Formatting.DARK_PURPLE)),
                false
        );

        mod.getElementManager().applyUpsides(player);
        return true;
    }

    /** Handle core consumption items */
    private static boolean handleCoreConsumption(ServerPlayerEntity player, ItemStack stack, Hand hand, ServerWorld world) {
        if (stack == null || stack.isEmpty()) return false;
        if (!ItemUtil.isElementItem(stack)) return false;

        ElementType type = ItemUtil.getElementType(stack);
        if (type != ElementType.LIFE && type != ElementType.DEATH) return false;

        ElementMod mod = ElementMod.getInstance();
        PlayerData pd = mod.getElementManager().data(player.getUuid());

        if (pd.hasElementItem(type)) {
            player.sendMessage(
                    Text.literal("You have already consumed the " + type.name() + " Element!")
                            .formatted(Formatting.RED),
                    false
            );
            return false;
        }

        if (type == ElementType.LIFE && mod.getDataStore().isLifeElementCrafted()) {
            player.sendMessage(Text.literal("The Life Element has already been crafted!")
                    .formatted(Formatting.RED), false);
            return false;
        }

        if (type == ElementType.DEATH && mod.getDataStore().isDeathElementCrafted()) {
            player.sendMessage(Text.literal("The Death Element has already been crafted!")
                    .formatted(Formatting.RED), false);
            return false;
        }

        ElementType oldElement = pd.getCurrentElement();
        if (oldElement != null) mod.getElementManager().get(oldElement).clearEffects(player);

        pd.setCurrentElement(type);
        pd.setCurrentElementUpgradeLevel(0);
        pd.addElementItem(type);
        mod.getDataStore().save(pd);

        stack.decrement(1);

        world.playSound(
                null,
                player.getBlockPos(),
                SoundEvents.ITEM_TOTEM_USE,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
        );

        String emoji = type == ElementType.LIFE ? "💚" : "💀";

        player.sendMessage(
                Text.literal(emoji + " You consumed the ")
                        .formatted(type == ElementType.LIFE ? Formatting.GREEN : Formatting.DARK_GRAY)
                        .append(Text.literal(type.name() + " Element").formatted(Formatting.BOLD))
                        .append(Text.literal("! " + emoji)
                                .formatted(type == ElementType.LIFE ? Formatting.GREEN : Formatting.DARK_GRAY)),
                false
        );

        mod.getElementManager().applyUpsides(player);
        return true;
    }
}
