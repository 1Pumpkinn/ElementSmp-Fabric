package hs.elementmod.network;

import hs.elementmod.ElementMod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Packet for sending ability activation from client to server
 * Fabric 1.21.10 with Yarn mappings
 */
public record AbilityPacket(int abilityId) implements CustomPayload {

    public static final CustomPayload.Id<AbilityPacket> ID =
            new CustomPayload.Id<>(Identifier.of("elementmod", "ability_packet"));

    // PacketCodec with explicit type parameters to avoid inference issues
    public static final PacketCodec<RegistryByteBuf, AbilityPacket> CODEC =
            new PacketCodec<RegistryByteBuf, AbilityPacket>() {
                @Override
                public AbilityPacket decode(RegistryByteBuf buf) {
                    return new AbilityPacket(buf.readInt());
                }

                @Override
                public void encode(RegistryByteBuf buf, AbilityPacket value) {
                    buf.writeInt(value.abilityId);
                }
            };

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * Send this packet from client to server
     * Call this from client-side code only
     */
    public static void send(int abilityId) {
        ClientPlayNetworking.send(new AbilityPacket(abilityId));
    }

    /**
     * Handle packet on server side
     * Called by NetworkHandler when packet is received
     *
     * @param packet The ability packet received
     * @param player The player who sent the packet
     */
    public static void handle(AbilityPacket packet, ServerPlayerEntity player) {
        int abilityId = packet.abilityId();

        ElementMod mod = ElementMod.getInstance();
        if (mod == null || mod.getElementManager() == null) {
            ElementMod.LOGGER.warn("Received ability packet but ElementMod not initialized");
            return;
        }

        // Execute the requested ability
        boolean success = switch (abilityId) {
            case 1 -> mod.getElementManager().useAbility1(player);
            case 2 -> mod.getElementManager().useAbility2(player);
            default -> {
                ElementMod.LOGGER.warn("Invalid ability ID: {}", abilityId);
                yield false;
            }
        };

        if (!success) {
            ElementMod.LOGGER.debug("Failed to execute ability {} for player {}",
                    abilityId, player.getName().getString());
        }
    }
}