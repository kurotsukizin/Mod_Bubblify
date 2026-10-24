package net.bubblify.mod.network;

import net.bubblify.mod.bubble.BubbleStyle;
import net.bubblify.mod.event.ServerChatEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Locale;
import java.util.UUID;
import java.util.function.Supplier;

public class UpdateColorPacket {

    private static final int MAX_STYLE_ID_LENGTH = 64;

    private final int color;
    private final boolean textoBranco;
    private final String styleId;

    public UpdateColorPacket(
            int color,
            boolean textoBranco,
            String styleId
    ) {
        this.color = color & 0xFFFFFF;
        this.textoBranco = textoBranco;
        this.styleId = sanitizeStyleId(styleId);
    }

    public UpdateColorPacket(FriendlyByteBuf buffer) {
        this.color = buffer.readInt() & 0xFFFFFF;
        this.textoBranco = buffer.readBoolean();
        this.styleId = sanitizeStyleId(
                buffer.readUtf(MAX_STYLE_ID_LENGTH)
        );
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(this.color);
        buffer.writeBoolean(this.textoBranco);
        buffer.writeUtf(
                this.styleId,
                MAX_STYLE_ID_LENGTH
        );
    }

    public void handle(
            Supplier<NetworkEvent.Context> supplier
    ) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

            String allowedStyleId = resolveAllowedStyle(
                    player,
                    this.styleId
            );

            ServerChatEvents.savePlayerPreferences(
                    player,
                    this.color,
                    this.textoBranco,
                    allowedStyleId
            );
        });

        context.setPacketHandled(true);
    }

    private static String resolveAllowedStyle(
            ServerPlayer player,
            String requestedStyleId
    ) {
        /*
         * Admin com nível 2 ou maior:
         * pode selecionar qualquer ID válido.
         */
        if (player.hasPermissions(2)) {
            return requestedStyleId;
        }

        String role = player.getPersistentData()
                .getString("bubblify_role")
                .trim()
                .toLowerCase(Locale.ROOT);

        /*
         * NPC pode selecionar:
         * - default
         * - estilos que começam por npc_
         */
        if ("npc".equals(role)) {
            if (BubbleStyle.DEFAULT.getId().equals(
                    requestedStyleId
            ) || requestedStyleId.startsWith("npc_")) {
                return requestedStyleId;
            }

            return BubbleStyle.DEFAULT.getId();
        }

        /*
         * Children pode selecionar:
         * - default
         * - estilos que começam por children_
         */
        if ("children".equals(role)) {
            if (BubbleStyle.DEFAULT.getId().equals(
                    requestedStyleId
            ) || requestedStyleId.startsWith("children_")) {
                return requestedStyleId;
            }

            return BubbleStyle.DEFAULT.getId();
        }

        /*
         * Sem role especial:
         * sempre usa a bolha colorida padrão.
         */
        return BubbleStyle.DEFAULT.getId();
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

        if (!normalizedId.matches("[a-z0-9_./-]+")
                || normalizedId.contains("..")
                || normalizedId.startsWith(".")
                || normalizedId.startsWith("/")) {
            return BubbleStyle.DEFAULT.getId();
        }

        return normalizedId;
    }
}