package hs.elementmod.client.listeners;

import hs.elementmod.network.AbilityPacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class AbilityKeybindHandler {

    private static KeyBinding ability1Key;
    private static KeyBinding ability2Key;

    private static boolean ability1WasPressed = false;
    private static boolean ability2WasPressed = false;

    private static final KeyBinding.Category CATEGORY =
            new KeyBinding.Category(Identifier.of("elementmod", "abilities"));

    public static void register() {

        ability1Key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.elementmod.ability1",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                CATEGORY
        ));

        ability2Key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.elementmod.ability2",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F,
                CATEGORY
        ));

        // Tick event for handling input
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (ability1Key.isPressed()) {
                if (!ability1WasPressed) {
                    AbilityPacket.send(1);
                    ability1WasPressed = true;
                }
            } else ability1WasPressed = false;

            if (ability2Key.isPressed()) {
                if (!ability2WasPressed) {
                    AbilityPacket.send(2);
                    ability2WasPressed = true;
                }
            } else ability2WasPressed = false;
        });
    }
}
