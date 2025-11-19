package hs.elementmod.client.listeners;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class CustomKeybindAbilityListener {

    // KeyBindings
    private static KeyBinding ability1Key;
    private static KeyBinding ability2Key;

    // Custom category for ElementMod abilities (Identifier-based)
    private static final KeyBinding.Category ELEMENTMOD_CATEGORY =
            new KeyBinding.Category(Identifier.of("elementmod", "abilities"));

    public static void registerClient() {
        ability1Key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.elementmod.ability1",        // translation key
                InputUtil.Type.KEYSYM,            // type of input
                GLFW.GLFW_KEY_R,                  // default key
                ELEMENTMOD_CATEGORY               // Identifier-based category
        ));

        ability2Key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.elementmod.ability2",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F,
                ELEMENTMOD_CATEGORY
        ));
    }

    // Example usage note:
    // Detect key presses client-side and send packets to server for ability activation.
    // This class only registers keybindings.
}
