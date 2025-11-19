package hs.elementmod.listeners;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
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
        // Implementation from Paper version
    }

    private static void onPlayerQuit(ServerPlayerEntity player) {
        // Implementation from Paper version
    }

    private static void onPlayerRespawn(ServerPlayerEntity player) {
        // Implementation from Paper version
    }
}
