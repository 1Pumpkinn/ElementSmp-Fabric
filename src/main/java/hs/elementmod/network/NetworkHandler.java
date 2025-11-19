package hs.elementmod.network;

import hs.elementmod.ElementMod;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Handles network packet registration and processing
 * Fabric 1.21.10 with Yarn mappings
 */
public class NetworkHandler {

    /**
     * Register all network packets
     * Must be called during mod initialization (before server starts)
     */
    public static void registerPackets() {
        // Register the packet type for C2S (Client to Server)
        PayloadTypeRegistry.playC2S().register(AbilityPacket.ID, AbilityPacket.CODEC);

        // Register server-side receiver
        ServerPlayNetworking.registerGlobalReceiver(AbilityPacket.ID, (payload, context) -> {
            // Execute on main server thread to avoid concurrency issues
            context.server().execute(() -> {
                // Call the static handle method in AbilityPacket
                AbilityPacket.handle(payload, context.player());
            });
        });

        ElementMod.LOGGER.info("Network packets registered successfully");
    }
}