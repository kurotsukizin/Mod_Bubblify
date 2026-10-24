package net.bubblify.mod.network;

import net.bubblify.mod.bubble.BubbleStyle;
import net.bubblify.mod.event.ClientForgeEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Locale;
import java.util.UUID;
import java.util.function.Supplier;

public class BubbleMessagePacket {

    private static final int MAX_MESSAGE_LENGTH = 256;
    private static final int MAX_STYLE_ID_LENGTH = 64;

    private final UUID playerId;
    private final String message;
    private final int color;
    private final boolean textoBranco;
    private final String styleId;

    public BubbleMessagePacket(
            UUID playerId,
            String message,
            int color,
            boolean textoBranco,
            String styleId
    ) {
        this.playerId = playerId;
        this.message = sanitizeMessage(message);
        this.color = color & 0xFFFFFF;
        this.textoBranco = textoBranco;
        this.styleId = sanitizeStyleId(styleId);
    }

    public BubbleMessagePacket(FriendlyByteBuf buffer) {
        this.playerId = buffer.readUUID();

        this.message = sanitizeMessage(
                buffer.readUtf(MAX_MESSAGE_LENGTH)
        );

        this.color = buffer.readInt() & 0xFFFFFF;

        this.textoBranco = buffer.readBoolean();

        this.styleId = sanitizeStyleId(
                buffer.readUtf(MAX_STYLE_ID_LENGTH)
        );
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.playerId);
        buffer.writeUtf(this.message, MAX_MESSAGE_LENGTH);
        buffer.writeInt(this.color);
        buffer.writeBoolean(this.textoBranco);
        buffer.writeUtf(this.styleId, MAX_STYLE_ID_LENGTH);
    }

    public void handle(
            Supplier<NetworkEvent.Context> supplier
    ) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(
                        Dist.CLIENT,
                        () -> () -> ClientForgeEvents.addBubble(
                                this.playerId,
                                this.message,
                                this.color,
                                this.textoBranco,
                                this.styleId
                        )
                )
        );

        context.setPacketHandled(true);
    }

    private static String sanitizeMessage(
            String message
    ) {
        if (message == null) {
            return "";
        }

        if (message.length() > MAX_MESSAGE_LENGTH) {
            return message.substring(
                    0,
                    MAX_MESSAGE_LENGTH
            );
        }

        return message;
    }

    private static String sanitizeStyleId(
            String styleId
    ) {
        if (styleId == null || styleId.isBlank()) {
            return BubbleStyle.DEFAULT.getId();
        }

        String normalizedId = styleId
                .trim()
                .toLowerCase(Locale.ROOT);

        if (normalizedId.length() > MAX_STYLE_ID_LENGTH) {
            return BubbleStyle.DEFAULT.getId();
        }

        /*
         * O servidor não deve exigir que o ID exista no registry
         * do cliente. Ele apenas transporta um ID seguro.
         *
         * A confirmação de que o PNG existe ocorre no cliente,
         * em BubbleStyleRegistry.getOrDefault(styleId).
         */
        if (!normalizedId.matches("[a-z0-9_./-]+")
                || normalizedId.contains("..")
                || normalizedId.startsWith(".")
                || normalizedId.startsWith("/")) {
            return BubbleStyle.DEFAULT.getId();
        }

        return normalizedId;
    }
}