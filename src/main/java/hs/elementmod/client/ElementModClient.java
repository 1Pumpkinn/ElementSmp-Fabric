package hs.elementmod.client;

import hs.elementmod.client.listeners.AbilityKeybindHandler;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side mod initializer
 * Registers client-specific features like keybindings
 */
public class ElementModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register keybindings for abilities
        AbilityKeybindHandler.register();
    }
}