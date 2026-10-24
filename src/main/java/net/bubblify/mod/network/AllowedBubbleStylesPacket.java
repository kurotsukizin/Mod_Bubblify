package net.bubblify.mod.network;

import net.bubblify.mod.client.BubbleStyleClientData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AllowedBubbleStylesPacket {

    private static final int MAX_STYLES = 64;
    private static final int MAX_STYLE_ID_LENGTH = 64;

    private final List<String> allowedStyleIds;

    public AllowedBubbleStylesPacket(
            List<String> allowedStyleIds
    ) {
        this.allowedStyleIds = sanitizeStyleIds(allowedStyleIds);
    }

    public AllowedBubbleStylesPacket(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();

        if (size < 0 || size > MAX_STYLES) {
            throw new IllegalArgumentException(
                    "Quantidade inválida de estilos: " + size
            );
        }

        List<String> readIds = new ArrayList<>();

        for (int index = 0; index < size; index++) {
            readIds.add(
                    buffer.readUtf(MAX_STYLE_ID_LENGTH)
            );
        }

        this.allowedStyleIds = sanitizeStyleIds(readIds);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.allowedStyleIds.size());

        for (String styleId : this.allowedStyleIds) {
            buffer.writeUtf(styleId, MAX_STYLE_ID_LENGTH);
        }
    }

    public void handle(
            Supplier<NetworkEvent.Context> supplier
    ) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(
                        Dist.CLIENT,
                        () -> () -> BubbleStyleClientData
                                .setAllowedStyleIds(
                                        this.allowedStyleIds
                                )
                )
        );

        context.setPacketHandled(true);
    }

    private static List<String> sanitizeStyleIds(
            List<String> styleIds
    ) {
        List<String> safeIds = new ArrayList<>();

        if (styleIds == null) {
            return safeIds;
        }

        for (String styleId : styleIds) {
            if (styleId == null || styleId.isBlank()) {
                continue;
            }

            String trimmedId = styleId.trim();

            if (trimmedId.length() > MAX_STYLE_ID_LENGTH) {
                continue;
            }

            if (!safeIds.contains(trimmedId)) {
                safeIds.add(trimmedId);
            }
        }

        return safeIds;
    }
}