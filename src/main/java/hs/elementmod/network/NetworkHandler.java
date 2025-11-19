package hs.elementmod.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class NetworkHandler {

    public static void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(AbilityPacket.ID,
                (server, player, handler, buf, context) -> {
                    // read packet
                    AbilityPacket packet = AbilityPacket.fromBuf(buf);

                    // run on main server thread
                    server.execute(() -> {
                        AbilityPacket.handle(packet, player);
                    });
                });
    }
}
