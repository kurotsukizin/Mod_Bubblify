package net.bubblify.mod.network;

import net.bubblify.mod.event.ServerChatEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateColorPacket {
    private final int color;

    public UpdateColorPacket(int color) {
        this.color = color;
    }

    public UpdateColorPacket(FriendlyByteBuf buffer) {
        this.color = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(color);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                int sanitizedColor = color & 0xFFFFFF;

                ServerChatEvents.PLAYER_COLORS.put(player.getUUID(), sanitizedColor);

                player.getPersistentData().putInt("bubblify_chat_color", sanitizedColor);
            }
        });
        context.setPacketHandled(true);
    }
}