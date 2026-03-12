package net.bubblify.mod.event;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.bubblify.mod.BubblifyMod;
import org.joml.Matrix4f;

import java.util.*;

@Mod.EventBusSubscriber(modid = BubblifyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {
    public static class ChatBubble {
        public String text;
        public int color;
        public int ticksRemaining;
        public float currentYOffset;

        public ChatBubble(String text, int color) {
            this.text = text;
            this.color = color;
            this.ticksRemaining = 300;
            this.currentYOffset = 0.0F;
        }
    }
    public static final Map<UUID, List<ChatBubble>> ACTIVE_BUBBLES = new HashMap<>();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().level != null) {
            Iterator<Map.Entry<UUID, List<ChatBubble>>> it = ACTIVE_BUBBLES.entrySet().iterator();
            while (it.hasNext()) {
                List<ChatBubble> bubbles = it.next().getValue();

                bubbles.removeIf(bubble -> {
                    bubble.ticksRemaining--;
                    return bubble.ticksRemaining <= 0;
                });

                if (bubbles.isEmpty()) {
                    it.remove();
                }
            }
        }
    }



    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        List<ChatBubble> bubbles = ACTIVE_BUBBLES.get(player.getUUID());
        if (bubbles != null && !bubbles.isEmpty()) {
            PoseStack poseStack = event.getPoseStack();
            Font font = Minecraft.getInstance().font;
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

            float heightOffset = player.getBbHeight() + 0.8F;

            poseStack.pushPose();
            poseStack.translate(0.0D, heightOffset, 0.0D);
            poseStack.mulPose(dispatcher.cameraOrientation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);

            Matrix4f matrix4f = poseStack.last().pose();
            int light = event.getPackedLight();
            int bubbleSpacing = 14;

            for (int i = 0; i < bubbles.size(); i++) {
                ChatBubble bubble = bubbles.get(i);

                int reverseIndex = bubbles.size() - 1 - i;
                float targetYOffset = - (reverseIndex * bubbleSpacing);

                bubble.currentYOffset += (targetYOffset - bubble.currentYOffset) * 0.15F;

                int textWidth = font.width(bubble.text);
                float xOffset = (float) (-textWidth / 2);

                int bgColor = 0xAA000000 | (bubble.color & 0xFFFFFF);

                font.drawInBatch(bubble.text, xOffset, bubble.currentYOffset, 0xFFFFFF, false, matrix4f, event.getMultiBufferSource(), Font.DisplayMode.NORMAL, bgColor, light);
            }

            poseStack.popPose();
        }
    }



    private static void drawRoundedBalloon(com.mojang.blaze3d.vertex.VertexConsumer buffer, Matrix4f matrix, float x, float y, float width, float height, int color, int borderColor, int light, boolean hasTail) {
        float left = x - 4;
        float right = x + width + 4;
        float top = y - 4;
        float bottom = y + height + 4;

        fill(buffer, matrix, left + 2, top - 1, right - 2, bottom + 1, borderColor, light); // Centro
        fill(buffer, matrix, left - 1, top + 2, right + 1, bottom - 2, borderColor, light); // Lados
        fill(buffer, matrix, left, top, left + 2, top + 2, borderColor, light); // Canto Superior Esquerdo
        fill(buffer, matrix, right - 2, top, right, top + 2, borderColor, light); // Canto Superior Direito
        fill(buffer, matrix, left, bottom - 2, left + 2, bottom, borderColor, light); // Canto Inferior Esquerdo
        fill(buffer, matrix, right - 2, bottom - 2, right, bottom, borderColor, light); // Canto Inferior Direito

        if (hasTail) {
            fill(buffer, matrix, -3, bottom, 3, bottom + 3, borderColor, light); // Base da cauda
            fill(buffer, matrix, -2, bottom + 3, 2, bottom + 4, borderColor, light); // Ponta da cauda
        }

        // 2. DESENHANDO O FUNDO COLORIDO (Por dentro da borda)
        fill(buffer, matrix, left + 2, top, right - 2, bottom, color, light); // Centro
        fill(buffer, matrix, left, top + 2, right, bottom - 2, color, light); // Lados
        fill(buffer, matrix, left + 1, top + 1, left + 2, top + 2, color, light); // Canto Superior Esquerdo
        fill(buffer, matrix, right - 2, top + 1, right - 1, top + 2, color, light); // Canto Superior Direito
        fill(buffer, matrix, left + 1, bottom - 2, left + 2, bottom - 1, color, light); // Canto Inferior Esquerdo
        fill(buffer, matrix, right - 2, bottom - 2, right - 1, bottom - 1, color, light); // Canto Inferior Direito

        if (hasTail) {
            fill(buffer, matrix, -2, bottom, 2, bottom + 2, color, light); // Base da cauda
            fill(buffer, matrix, -1, bottom + 2, 1, bottom + 3, color, light); // Ponta da cauda
        }
    }

    private static void fill(com.mojang.blaze3d.vertex.VertexConsumer consumer, Matrix4f matrix, float minX, float minY, float maxX, float maxY, int color, int light) {
        int a = (color >> 24) & 255;
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;

        consumer.vertex(matrix, minX, maxY, 0.01F).color(r, g, b, a).uv2(light).endVertex();
        consumer.vertex(matrix, maxX, maxY, 0.01F).color(r, g, b, a).uv2(light).endVertex();
        consumer.vertex(matrix, maxX, minY, 0.01F).color(r, g, b, a).uv2(light).endVertex();
        consumer.vertex(matrix, minX, minY, 0.01F).color(r, g, b, a).uv2(light).endVertex();
    }
}
