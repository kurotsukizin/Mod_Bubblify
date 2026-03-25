package net.bubblify.mod.commands; // Ajuste para o seu pacote!

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.bubblify.mod.BubblifyServerConfig;

@Mod.EventBusSubscriber(modid = "bubblifymod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BubblifyCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("bubblify")

                .requires(source -> source.hasPermission(2))

                .then(Commands.literal("show_chat")

                        .then(Commands.argument("valor", BoolArgumentType.bool())

                                .executes(context -> {
                                    boolean novoValor = BoolArgumentType.getBool(context, "valor");

                                    BubblifyServerConfig.SHOW_CHAT.set(novoValor);

                                    context.getSource().sendSuccess(() ->
                                            Component.literal("§a[Bubblify] O chat padrão agora está definido como: " + novoValor), true);

                                    return 1;
                                })
                        )
                )
        );
    }
}