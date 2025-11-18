package hs.elementmod.listeners;

import hs.elementmod.ElementMod;
import hs.elementmod.data.PlayerData;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

public class GameModeListener {
    public static void register() {
        // Note: Fabric doesn't have a direct gamemode change event
        // You would need to implement this via mixin or by checking each tick
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (oldPlayer.interactionManager.getGameMode() !=
                    newPlayer.interactionManager.getGameMode()) {
                onGameModeChange(newPlayer, newPlayer.interactionManager.getGameMode());
            }
        });
    }

    private static void onGameModeChange(ServerPlayerEntity player, GameMode newMode) {
        if (newMode == GameMode.CREATIVE) {
            int maxMana = ElementMod.getInstance().getConfigManager().getMaxMana();
            PlayerData pd = ElementMod.getInstance().getManaManager().get(player.getUuid());
            pd.setMana(maxMana);
        }
    }
}