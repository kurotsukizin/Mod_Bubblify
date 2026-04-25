package net.bubblify.mod.commands; // Ajuste para o seu pacote!

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
                                            Component.translatable("commands.bubblifymod.show_chat.success", novoValor), true);

                                    return 1;
                                })
                        )
                )

                .then(Commands.literal("class")
                        .then(Commands.argument("player", EntityArgument.player())

                                .then(Commands.literal("npc")
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                            player.getPersistentData().putString("bubblify_role", "npc");

                                            context.getSource().sendSuccess(() ->
                                                    Component.translatable("commands.bubblifymod.class.success", player.getName().getString(), "NPC"), true);
                                            return 1;
                                        })
                                )

                                .then(Commands.literal("children")
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                            player.getPersistentData().putString("bubblify_role", "children");

                                            context.getSource().sendSuccess(() ->
                                                    Component.translatable("commands.bubblifymod.class.success", player.getName().getString(), "CHILDREN"), true);
                                            return 1;
                                        })
                                )

                                .then(Commands.literal("clear")
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");

                                            player.getPersistentData().remove("bubblify_role");

                                            context.getSource().sendSuccess(() ->
                                                    Component.translatable("commands.bubblifymod.class.cleared", player.getName().getString()), true);
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}