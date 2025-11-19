package hs.elementmod.listeners;

import hs.elementmod.ElementMod;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unified ability activation listener for all elements
 * Handles keybind-based ability activation without per-element classes
 *
 * Uses right-click for abilities:
 * - Single right-click = Ability 1
 * - Shift + right-click = Ability 2
 * - Double right-click = Use item normally
 */
public class AbilityEventListeners {

    private static final int DOUBLE_TAP_TICKS = 5;   // 5 ticks = 250 ms
    private static final int CHECK_DELAY_TICKS = 6;  // slightly after double-tap window

    private static final Map<UUID, TapTracker> tapTrackers = new ConcurrentHashMap<>();

    public static void register() {
        // Handle item right-click (main-hand only)
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient() || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }

            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }

            handleAbilityActivation(serverPlayer);
            return ActionResult.PASS;
        });

        // Tick handler for ability checks
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (UUID id : tapTrackers.keySet()) {
                TapTracker tracker = tapTrackers.get(id);
                if (tracker == null) continue;

                tracker.ticksWaiting++;
                tracker.ticksSinceLastTap++;

                // Once enough ticks pass and no second tap happened → trigger ability
                if (!tracker.doubleTapped && tracker.ticksWaiting >= CHECK_DELAY_TICKS) {
                    ServerPlayerEntity player = server.getPlayerManager().getPlayer(id);
                    if (player != null) {
                        boolean success = tracker.shiftHeld ?
                                ElementMod.getInstance().getElementManager().useAbility2(player) :
                                ElementMod.getInstance().getElementManager().useAbility1(player);

                        if (success) {
                            tapTrackers.remove(id);
                        }
                    }
                }

                // Remove if idle for too long
                if (tracker.ticksWaiting > 40) {
                    tapTrackers.remove(id);
                }
            }
        });
    }

    private static void handleAbilityActivation(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        TapTracker tracker = tapTrackers.computeIfAbsent(id, x -> new TapTracker());

        // Double tap check
        if (tracker.ticksSinceLastTap <= DOUBLE_TAP_TICKS) {
            tracker.doubleTapped = true;
            tapTrackers.remove(id);
            return; // allow normal item action
        }

        // First tap
        tracker.shiftHeld = player.isSneaking();
        tracker.ticksSinceLastTap = 0;
        tracker.doubleTapped = false;
        tracker.ticksWaiting = 0;
    }

    private static class TapTracker {
        boolean shiftHeld = false;
        boolean doubleTapped = false;
        int ticksSinceLastTap = 999;
        int ticksWaiting = 0;
    }
}

/**
 * Alternative: Custom keybinding approach (optional)
 * This creates dedicated ability keys that players can customize
 */
class CustomKeybindAbilityListener {
    // Client-side only - keybindings
    private static KeyBinding ability1Key;
    private static KeyBinding ability2Key;

    public static void registerClient() {
        ability1Key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.elementmod.ability1",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.elementmod.abilities"
        ));

        ability2Key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.elementmod.ability2",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F,
                "category.elementmod.abilities"
        ));
    }

    // Note: In Fabric, you'd need to send packets to server when keys are pressed
    // The current right-click approach is simpler and doesn't require custom networking
}