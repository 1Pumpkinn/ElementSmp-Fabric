package hs.elementmod.listeners;

import hs.elementmod.ElementMod;
import hs.elementmod.data.PlayerData;
import hs.elementmod.elements.ElementType;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerEventListeners {
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            onPlayerJoin(player);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.player;
            onPlayerQuit(player);
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            onPlayerRespawn(newPlayer);
        });
    }

    private static void onPlayerJoin(ServerPlayerEntity player) {
        ElementMod mod = ElementMod.getInstance();
        PlayerData pd = mod.getDataStore().getPlayerData(player.getUuid());

        if (pd.getCurrentElement() == null) {
            // Open element selection GUI or assign random element
            ElementMod.LOGGER.info("Player {} joined without element", player.getName().getString());
        }

        // Apply element upsides
        mod.getElementManager().applyUpsides(player);

        // Ensure mana loaded
        mod.getManaManager().get(player.getUuid());
    }

    private static void onPlayerQuit(ServerPlayerEntity player) {
        ElementMod mod = ElementMod.getInstance();

        // Cancel any rolling
        mod.getElementManager().cancelRolling(player);

        // Save mana
        mod.getManaManager().save(player.getUuid());

        // Save player data
        mod.getDataStore().save(mod.getDataStore().getPlayerData(player.getUuid()));
    }

    private static void onPlayerRespawn(ServerPlayerEntity player) {
        ElementMod mod = ElementMod.getInstance();

        // Reapply element effects after respawn
        server.execute(() -> {
            try {
                Thread.sleep(100); // Small delay to ensure player is fully respawned
                mod.getElementManager().applyUpsides(player);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}