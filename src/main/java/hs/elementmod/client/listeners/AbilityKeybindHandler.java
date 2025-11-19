package hs.elementmod.client.listeners;

import hs.elementmod.network.AbilityPacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Handles ability keybinds on the client side
 * Sends packets to the server when keys are pressed
 */
public class AbilityKeybindHandler {

    private static KeyBinding ability1Key;
    private static KeyBinding ability2Key;

    private static boolean ability1WasPressed = false;
    private static boolean ability2WasPressed = false;

    // Create custom category for element mod keybindings
    private static final KeyBinding.Category ELEMENT_MOD_CATEGORY =
            KeyBinding.Category.create(Identifier.of("elementmod", "abilities"));

    /**
     * Register keybindings and tick listener
     */
    public static void register() {
        // Register keybindings with proper category
        ability1Key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.elementmod.ability1",       // translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,                 // default key: R
                ELEMENT_MOD_CATEGORY             // category object
        ));

        ability2Key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.elementmod.ability2",       // translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F,                 // default key: F
                ELEMENT_MOD_CATEGORY             // category object
        ));

        // Register tick event to listen for key presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Handle ability 1 key
            if (ability1Key.isPressed()) {
                if (!ability1WasPressed) {
                    AbilityPacket.send(1);
                    ability1WasPressed = true;
                }
            } else {
                ability1WasPressed = false;
            }

            // Handle ability 2 key
            if (ability2Key.isPressed()) {
                if (!ability2WasPressed) {
                    AbilityPacket.send(2);
                    ability2WasPressed = true;
                }
            } else {
                ability2WasPressed = false;
            }
        });
    }
}