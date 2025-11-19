package hs.elementmod.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class AbilityPacket {
    private final int abilityId;
    public static final Identifier ID = Identifier.of("elementmod", "ability_packet");

    public AbilityPacket(int abilityId) {
        this.abilityId = abilityId;
    }

    public int getAbilityId() {
        return abilityId;
    }

    /** Send this packet from client to server */
    public static void send(int abilityId) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(abilityId);
        ClientPlayNetworking.send(ID, buf); // ✅ now works
    }

    /** Read packet from server buffer */
    public static AbilityPacket fromBuf(PacketByteBuf buf) {
        return new AbilityPacket(buf.readInt());
    }

    /** Write packet to buffer */
    public void toBuf(PacketByteBuf buf) {
        buf.writeInt(abilityId);
    }
}
