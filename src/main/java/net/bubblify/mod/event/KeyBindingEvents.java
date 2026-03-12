package net.bubblify.mod.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.bubblify.mod.BubblifyMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.bubblify.mod.client.ColorPickerScreen;
import org.lwjgl.glfw.GLFW;

public class KeyBindingEvents {

    public static final KeyMapping COLOR_MENU_KEY = new KeyMapping(
            "key.bubblifymod.color_menu",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "category.bubblifymod.keys"
    );

    @Mod.EventBusSubscriber(modid = BubblifyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(COLOR_MENU_KEY);
        }
    }

    @Mod.EventBusSubscriber(modid = BubblifyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientForgeBusEvents {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (COLOR_MENU_KEY.consumeClick() && Minecraft.getInstance().screen == null) {
                Minecraft.getInstance().setScreen(new ColorPickerScreen());
            }
        }
    }
}