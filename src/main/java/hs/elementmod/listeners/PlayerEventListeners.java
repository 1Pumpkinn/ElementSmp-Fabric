package hs.elementmod.listeners;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerEventListeners {
    public static void register() {

        // Join event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            onPlayerJoin(player);
        });

        // Quit event
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.player;
            onPlayerQuit(player);
        });

        // Respawn event
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            onPlayerRespawn(newPlayer);
        });
    }

    private static void onPlayerJoin(ServerPlayerEntity player) {
        // Load player data and apply element upsides
        if (hs.elementmod.ElementMod.getInstance() == null) return;
        var mod = hs.elementmod.ElementMod.getInstance();
        var em = mod.getElementManager();
        var mm = mod.getManaManager();

        // Ensure mana cache is populated and player data loaded
        if (mm != null) mm.get(player.getUuid());

        // Apply element upsides for this player (if any)
        if (em != null) em.applyUpsides(player);
    }

    private static void onPlayerQuit(ServerPlayerEntity player) {
        if (hs.elementmod.ElementMod.getInstance() == null) return;
        var mod = hs.elementmod.ElementMod.getInstance();
        var ds = mod.getDataStore();
        var mm = mod.getManaManager();

        // Save player data on disconnect
        try {
            if (mm != null) mm.save(player.getUuid());
            if (ds != null) ds.save(ds.getPlayerData(player.getUuid()));
        } catch (Exception e) {
            hs.elementmod.ElementMod.LOGGER.error("Failed to save player data on quit for {}", player.getUuid(), e);
        }
    }

    private static void onPlayerRespawn(ServerPlayerEntity player) {
        if (hs.elementmod.ElementMod.getInstance() == null) return;
        var mod = hs.elementmod.ElementMod.getInstance();
        var em = mod.getElementManager();
        var mm = mod.getManaManager();

        if (mm != null) mm.get(player.getUuid());
        if (em != null) em.applyUpsides(player);
    }
}
