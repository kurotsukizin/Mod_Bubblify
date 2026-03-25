package net.bubblify.mod.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.bubblify.mod.BubblifyServerConfig;

@Mod.EventBusSubscriber(modid = "bubblifymod", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ChatEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onClientChat(ClientChatReceivedEvent event) {

        boolean mostrarChat = BubblifyServerConfig.SHOW_CHAT.get();

        System.out.println("[BUBBLIFY DEBUG] O Cliente acha que mostrarChat é: " + mostrarChat);

        if (!mostrarChat) {
            if (!event.isSystem()) {
                event.setCanceled(true);
            }
        }
    }
}