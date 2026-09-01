package net.bubblify.mod.event;

import net.bubblify.mod.BubblifyMod;
import net.bubblify.mod.BubblifyServerConfig;
import net.bubblify.mod.network.BubbleMessagePacket;
import net.bubblify.mod.network.ModMessages;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = BubblifyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerChatEvents {

    public static final Map<UUID, Integer> PLAYER_COLORS = new HashMap<>();

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String message = event.getRawText();

        if (message.isBlank()) {
            return;
        }

        if (message.length() > 256) {
            message = message.substring(0, 256);
        }

        int playerColor = PLAYER_COLORS.getOrDefault(player.getUUID(), 0x000000);
        String classeDoJogador = player.getPersistentData().getString("bubblify_role");
        boolean temPermissaoEspecial = "npc".equals(classeDoJogador) || "children".equals(classeDoJogador);

        boolean mostrarChatNormal = BubblifyServerConfig.SHOW_CHAT.get();

        if (!mostrarChatNormal || temPermissaoEspecial) {
            event.setCanceled(true);

            ModMessages.sendToAllPlayers(new BubbleMessagePacket(player.getUUID(), message, playerColor));
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.getPersistentData().contains("bubblify_chat_color")) {
                int savedColor = player.getPersistentData().getInt("bubblify_chat_color");
                PLAYER_COLORS.put(player.getUUID(), savedColor);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getOriginal().getPersistentData().contains("bubblify_chat_color")) {
            int savedColor = event.getOriginal().getPersistentData().getInt("bubblify_chat_color");
            event.getEntity().getPersistentData().putInt("bubblify_chat_color", savedColor);
            PLAYER_COLORS.put(event.getEntity().getUUID(), savedColor);
        }
    }
}