package hs.elementmod.elements.abilities.impl.earth;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.abilities.BaseAbility;
import hs.elementmod.elements.ElementContext;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.*;

public class EarthTunnelAbility extends BaseAbility {
    private static final Set<Block> TUNNELABLE = Set.of(
            Blocks.STONE, Blocks.DEEPSLATE, Blocks.DIRT, Blocks.GRASS_BLOCK,
            Blocks.COBBLESTONE, Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE,
            Blocks.GRAVEL, Blocks.SAND, Blocks.RED_SAND, Blocks.SANDSTONE,
            Blocks.TUFF, Blocks.CALCITE, Blocks.DRIPSTONE_BLOCK,
            Blocks.BLACKSTONE, Blocks.ANCIENT_DEBRIS, Blocks.CRIMSON_NYLIUM,
            Blocks.WARPED_HYPHAE, Blocks.SOUL_SAND, Blocks.BASALT, Blocks.SOUL_SOIL,
            Blocks.NETHERRACK,
            Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
            Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
            Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE
    );

    private final ElementMod mod;
    private final Map<UUID, TunnelState> activeTunnels = new HashMap<>();

    public EarthTunnelAbility(ElementMod mod) {
        super("earth_tunnel", 50, 10, 1);
        this.mod = mod;

        // Register tick handler
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            activeTunnels.entrySet().removeIf(entry -> {
                TunnelState state = entry.getValue();
                ServerPlayerEntity player = state.player;

                if (!player.isAlive() || player.isRemoved() || state.ticks >= 1000) {
                    setActive(player, false);
                    return true;
                }

                Vec3d direction = player.getRotationVec(1.0f).normalize();
                Vec3d mineLocation = direction.getY() < -0.5 ?
                        player.getPos().add(direction.multiply(1.0)) :
                        player.getEyePos().add(direction.multiply(1.5));

                breakTunnel(player.getServerWorld(), mineLocation, player);

                player.getServerWorld().spawnParticles(
                        ParticleTypes.BLOCK,
                        mineLocation.x, mineLocation.y, mineLocation.z,
                        10, 0.5, 0.5, 0.5, 0.1);

                state.ticks++;
                return false;
            });
        });
    }

    @Override
    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        UUID playerId = player.getUuid();

        // Check if already active - cancel if so
        if (activeTunnels.containsKey(playerId)) {
            activeTunnels.remove(playerId);
            player.sendMessage(Text.literal("Tunneling cancelled")
                    .formatted(Formatting.YELLOW), false);
            setActive(player, false);
            return true;
        }

        // Start tunneling
        ServerWorld world = player.getServerWorld();
        world.playSound(null, player.getBlockPos(),
                SoundEvents.BLOCK_STONE_BREAK, SoundCategory.PLAYERS,
                1f, 0.8f);

        player.sendMessage(Text.literal("Tunneling started! Press again to cancel.")
                .formatted(Formatting.GOLD), false);

        activeTunnels.put(playerId, new TunnelState(player));
        setActive(player, true);

        return true;
    }

    private void breakTunnel(ServerWorld world, Vec3d center, ServerPlayerEntity player) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos blockPos = BlockPos.ofFloored(center.add(x, y, z));
                    BlockState state = world.getBlockState(blockPos);

                    if (TUNNELABLE.contains(state.getBlock())) {
                        world.breakBlock(blockPos, true, player);
                        world.playSound(null, blockPos,
                                SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS,
                                0.3f, 1.0f);
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Earth Tunnel";
    }

    @Override
    public String getDescription() {
        return "Create a tunnel through earth and stone. Press again to cancel.";
    }

    private static class TunnelState {
        final ServerPlayerEntity player;
        int ticks;

        TunnelState(ServerPlayerEntity player) {
            this.player = player;
            this.ticks = 0;
        }
    }
}
