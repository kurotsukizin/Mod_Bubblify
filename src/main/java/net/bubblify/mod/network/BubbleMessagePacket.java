package net.bubblify.mod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.UUID;
import java.util.function.Supplier;

public class BubbleMessagePacket {
    private final UUID playerId;
    private final String message;
    private final int color;

    public BubbleMessagePacket(UUID playerId, String message, int color) {
        this.playerId = playerId;
        this.message = message;
        this.color = color;
    }

    public BubbleMessagePacket(FriendlyByteBuf buffer) {
        this.playerId = buffer.readUUID();
        this.message = buffer.readUtf(256);
        this.color = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerId);
        buffer.writeUtf(message);
        buffer.writeInt(color);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            java.util.List<net.bubblify.mod.event.ClientForgeEvents.ChatBubble> list =
                    net.bubblify.mod.event.ClientForgeEvents.ACTIVE_BUBBLES.computeIfAbsent(playerId, k -> new java.util.ArrayList<>());

            list.add(new net.bubblify.mod.event.ClientForgeEvents.ChatBubble(message, color));

            if (list.size() > 3) {
                list.remove(0);
            }
        });
        context.setPacketHandled(true);
    }
}