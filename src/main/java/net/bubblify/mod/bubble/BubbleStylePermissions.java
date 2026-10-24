package net.bubblify.mod.bubble;

import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BubbleStylePermissions {

    public static final String ROLE_KEY = "bubblify_role";

    private BubbleStylePermissions() {
    }

    public static boolean canUseStyle(
            ServerPlayer player,
            BubbleStyle style
    ) {
        if (player == null || style == null) {
            return false;
        }

        String role = player.getPersistentData()
                .getString(ROLE_KEY);

        return canUseStyle(style, role);
    }

    public static boolean canUseStyle(
            BubbleStyle style,
            String role
    ) {
        if (style == null || style.getCategory() == null) {
            return false;
        }

        String normalizedRole =
                role == null
                        ? ""
                        : role.trim().toLowerCase(Locale.ROOT);

        return switch (style.getCategory()) {
            case DEFAULT -> true;
            case ADMIN -> "admin".equals(normalizedRole);
            case NPC -> "npc".equals(normalizedRole);
            case CHILDREN -> "children".equals(normalizedRole);
        };
    }

    public static List<String> getAllowedStyleIds(
            ServerPlayer player
    ) {
        List<String> allowedStyleIds = new ArrayList<>();

        for (BubbleStyle style
                : BubbleStyleRegistry.getAllStyles()) {

            if (canUseStyle(player, style)) {
                allowedStyleIds.add(style.getId());
            }
        }

        return allowedStyleIds;
    }
}