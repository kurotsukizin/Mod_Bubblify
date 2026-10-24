package net.bubblify.mod.event;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.bubblify.mod.BubblifyMod;
import net.bubblify.mod.bubble.BubbleStyle;
import net.bubblify.mod.bubble.BubbleStyleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(
        modid = BubblifyMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class ClientForgeEvents {

    private static final int BUBBLE_LIFETIME_TICKS = 300;
    private static final int MAX_BUBBLES_PER_PLAYER = 3;

    private static final int FADE_IN_TICKS = 10;
    private static final int FADE_OUT_TICKS = 30;

    private static final int MAX_TEXT_WIDTH = 150;

    private static final float DEFAULT_BUBBLE_SCALE = 0.025F;
    private static final float TEXTURED_BUBBLE_SCALE = 0.016F;

    private static final float TEXTURED_HEIGHT_OFFSET = 1.50F;
    private static final float DEFAULT_HEIGHT_OFFSET = 0.75F;

    private static final float DEFAULT_PADDING_X = 5.0F;
    private static final float DEFAULT_PADDING_Y = 2.0F;
    private static final float DEFAULT_MIN_WIDTH = 12.0F;

    private static final float TOP_WIDTH = 80.0F;
    private static final float TOP_HEIGHT = 16.0F;

    private static final float TAIL_WIDTH = 16.0F;
    private static final float TAIL_HEIGHT = 8.0F;

    private static final float TOP_INSET = 0.0F;
    private static final float TAIL_INSET = 0.0F;

    public static final Map<UUID, List<ChatBubble>> ACTIVE_BUBBLES =
            new HashMap<>();

    private static final Map<ResourceLocation, TextureCrop> CROP_CACHE =
            new HashMap<>();

    public static class ChatBubble {

        public final String text;
        public final int color;
        public final boolean textoBranco;
        public final BubbleStyle style;

        public int ticksRemaining;
        public float currentYOffset;

        public ChatBubble(
                String text,
                int color,
                boolean textoBranco,
                String styleId
        ) {
            this.text = text;
            this.color = color & 0xFFFFFF;
            this.textoBranco = textoBranco;
            this.style = BubbleStyleRegistry.getOrDefault(styleId);
            this.ticksRemaining = BUBBLE_LIFETIME_TICKS;
            this.currentYOffset = 0.0F;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (Minecraft.getInstance().level == null) {
            ACTIVE_BUBBLES.clear();
            CROP_CACHE.clear();
            return;
        }

        Iterator<Map.Entry<UUID, List<ChatBubble>>> iterator =
                ACTIVE_BUBBLES.entrySet().iterator();

        while (iterator.hasNext()) {
            List<ChatBubble> bubbles = iterator.next().getValue();

            bubbles.removeIf(
                    bubble -> --bubble.ticksRemaining <= 0
            );

            if (bubbles.isEmpty()) {
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        List<ChatBubble> bubbles =
                ACTIVE_BUBBLES.get(player.getUUID());

        if (bubbles == null || bubbles.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        ChatBubble newest = bubbles.get(bubbles.size() - 1);

        BubbleStyle style = newest.style == null
                ? BubbleStyle.DEFAULT
                : newest.style;

        boolean defaultStyle =
                style.isDefault() || style.getBodyTexture() == null;

        float paddingLeft = defaultStyle
                ? DEFAULT_PADDING_X
                : style.getPaddingLeft();

        float paddingRight = defaultStyle
                ? DEFAULT_PADDING_X
                : style.getPaddingRight();

        float paddingTop = defaultStyle
                ? DEFAULT_PADDING_Y
                : style.getPaddingTop();

        float paddingBottom = defaultStyle
                ? DEFAULT_PADDING_Y
                : style.getPaddingBottom();

        float minimumWidth = defaultStyle
                ? DEFAULT_MIN_WIDTH
                : Math.max(1.0F, style.getSourceWidth());

        float minimumHeight = defaultStyle
                ? font.lineHeight + DEFAULT_PADDING_Y * 2.0F
                : Math.max(1.0F, style.getBodyHeight());

        List<StackPanel> panels = new ArrayList<>();

        float sharedWidth = minimumWidth;

        int firstIndex = Math.max(
                0,
                bubbles.size() - MAX_BUBBLES_PER_PLAYER
        );

        for (int index = firstIndex; index < bubbles.size(); index++) {
            ChatBubble bubble = bubbles.get(index);

            List<FormattedCharSequence> lines = font.split(
                    Component.literal(bubble.text),
                    MAX_TEXT_WIDTH
            );

            if (lines.isEmpty()) {
                continue;
            }

            int widestLine = 0;

            for (FormattedCharSequence line : lines) {
                widestLine = Math.max(
                        widestLine,
                        font.width(line)
                );
            }

            float textHeight = lines.size() * font.lineHeight;

            float width = Math.max(
                    minimumWidth,
                    widestLine + paddingLeft + paddingRight
            );

            float height = Math.max(
                    minimumHeight,
                    textHeight + paddingTop + paddingBottom
            );

            float textStartY = paddingTop + Math.max(
                    0.0F,
                    (
                            height
                                    - paddingTop
                                    - paddingBottom
                                    - textHeight
                    ) / 2.0F
            );

            sharedWidth = Math.max(sharedWidth, width);

            panels.add(
                    new StackPanel(
                            bubble,
                            lines,
                            width,
                            height,
                            textStartY
                    )
            );
        }

        if (panels.isEmpty()) {
            return;
        }

        if (!defaultStyle) {
            for (StackPanel panel : panels) {
                panel.width = sharedWidth;
            }
        }

        float bottomAnchor = defaultStyle
                ? font.lineHeight + DEFAULT_PADDING_Y * 2.0F
                : 16.0F + minimumHeight;

        float nextBottom = bottomAnchor;

        for (int index = panels.size() - 1; index >= 0; index--) {
            StackPanel panel = panels.get(index);

            panel.y = nextBottom - panel.height;
            nextBottom = panel.y;
        }

        StackPanel firstPanel = panels.get(0);
        StackPanel lastPanel = panels.get(panels.size() - 1);

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffers = event.getMultiBufferSource();

        int light = LightTexture.FULL_BRIGHT;

        poseStack.pushPose();

        try {
            poseStack.translate(
                    0.0D,
                    player.getBbHeight()
                            + (defaultStyle
                            ? DEFAULT_HEIGHT_OFFSET
                            : TEXTURED_HEIGHT_OFFSET),
                    0.0D
            );

            poseStack.mulPose(
                    minecraft.getEntityRenderDispatcher()
                            .cameraOrientation()
            );

            float bubbleScale = defaultStyle
                    ? DEFAULT_BUBBLE_SCALE
                    : TEXTURED_BUBBLE_SCALE;

            poseStack.scale(
                    -bubbleScale,
                    -bubbleScale,
                    bubbleScale
            );

            Matrix4f matrix = new Matrix4f(poseStack.last().pose());

            if (defaultStyle) {
                drawDefaultPanels(matrix, panels);
            } else {
                float panelX = -sharedWidth / 2.0F;

                drawWholeTexture(
                        buffers,
                        matrix,
                        style.getTopTexture(),
                        panelX + sharedWidth - TOP_WIDTH,
                        firstPanel.y - TOP_HEIGHT + TOP_INSET,
                        TOP_WIDTH,
                        TOP_HEIGHT,
                        getAlphaMultiplier(firstPanel.bubble),
                        light
                );

                drawWholeTexture(
                        buffers,
                        matrix,
                        style.getTailTexture(),
                        -TAIL_WIDTH / 2.0F,
                        lastPanel.y + lastPanel.height - TAIL_INSET,
                        TAIL_WIDTH,
                        TAIL_HEIGHT,
                        getAlphaMultiplier(lastPanel.bubble),
                        light
                );

                for (StackPanel panel : panels) {
                    float alpha = getAlphaMultiplier(panel.bubble);

                    if (alpha <= 0.0F) {
                        continue;
                    }

                    drawPanelTexture(
                            buffers,
                            matrix,
                            style,
                            -panel.width / 2.0F,
                            panel.y,
                            panel.width,
                            panel.height,
                            alpha,
                            light
                    );
                }
            }

            poseStack.pushPose();

            try {
                poseStack.translate(0.0D, 0.0D, -1.0D);

                Matrix4f textMatrix =
                        new Matrix4f(poseStack.last().pose());

                float textCenterX = defaultStyle
                        ? 0.0F
                        : (paddingLeft - paddingRight) / 2.0F;

                for (StackPanel panel : panels) {
                    float alpha = getAlphaMultiplier(panel.bubble);

                    if (alpha <= 0.0F) {
                        continue;
                    }

                    int textColor = applyAlpha(
                            panel.bubble.textoBranco
                                    ? 0xFFFFFFFF
                                    : 0xFF000000,
                            alpha
                    );

                    for (int lineIndex = 0;
                         lineIndex < panel.lines.size();
                         lineIndex++) {

                        FormattedCharSequence line =
                                panel.lines.get(lineIndex);

                        float textX =
                                textCenterX - font.width(line) / 2.0F;

                        float textY =
                                panel.y
                                        + panel.textStartY
                                        + lineIndex * font.lineHeight;

                        font.drawInBatch(
                                line,
                                textX,
                                textY,
                                textColor,
                                false,
                                textMatrix,
                                buffers,
                                Font.DisplayMode.NORMAL,
                                0,
                                light
                        );
                    }
                }
            } finally {
                poseStack.popPose();
            }
        } finally {
            poseStack.popPose();
        }
    }

    private static void drawDefaultPanels(
            Matrix4f matrix,
            List<StackPanel> panels
    ) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(
                GameRenderer::getPositionColorShader
        );

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();

        builder.begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR
        );

        for (int index = 0; index < panels.size(); index++) {
            StackPanel panel = panels.get(index);

            float alpha = getAlphaMultiplier(panel.bubble);

            if (alpha <= 0.0F) {
                continue;
            }

            int backgroundColor = applyAlpha(
                    0xE6000000 | panel.bubble.color,
                    alpha
            );

            int borderColor = applyAlpha(
                    0xFF000000,
                    alpha
            );

            boolean drawTail = index == panels.size() - 1;

            drawRoundedBalloon(
                    builder,
                    matrix,
                    -panel.width / 2.0F,
                    panel.y,
                    panel.width,
                    panel.height,
                    backgroundColor,
                    borderColor,
                    drawTail
            );
        }

        tesselator.end();
    }

    private static void drawRoundedBalloon(
            BufferBuilder builder,
            Matrix4f matrix,
            float x,
            float y,
            float width,
            float height,
            int backgroundColor,
            int borderColor,
            boolean drawTail
    ) {
        float right = x + width;
        float bottom = y + height;

        float corner = Math.min(
                2.0F,
                Math.min(width / 2.0F, height / 2.0F)
        );

        addColorQuad(
                builder, matrix,
                x + corner, y,
                right - corner, y + 1.0F,
                borderColor
        );

        addColorQuad(
                builder, matrix,
                x + 1.0F, y + 1.0F,
                x + corner, y + corner,
                borderColor
        );

        addColorQuad(
                builder, matrix,
                right - corner, y + 1.0F,
                right - 1.0F, y + corner,
                borderColor
        );

        addColorQuad(
                builder, matrix,
                x, y + corner,
                x + 1.0F, bottom - corner,
                borderColor
        );

        addColorQuad(
                builder, matrix,
                right - 1.0F, y + corner,
                right, bottom - corner,
                borderColor
        );

        addColorQuad(
                builder, matrix,
                x + 1.0F, bottom - corner,
                x + corner, bottom - 1.0F,
                borderColor
        );

        addColorQuad(
                builder, matrix,
                right - corner, bottom - corner,
                right - 1.0F, bottom - 1.0F,
                borderColor
        );

        float centerX = x + width / 2.0F;

        if (drawTail) {
            addColorQuad(
                    builder, matrix,
                    x + corner, bottom - 1.0F,
                    centerX - 3.0F, bottom,
                    borderColor
            );

            addColorQuad(
                    builder, matrix,
                    centerX + 3.0F, bottom - 1.0F,
                    right - corner, bottom,
                    borderColor
            );
        } else {
            addColorQuad(
                    builder, matrix,
                    x + corner, bottom - 1.0F,
                    right - corner, bottom,
                    borderColor
            );
        }

        addColorQuad(
                builder, matrix,
                x + corner, y + 1.0F,
                right - corner, y + corner,
                backgroundColor
        );

        addColorQuad(
                builder, matrix,
                x + 1.0F, y + corner,
                right - 1.0F, bottom - corner,
                backgroundColor
        );

        addColorQuad(
                builder, matrix,
                x + corner, bottom - corner,
                right - corner, bottom - 1.0F,
                backgroundColor
        );

        if (drawTail) {
            addColorQuad(
                    builder, matrix,
                    centerX - 3.0F, bottom - 1.0F,
                    centerX + 3.0F, bottom,
                    backgroundColor
            );

            addColorQuad(
                    builder, matrix,
                    centerX - 3.0F, bottom,
                    centerX - 2.0F, bottom + 2.0F,
                    borderColor
            );

            addColorQuad(
                    builder, matrix,
                    centerX + 2.0F, bottom,
                    centerX + 3.0F, bottom + 2.0F,
                    borderColor
            );

            addColorQuad(
                    builder, matrix,
                    centerX - 2.0F, bottom + 2.0F,
                    centerX - 1.0F, bottom + 3.0F,
                    borderColor
            );

            addColorQuad(
                    builder, matrix,
                    centerX + 1.0F, bottom + 2.0F,
                    centerX + 2.0F, bottom + 3.0F,
                    borderColor
            );

            addColorQuad(
                    builder, matrix,
                    centerX - 1.0F, bottom + 3.0F,
                    centerX + 1.0F, bottom + 4.0F,
                    borderColor
            );

            addColorQuad(
                    builder, matrix,
                    centerX - 2.0F, bottom,
                    centerX + 2.0F, bottom + 2.0F,
                    backgroundColor
            );

            addColorQuad(
                    builder, matrix,
                    centerX - 1.0F, bottom + 2.0F,
                    centerX + 1.0F, bottom + 3.0F,
                    backgroundColor
            );
        }
    }

    private static void addColorQuad(
            BufferBuilder builder,
            Matrix4f matrix,
            float minX,
            float minY,
            float maxX,
            float maxY,
            int color
    ) {
        if (maxX <= minX || maxY <= minY) {
            return;
        }

        int alpha = (color >>> 24) & 0xFF;
        int red = (color >>> 16) & 0xFF;
        int green = (color >>> 8) & 0xFF;
        int blue = color & 0xFF;

        builder.vertex(matrix, minX, maxY, 0.0F)
                .color(red, green, blue, alpha)
                .endVertex();

        builder.vertex(matrix, maxX, maxY, 0.0F)
                .color(red, green, blue, alpha)
                .endVertex();

        builder.vertex(matrix, maxX, minY, 0.0F)
                .color(red, green, blue, alpha)
                .endVertex();

        builder.vertex(matrix, minX, minY, 0.0F)
                .color(red, green, blue, alpha)
                .endVertex();
    }

    private static void drawPanelTexture(
            MultiBufferSource buffers,
            Matrix4f matrix,
            BubbleStyle style,
            float x,
            float y,
            float width,
            float height,
            float alpha,
            int light
    ) {
        ResourceLocation texture = style.getBodyTexture();

        if (texture == null || alpha <= 0.0F) {
            return;
        }

        TextureCrop crop = getTextureCrop(texture);

        float sourceWidth = Math.max(
                1.0F,
                style.getSourceWidth()
        );

        float left = Math.max(
                0.0F,
                style.getLeftSlice()
        );

        float right = Math.max(
                0.0F,
                style.getRightSlice()
        );

        if (left + right >= sourceWidth
                || left + right > width) {

            drawWholeTexture(
                    buffers,
                    matrix,
                    texture,
                    x,
                    y,
                    width,
                    height,
                    alpha,
                    light
            );

            return;
        }

        float u1 = left / sourceWidth;
        float u2 = 1.0F - right / sourceWidth;

        VertexConsumer buffer = buffers.getBuffer(
                RenderType.text(texture)
        );

        int opacity = Math.round(255.0F * alpha);

        addTexturedQuad(
                buffer, matrix,
                x, y,
                x + left, y + height,
                0.0F, crop.minV,
                u1, crop.maxV,
                opacity, light
        );

        addTexturedQuad(
                buffer, matrix,
                x + left, y,
                x + width - right, y + height,
                u1, crop.minV,
                u2, crop.maxV,
                opacity, light
        );

        addTexturedQuad(
                buffer, matrix,
                x + width - right, y,
                x + width, y + height,
                u2, crop.minV,
                1.0F, crop.maxV,
                opacity, light
        );
    }

    private static void drawWholeTexture(
            MultiBufferSource buffers,
            Matrix4f matrix,
            ResourceLocation texture,
            float x,
            float y,
            float width,
            float height,
            float alpha,
            int light
    ) {
        if (texture == null || alpha <= 0.0F) {
            return;
        }

        TextureCrop crop = getTextureCrop(texture);

        VertexConsumer buffer = buffers.getBuffer(
                RenderType.text(texture)
        );

        addTexturedQuad(
                buffer, matrix,
                x, y,
                x + width, y + height,
                0.0F, crop.minV,
                1.0F, crop.maxV,
                Math.round(255.0F * alpha),
                light
        );
    }

    private static void addTexturedQuad(
            VertexConsumer buffer,
            Matrix4f matrix,
            float minX,
            float minY,
            float maxX,
            float maxY,
            float minU,
            float minV,
            float maxU,
            float maxV,
            int alpha,
            int light
    ) {
        if (maxX <= minX || maxY <= minY) {
            return;
        }

        buffer.vertex(matrix, minX, maxY, 0.0F)
                .color(255, 255, 255, alpha)
                .uv(minU, maxV)
                .uv2(light)
                .endVertex();

        buffer.vertex(matrix, maxX, maxY, 0.0F)
                .color(255, 255, 255, alpha)
                .uv(maxU, maxV)
                .uv2(light)
                .endVertex();

        buffer.vertex(matrix, maxX, minY, 0.0F)
                .color(255, 255, 255, alpha)
                .uv(maxU, minV)
                .uv2(light)
                .endVertex();

        buffer.vertex(matrix, minX, minY, 0.0F)
                .color(255, 255, 255, alpha)
                .uv(minU, minV)
                .uv2(light)
                .endVertex();
    }

    private static TextureCrop getTextureCrop(
            ResourceLocation texture
    ) {
        TextureCrop cached = CROP_CACHE.get(texture);

        if (cached != null) {
            return cached;
        }

        TextureCrop result = TextureCrop.FULL;

        try {
            var resource = Minecraft.getInstance()
                    .getResourceManager()
                    .getResource(texture);

            if (resource.isPresent()) {
                try (
                        InputStream stream = resource.get().open();
                        NativeImage image = NativeImage.read(stream)
                ) {
                    int width = image.getWidth();
                    int height = image.getHeight();

                    int firstVisible = height;
                    int lastVisible = -1;

                    for (int y = 0; y < height; y++) {
                        boolean visible = false;

                        for (int x = 0; x < width; x++) {
                            int pixel = image.getPixelRGBA(x, y);
                            int alpha = (pixel >>> 24) & 0xFF;

                            if (alpha > 0) {
                                visible = true;
                                break;
                            }
                        }

                        if (visible) {
                            firstVisible = Math.min(
                                    firstVisible,
                                    y
                            );

                            lastVisible = y;
                        }
                    }

                    if (lastVisible >= firstVisible) {
                        result = new TextureCrop(
                                firstVisible / (float) height,
                                (lastVisible + 1) / (float) height
                        );
                    }
                }
            }
        } catch (Exception exception) {
            BubblifyMod.LOGGER.warn(
                    "Não foi possível calcular o recorte de {}.",
                    texture,
                    exception
            );
        }

        CROP_CACHE.put(texture, result);

        return result;
    }

    public static void clearTextureCropCache() {
        CROP_CACHE.clear();
    }

    private static float getAlphaMultiplier(ChatBubble bubble) {
        int elapsed =
                BUBBLE_LIFETIME_TICKS - bubble.ticksRemaining;

        float fadeIn = Math.min(
                1.0F,
                elapsed / (float) FADE_IN_TICKS
        );

        float fadeOut = Math.min(
                1.0F,
                bubble.ticksRemaining / (float) FADE_OUT_TICKS
        );

        return Math.max(
                0.0F,
                Math.min(fadeIn, fadeOut)
        );
    }

    private static int applyAlpha(
            int color,
            float multiplier
    ) {
        int originalAlpha = (color >>> 24) & 0xFF;

        int alpha = Math.round(
                originalAlpha * Math.max(
                        0.0F,
                        Math.min(1.0F, multiplier)
                )
        );

        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    public static void addBubble(
            UUID playerId,
            String message,
            int color,
            boolean textoBranco,
            String styleId
    ) {
        if (playerId == null
                || message == null
                || message.isBlank()) {
            return;
        }

        List<ChatBubble> bubbles =
                ACTIVE_BUBBLES.computeIfAbsent(
                        playerId,
                        ignored -> new ArrayList<>()
                );

        bubbles.add(
                new ChatBubble(
                        message,
                        color,
                        textoBranco,
                        styleId
                )
        );

        while (bubbles.size() > MAX_BUBBLES_PER_PLAYER) {
            bubbles.remove(0);
        }
    }

    private static final class StackPanel {

        private final ChatBubble bubble;
        private final List<FormattedCharSequence> lines;
        private final float height;
        private final float textStartY;

        private float width;
        private float y;

        private StackPanel(
                ChatBubble bubble,
                List<FormattedCharSequence> lines,
                float width,
                float height,
                float textStartY
        ) {
            this.bubble = bubble;
            this.lines = lines;
            this.width = width;
            this.height = height;
            this.textStartY = textStartY;
        }
    }

    private static final class TextureCrop {

        private static final TextureCrop FULL =
                new TextureCrop(0.0F, 1.0F);

        private final float minV;
        private final float maxV;

        private TextureCrop(
                float minV,
                float maxV
        ) {
            this.minV = minV;
            this.maxV = maxV;
        }
    }
}