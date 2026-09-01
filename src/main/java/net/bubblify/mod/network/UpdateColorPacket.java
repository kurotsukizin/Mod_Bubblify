package net.bubblify.mod.network;

import net.bubblify.mod.event.ServerChatEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateColorPacket {

    private final int color;
    private final boolean textoBranco;

    public UpdateColorPacket(int color, boolean textoBranco) {
        this.color = color;
        this.textoBranco = textoBranco;
    }

    public UpdateColorPacket(FriendlyByteBuf buffer) {
        this.color = buffer.readInt();
        this.textoBranco = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(color);
        buffer.writeBoolean(textoBranco);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

            int sanitizedColor = color & 0xFFFFFF;

            ServerChatEvents.PLAYER_COLORS.put(
                    player.getUUID(),
                    sanitizedColor
            );

            ServerChatEvents.PLAYER_TEXT_COLORS.put(
                    player.getUUID(),
                    textoBranco
            );

            player.getPersistentData().putInt(
                    "bubblify_chat_color",
                    sanitizedColor
            );

            player.getPersistentData().putBoolean(
                    "bubblify_text_white",
                    textoBranco
            );
        });

        context.setPacketHandled(true);
    }
}