package net.bubblify.mod;

import com.mojang.logging.LogUtils;
import net.bubblify.mod.bubble.BubbleStyleRegistry;
import net.bubblify.mod.network.ModMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@SuppressWarnings("removal")
@Mod(BubblifyMod.MOD_ID)
public class BubblifyMod {

    public static final String MOD_ID = "bubblifymod";

    public static final Logger LOGGER =
            LogUtils.getLogger();

    public BubblifyMod(
            FMLJavaModLoadingContext context
    ) {
        IEventBus modEventBus =
                context.getModEventBus();

        modEventBus.addListener(
                this::commonSetup
        );

        modEventBus.addListener(
                this::addCreative
        );

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(
                ModConfig.Type.SERVER,
                BubblifyServerConfig.SPEC
        );

        ModLoadingContext.get().registerConfig(
                ModConfig.Type.CLIENT,
                net.bubblify.mod.config.BubblifyClientConfig.SPEC
        );
    }

    private void commonSetup(
            final FMLCommonSetupEvent event
    ) {
        event.enqueueWork(() -> {

            BubbleStyleRegistry.registerDefaults();

            ModMessages.register();
        });
    }

    private void addCreative(
            BuildCreativeModeTabContentsEvent event
    ) {
    }

    @SubscribeEvent
    public void onServerStarting(
            ServerStartingEvent event
    ) {
    }

    @Mod.EventBusSubscriber(
            modid = MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT
    )
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(
                FMLClientSetupEvent event
        ) {
            event.enqueueWork(() -> {
                Minecraft minecraft =
                        Minecraft.getInstance();

                ResourceManager resourceManager =
                        minecraft.getResourceManager();

                BubbleStyleRegistry.registerDefaults();

                BubbleStyleRegistry.discoverAndRegisterStyles(
                        resourceManager
                );
            });
        }
    }
}