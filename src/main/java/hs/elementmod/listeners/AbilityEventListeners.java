package hs.elementmod.listeners;

import hs.elementmod.ElementMod;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

public class AbilityEventListeners {
    private static final long DOUBLE_TAP_THRESHOLD_MS = 250;
    private static final java.util.Map<java.util.UUID, TapTracker> tapTrackers =
            new java.util.concurrent.ConcurrentHashMap<>();

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) {
                return TypedActionResult.pass(player.getStackInHand(hand));
            }

            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return TypedActionResult.pass(player.getStackInHand(hand));
            }

            // Check for swap hands keybind (F)
            // This is simplified - you'll need proper keybind detection
            handleAbilityActivation(serverPlayer);

            return TypedActionResult.pass(player.getStackInHand(hand));
        });
    }

    private static void handleAbilityActivation(ServerPlayerEntity player) {
        java.util.UUID playerId = player.getUuid();
        TapTracker tracker = tapTrackers.computeIfAbsent(playerId, k -> new TapTracker());

        long currentTime = System.currentTimeMillis();

        if (tracker.isDoubleTap(currentTime)) {
            tracker.reset();
            tapTrackers.remove(playerId);
            return; // Double tap - allow normal swap
        }

        tracker.recordTap(currentTime, player.isSneaking());

        // Schedule ability activation check
        scheduleAbilityCheck(player, playerId, currentTime);
    }

    private static void scheduleAbilityCheck(ServerPlayerEntity player,
                                             java.util.UUID playerId,
                                             long tapTime) {
        player.getServer().execute(() -> {
            try {
                Thread.sleep(300); // CHECK_DELAY_TICKS * 50ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            TapTracker tracker = tapTrackers.get(playerId);
            if (tracker == null || !tracker.isValidTap(tapTime)) {
                return; // Second tap occurred
            }

            // Execute ability
            boolean success = tracker.wasShiftHeld ?
                    ElementMod.getInstance().getElementManager().useAbility2(player) :
                    ElementMod.getInstance().getElementManager().useAbility1(player);

            if (success) {
                tapTrackers.remove(playerId);
            }
        });
    }

    private static class TapTracker {
        private long lastTapTime = 0;
        private boolean wasShiftHeld = false;

        boolean isDoubleTap(long currentTime) {
            return lastTapTime > 0 &&
                    (currentTime - lastTapTime) <= DOUBLE_TAP_THRESHOLD_MS;
        }

        void recordTap(long time, boolean shiftHeld) {
            this.lastTapTime = time;
            this.wasShiftHeld = shiftHeld;
        }

        boolean isValidTap(long originalTime) {
            return lastTapTime == originalTime;
        }

        void reset() {
            lastTapTime = 0;
            wasShiftHeld = false;
        }
    }
}
