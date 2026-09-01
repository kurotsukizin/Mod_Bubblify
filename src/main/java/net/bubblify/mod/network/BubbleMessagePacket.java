package net.bubblify.mod.network;

import net.bubblify.mod.event.ClientForgeEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class BubbleMessagePacket {

    private final UUID playerId;
    private final String message;
    private final int color;
    private final boolean textoBranco;

    public BubbleMessagePacket(
            UUID playerId,
            String message,
            int color,
            boolean textoBranco
    ) {
        this.playerId = playerId;
        this.message = message;
        this.color = color;
        this.textoBranco = textoBranco;
    }

    public BubbleMessagePacket(FriendlyByteBuf buffer) {
        this.playerId = buffer.readUUID();
        this.message = buffer.readUtf(256);
        this.color = buffer.readInt();
        this.textoBranco = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerId);
        buffer.writeUtf(message);
        buffer.writeInt(color);
        buffer.writeBoolean(textoBranco);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ClientForgeEvents.addBubble(
                    playerId,
                    message,
                    color,
                    textoBranco
            );
        });

        context.setPacketHandled(true);
    }
}