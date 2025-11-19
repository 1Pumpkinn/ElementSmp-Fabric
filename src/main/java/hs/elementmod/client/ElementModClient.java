package hs.elementmod.client;

import hs.elementmod.client.listeners.CustomKeybindAbilityListener;
import net.fabricmc.api.ClientModInitializer;

public class ElementModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register the keybindings
        CustomKeybindAbilityListener.registerClient();
    }
}
