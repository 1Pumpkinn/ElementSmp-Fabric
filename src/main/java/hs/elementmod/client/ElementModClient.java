package hs.elementmod.client;

import hs.elementmod.network.AbilityPacket;
import net.fabricmc.api.ClientModInitializer;

public class ElementModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Example: send ability 1 packet
        AbilityPacket.send(1);
    }
}
