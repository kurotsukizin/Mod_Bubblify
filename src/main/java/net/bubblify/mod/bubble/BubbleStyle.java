package net.bubblify.mod.bubble;

import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public final class BubbleStyle {

    private static final String DEFAULT_ID = "default";

    public enum Category {
        DEFAULT,
        ADMIN,
        NPC,
        CHILDREN
    }

    public static final BubbleStyle DEFAULT =
            new BubbleStyle(
                    "default",
                    "Padrão",
                    Category.DEFAULT,
                    true,
                    null,
                    null,
                    null,
                    128.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    0.0F,
                    1.45F
            );

    private final String id;
    private final String displayName;
    private final Category category;
    private final boolean defaultStyle;

    private final ResourceLocation topTexture;
    private final ResourceLocation bodyTexture;
    private final ResourceLocation tailTexture;

    private final float sourceWidth;

    private final float topWidth;
    private final float topHeight;
    private final float topAlign;
    private final float topOffsetX;

    private final float bodyHeight;

    private final float tailWidth;
    private final float tailHeight;

    private final float leftSlice;
    private final float rightSlice;

    private final float paddingLeft;
    private final float paddingRight;
    private final float paddingTop;
    private final float paddingBottom;

    private final float heightOffset;

    public BubbleStyle(
            String id,
            String displayName,
            Category category,
            boolean defaultStyle,
            ResourceLocation topTexture,
            ResourceLocation bodyTexture,
            ResourceLocation tailTexture,
            float sourceWidth,
            float topHeight,
            float bodyHeight,
            float tailWidth,
            float tailHeight,
            float leftSlice,
            float rightSlice,
            float paddingLeft,
            float paddingRight,
            float paddingTop,
            float paddingBottom,
            float heightOffset
    ) {
        this(
                id,
                displayName,
                category,
                defaultStyle,
                topTexture,
                bodyTexture,
                tailTexture,
                sourceWidth,
                topHeight,
                bodyHeight,
                tailWidth,
                tailHeight,
                leftSlice,
                rightSlice,
                paddingLeft,
                paddingRight,
                paddingTop,
                paddingBottom,
                heightOffset,
                80.0F,
                1.0F,
                0.0F
        );
    }

    public BubbleStyle(
            String id,
            String displayName,
            Category category,
            boolean defaultStyle,
            ResourceLocation topTexture,
            ResourceLocation bodyTexture,
            ResourceLocation tailTexture,
            float sourceWidth,
            float topHeight,
            float bodyHeight,
            float tailWidth,
            float tailHeight,
            float leftSlice,
            float rightSlice,
            float paddingLeft,
            float paddingRight,
            float paddingTop,
            float paddingBottom,
            float heightOffset,
            float topWidth,
            float topAlign,
            float topOffsetX
    ) {
        this.id = normalizeId(id);

        this.displayName =
                displayName == null || displayName.isBlank()
                        ? this.id
                        : displayName;

        this.category =
                category == null
                        ? Category.DEFAULT
                        : category;

        this.defaultStyle = defaultStyle;

        this.topTexture = topTexture;
        this.bodyTexture = bodyTexture;
        this.tailTexture = tailTexture;

        this.sourceWidth = Math.max(
                1.0F,
                sourceWidth
        );

        this.topWidth = Float.isFinite(topWidth)
                ? Math.max(1.0F, topWidth)
                : 80.0F;

        this.topHeight = Math.max(
                0.0F,
                topHeight
        );

        this.topAlign = Float.isFinite(topAlign)
                ? Math.max(0.0F, Math.min(1.0F, topAlign))
                : 1.0F;

        this.topOffsetX = Float.isFinite(topOffsetX)
                ? topOffsetX
                : 0.0F;

        this.bodyHeight = Math.max(
                0.0F,
                bodyHeight
        );

        this.tailWidth = Math.max(
                0.0F,
                tailWidth
        );

        this.tailHeight = Math.max(
                0.0F,
                tailHeight
        );

        this.leftSlice = Math.max(
                0.0F,
                leftSlice
        );

        this.rightSlice = Math.max(
                0.0F,
                rightSlice
        );

        this.paddingLeft = Math.max(
                0.0F,
                paddingLeft
        );

        this.paddingRight = Math.max(
                0.0F,
                paddingRight
        );

        this.paddingTop = Math.max(
                0.0F,
                paddingTop
        );

        this.paddingBottom = Math.max(
                0.0F,
                paddingBottom
        );

        this.heightOffset = heightOffset;
    }

    public BubbleStyle(
            String id,
            String displayName,
            Category category,
            ResourceLocation texture
    ) {
        this(
                id,
                displayName,
                category,
                "default".equalsIgnoreCase(id),
                null,
                texture,
                null,
                128.0F,
                0.0F,
                32.0F,
                0.0F,
                0.0F,
                16.0F,
                16.0F,
                16.0F,
                16.0F,
                7.0F,
                7.0F,
                2.10F
        );
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isDefault() {
        return defaultStyle;
    }

    public ResourceLocation getTopTexture() {
        return topTexture;
    }

    public ResourceLocation getBodyTexture() {
        return bodyTexture;
    }

    public ResourceLocation getTailTexture() {
        return tailTexture;
    }

    public ResourceLocation getTexture() {
        return bodyTexture;
    }

    public float getSourceWidth() {
        return sourceWidth;
    }

    public float getTopWidth() {
        return topWidth;
    }

    public float getTopHeight() {
        return topHeight;
    }

    public float getTopAlign() {
        return topAlign;
    }

    public float getTopOffsetX() {
        return topOffsetX;
    }

    public float getBodyHeight() {
        return bodyHeight;
    }

    public float getTailWidth() {
        return tailWidth;
    }

    public float getTailHeight() {
        return tailHeight;
    }

    public float getLeftSlice() {
        return leftSlice;
    }

    public float getRightSlice() {
        return rightSlice;
    }

    public float getPaddingLeft() {
        return paddingLeft;
    }

    public float getPaddingRight() {
        return paddingRight;
    }

    public float getPaddingTop() {
        return paddingTop;
    }

    public float getPaddingBottom() {
        return paddingBottom;
    }

    public float getHeightOffset() {
        return heightOffset;
    }

    public boolean hasThreePartTexture() {
        return !defaultStyle
                && topTexture != null
                && bodyTexture != null
                && tailTexture != null;
    }

    public static BubbleStyle findRegistered(String styleId) {
        return BubbleStyleRegistry.getOrDefault(styleId);
    }

    public static String normalizeId(String id) {
        if (id == null || id.isBlank()) {
            return DEFAULT_ID;
        }

        return id.trim()
                .toLowerCase(Locale.ROOT)
                .replace(' ', '_');
    }

    @Override
    public String toString() {
        return "BubbleStyle{"
                + "id='"
                + id
                + '\''
                + ", displayName='"
                + displayName
                + '\''
                + ", category="
                + category
                + ", defaultStyle="
                + defaultStyle
                + ", topWidth="
                + topWidth
                + ", topAlign="
                + topAlign
                + ", topOffsetX="
                + topOffsetX
                + '}';
    }
}