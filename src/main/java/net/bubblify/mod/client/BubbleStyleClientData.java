package net.bubblify.mod.client;

import net.bubblify.mod.bubble.BubbleStyle;
import net.bubblify.mod.bubble.BubbleStyleRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class BubbleStyleClientData {

    private static final String DEFAULT_STYLE_ID =
            BubbleStyle.DEFAULT.getId();

 
    private static final Set<String> ALLOWED_STYLE_IDS =
            new LinkedHashSet<>();

 
    private static boolean styleTabAllowed = false;

 
    private static BubbleStyle selectedStyle =
            BubbleStyle.DEFAULT;

    static {
        resetToDefault();
    }

    private BubbleStyleClientData() {
    }

    public static BubbleStyle getSelectedStyle() {
        if (selectedStyle == null
                || !isStyleAllowed(selectedStyle)) {
            selectedStyle = BubbleStyle.DEFAULT;
        }

        return selectedStyle;
    }

    public static void setSelectedStyle(
            BubbleStyle style
    ) {
        if (style == null
                || !isStyleAllowed(style)) {
            selectedStyle = BubbleStyle.DEFAULT;
            return;
        }

        selectedStyle = style;
    }

    public static boolean canUseStyleTab() {
        return styleTabAllowed;
    }

yle visível

    public static void setAllowedStyleIds(
            List<String> allowedStyleIds
    ) {
        boolean hasSpecialStyle = false;

        if (allowedStyleIds != null) {
            for (String styleId : allowedStyleIds) {
                String normalizedId =
                        normalizeStyleId(styleId);

                if (normalizedId == null) {
                    continue;
                }

                if (!DEFAULT_STYLE_ID.equals(normalizedId)) {
                    hasSpecialStyle = true;
                    break;
                }
            }
        }

        setServerPermissions(
                hasSpecialStyle,
                allowedStyleIds
        );
    }


    public static void setServerPermissions(
            boolean canUseStyleTab,
            List<String> allowedStyleIds
    ) {
        styleTabAllowed = canUseStyleTab;

        ALLOWED_STYLE_IDS.clear();


        ALLOWED_STYLE_IDS.add(DEFAULT_STYLE_ID);

        if (allowedStyleIds != null) {
            for (String styleId : allowedStyleIds) {
                String normalizedId =
                        normalizeStyleId(styleId);

                if (normalizedId == null) {
                    continue;
                }

        
                if (DEFAULT_STYLE_ID.equals(normalizedId)
                        || BubbleStyleRegistry.contains(
                        normalizedId
                )) {
                    ALLOWED_STYLE_IDS.add(normalizedId);
                }
            }
        }


        if (!styleTabAllowed) {
            selectedStyle = BubbleStyle.DEFAULT;
            return;
        }


        if (!isStyleAllowed(selectedStyle)) {
            selectedStyle = BubbleStyle.DEFAULT;
        }
    }


    public static void enableLocalStyleTest() {
        List<String> ids = new ArrayList<>();

        ids.add(DEFAULT_STYLE_ID);

        for (BubbleStyle style
                : BubbleStyleRegistry.getAllStyles()) {

            if (style == null) {
                continue;
            }

            ids.add(style.getId());
        }

        setServerPermissions(
                true,
                ids
        );
    }

    public static List<BubbleStyle> getAllowedStyles() {
        List<BubbleStyle> styles =
                new ArrayList<>();


        styles.add(BubbleStyle.DEFAULT);

 
        if (!styleTabAllowed) {
            return Collections.unmodifiableList(styles);
        }

        for (String styleId : ALLOWED_STYLE_IDS) {
            if (DEFAULT_STYLE_ID.equals(styleId)) {
                continue;
            }

            BubbleStyle style =
                    BubbleStyle.findRegistered(styleId);

            /*
             * Não exibe estilos que o cliente não conseguiu
             * descobrir/carregar como textura válida.
             */
            if (style == null || style.isDefault()) {
                continue;
            }

            styles.add(style);
        }

        return Collections.unmodifiableList(styles);
    }

    public static List<String> getAllowedStyleIds() {
        return Collections.unmodifiableList(
                new ArrayList<>(ALLOWED_STYLE_IDS)
        );
    }

    public static boolean isStyleAllowed(
            BubbleStyle style
    ) {
        if (style == null) {
            return false;
        }

        return isStyleAllowed(style.getId());
    }

    public static boolean isStyleAllowed(
            String styleId
    ) {
        String normalizedId =
                normalizeStyleId(styleId);

        if (normalizedId == null) {
            return false;
        }


        if (DEFAULT_STYLE_ID.equals(normalizedId)) {
            return true;
        }


        if (!styleTabAllowed) {
            return false;
        }


        return ALLOWED_STYLE_IDS.contains(normalizedId)
                && BubbleStyleRegistry.contains(normalizedId);
    }

    public static void resetToDefault() {
        styleTabAllowed = false;

        ALLOWED_STYLE_IDS.clear();
        ALLOWED_STYLE_IDS.add(DEFAULT_STYLE_ID);

        selectedStyle = BubbleStyle.DEFAULT;
    }

    private static String normalizeStyleId(
            String styleId
    ) {
        if (styleId == null || styleId.isBlank()) {
            return null;
        }

        String normalizedId = styleId
                .trim()
                .toLowerCase(Locale.ROOT);

        if (normalizedId.length() > 64) {
            return null;
        }

        if (!normalizedId.matches("[a-z0-9_./-]+")) {
            return null;
        }

        if (normalizedId.contains("..")
                || normalizedId.startsWith(".")
                || normalizedId.startsWith("/")) {
            return null;
        }

        return normalizedId;
    }
}
