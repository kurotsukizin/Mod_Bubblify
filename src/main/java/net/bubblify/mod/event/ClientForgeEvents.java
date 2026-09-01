package net.bubblify.mod.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.bubblify.mod.BubblifyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

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
    private static final float BUBBLE_SCALE = 0.025F;
    private static final float BUBBLE_SPACING = 14.0F;
    private static final float BUBBLE_HEIGHT_OFFSET = 0.8F;

    public static final Map<UUID, List<ChatBubble>> ACTIVE_BUBBLES =
            new HashMap<>();

    public static class ChatBubble {

        public final String text;
        public final int color;

        /*
         * true  = white text
         * false = black text
         *
         * This value belongs to the player who sent the chat message.
         */
        public final boolean textoBranco;

        public int ticksRemaining;
        public float currentYOffset;

        public ChatBubble(
                String text,
                int color,
                boolean textoBranco
        ) {
            this.text = text;
            this.color = color & 0xFFFFFF;
            this.textoBranco = textoBranco;
            this.ticksRemaining = BUBBLE_LIFETIME_TICKS;
            this.currentYOffset = 0.0F;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || Minecraft.getInstance().level == null) {
            return;
        }

        Iterator<Map.Entry<UUID, List<ChatBubble>>> iterator =
                ACTIVE_BUBBLES.entrySet().iterator();

        while (iterator.hasNext()) {
            List<ChatBubble> bubbles = iterator.next().getValue();

            bubbles.removeIf(bubble -> --bubble.ticksRemaining <= 0);

            if (bubbles.isEmpty()) {
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage()
                != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null
                || minecraft.player == null
                || ACTIVE_BUBBLES.isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();

        MultiBufferSource.BufferSource bufferSource =
                minecraft.renderBuffers().bufferSource();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        /*
         * The bubble is intentionally rendered above entities,
         * blocks and particles.
         */
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        try {
            for (Map.Entry<UUID, List<ChatBubble>> entry
                    : ACTIVE_BUBBLES.entrySet()) {

                Player player = findPlayer(entry.getKey());

                if (player == null || entry.getValue().isEmpty()) {
                    continue;
                }

                double x = player.xOld
                        + (player.getX() - player.xOld)
                        * event.getPartialTick();

                double y = player.yo
                        + (player.getY() - player.yo)
                        * event.getPartialTick();

                double z = player.zOld
                        + (player.getZ() - player.zOld)
                        * event.getPartialTick();

                renderBubbleAt(
                        player,
                        x,
                        y,
                        z,
                        poseStack,
                        bufferSource,
                        entry.getValue()
                );
            }

            /*
             * This is valid because bufferSource is explicitly
             * MultiBufferSource.BufferSource.
             */
            bufferSource.endBatch();

        } finally {
            /*
             * Restore Minecraft's rendering state even if another
             * error happens while a bubble is being rendered.
             */
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
    }

    private static Player findPlayer(UUID uuid) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return null;
        }

        for (Player player : minecraft.level.players()) {
            if (player.getUUID().equals(uuid)) {
                return player;
            }
        }

        return null;
    }

    private static void renderBubbleAt(
            Player player,
            double x,
            double y,
            double z,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            List<ChatBubble> bubbles
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        Font font = minecraft.font;

        EntityRenderDispatcher dispatcher =
                minecraft.getEntityRenderDispatcher();

        double cameraX = dispatcher.camera.getPosition().x();
        double cameraY = dispatcher.camera.getPosition().y();
        double cameraZ = dispatcher.camera.getPosition().z();

        poseStack.pushPose();

        poseStack.translate(
                x - cameraX,
                y - cameraY
                        + player.getBbHeight()
                        + BUBBLE_HEIGHT_OFFSET,
                z - cameraZ
        );

        poseStack.mulPose(dispatcher.cameraOrientation());

        poseStack.scale(
                -BUBBLE_SCALE,
                -BUBBLE_SCALE,
                BUBBLE_SCALE
        );

        Matrix4f matrix = poseStack.last().pose();

        for (int index = 0; index < bubbles.size(); index++) {
            ChatBubble bubble = bubbles.get(index);

            int reverseIndex = bubbles.size() - 1 - index;

            float targetYOffset =
                    -(reverseIndex * BUBBLE_SPACING);

            bubble.currentYOffset +=
                    (targetYOffset - bubble.currentYOffset) * 0.15F;

            int textWidth = font.width(bubble.text);

            float bubbleX = -textWidth / 2.0F;
            float bubbleY = bubble.currentYOffset;

            int backgroundColor =
                    0xCC000000 | bubble.color;

            int borderColor = 0xFF000000;

            /*
             * The text color is defined by the sender of the message.
             */
            int textColor = bubble.textoBranco
                    ? 0xFFFFFF
                    : 0x000000;

            drawRoundedBalloon(
                    matrix,
                    bubbleX,
                    bubbleY,
                    textWidth,
                    9,
                    backgroundColor,
                    borderColor
            );

            font.drawInBatch(
                    bubble.text,
                    bubbleX,
                    bubbleY,
                    textColor,
                    false,
                    matrix,
                    bufferSource,
                    Font.DisplayMode.NORMAL,
                    0,
                    15728880
            );
        }

        poseStack.popPose();
    }

    private static void drawRoundedBalloon(
            Matrix4f matrix,
            float x,
            float y,
            float width,
            float height,
            int backgroundColor,
            int borderColor
    ) {
        float left = x - 4.0F;
        float right = x + width + 4.0F;
        float top = y - 4.0F;
        float bottom = y + height + 4.0F;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();

        builder.begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR
        );

        // Pixel-style black outer border.
        addQuad(
                builder,
                matrix,
                left + 2,
                top - 1,
                right - 2,
                bottom + 1,
                borderColor
        );

        addQuad(
                builder,
                matrix,
                left - 1,
                top + 2,
                right + 1,
                bottom - 2,
                borderColor
        );

        addQuad(
                builder,
                matrix,
                left,
                top,
                left + 2,
                top + 2,
                borderColor
        );

        addQuad(
                builder,
                matrix,
                right - 2,
                top,
                right,
                top + 2,
                borderColor
        );

        addQuad(
                builder,
                matrix,
                left,
                bottom - 2,
                left + 2,
                bottom,
                borderColor
        );

        addQuad(
                builder,
                matrix,
                right - 2,
                bottom - 2,
                right,
                bottom,
                borderColor
        );

        // Colored inner background.
        addQuad(
                builder,
                matrix,
                left + 2,
                top,
                right - 2,
                bottom,
                backgroundColor
        );

        addQuad(
                builder,
                matrix,
                left,
                top + 2,
                right,
                bottom - 2,
                backgroundColor
        );

        addQuad(
                builder,
                matrix,
                left + 1,
                top + 1,
                left + 2,
                top + 2,
                backgroundColor
        );

        addQuad(
                builder,
                matrix,
                right - 2,
                top + 1,
                right - 1,
                top + 2,
                backgroundColor
        );

        addQuad(
                builder,
                matrix,
                left + 1,
                bottom - 2,
                left + 2,
                bottom - 1,
                backgroundColor
        );

        addQuad(
                builder,
                matrix,
                right - 2,
                bottom - 2,
                right - 1,
                bottom - 1,
                backgroundColor
        );

        // Speech bubble tail.
        addQuad(
                builder,
                matrix,
                -3,
                bottom,
                3,
                bottom + 3,
                borderColor
        );

        addQuad(
                builder,
                matrix,
                -2,
                bottom + 3,
                2,
                bottom + 4,
                borderColor
        );

        addQuad(
                builder,
                matrix,
                -2,
                bottom,
                2,
                bottom + 2,
                backgroundColor
        );

        addQuad(
                builder,
                matrix,
                -1,
                bottom + 2,
                1,
                bottom + 3,
                backgroundColor
        );

        tesselator.end();
    }

    private static void addQuad(
            BufferBuilder builder,
            Matrix4f matrix,
            float minX,
            float minY,
            float maxX,
            float maxY,
            int color
    ) {
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        builder.vertex(matrix, minX, maxY, 0.01F)
                .color(red, green, blue, alpha)
                .endVertex();

        builder.vertex(matrix, maxX, maxY, 0.01F)
                .color(red, green, blue, alpha)
                .endVertex();

        builder.vertex(matrix, maxX, minY, 0.01F)
                .color(red, green, blue, alpha)
                .endVertex();

        builder.vertex(matrix, minX, minY, 0.01F)
                .color(red, green, blue, alpha)
                .endVertex();
    }

    public static void addBubble(
            UUID playerId,
            String message,
            int color,
            boolean textoBranco
    ) {
        List<ChatBubble> bubbles =
                ACTIVE_BUBBLES.computeIfAbsent(
                        playerId,
                        id -> new ArrayList<>()
                );

        bubbles.add(
                new ChatBubble(
                        message,
                        color,
                        textoBranco
                )
        );

        if (bubbles.size() > MAX_BUBBLES_PER_PLAYER) {
            bubbles.remove(0);
        }
    }
}