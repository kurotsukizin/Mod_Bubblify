package net.bubblify.mod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.bubblify.mod.BubblifyMod;
import net.bubblify.mod.BubblifyServerConfig;
import net.bubblify.mod.bubble.BubbleStyle;
import net.bubblify.mod.bubble.BubbleStylePermissions;
import net.bubblify.mod.bubble.BubbleStyleRegistry;
import net.bubblify.mod.event.ServerChatEvents;
import net.bubblify.mod.network.AllowedBubbleStylesPacket;
import net.bubblify.mod.network.ModMessages;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = BubblifyMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class BubblifyCommand {

    private static final String ROLE_NPC = "npc";
    private static final String ROLE_CHILDREN = "children";

    private BubblifyCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(
            RegisterCommandsEvent event
    ) {
        CommandDispatcher<CommandSourceStack> dispatcher =
                event.getDispatcher();

        dispatcher.register(
                Commands.literal("bubblify")
                        .requires(source -> source.hasPermission(2))

                        .then(
                                Commands.literal("show_chat")
                                        .then(
                                                Commands.argument(
                                                        "valor",
                                                        BoolArgumentType.bool()
                                                ).executes(context -> {
                                                    boolean novoValor =
                                                            BoolArgumentType
                                                                    .getBool(
                                                                            context,
                                                                            "valor"
                                                                    );

                                                    BubblifyServerConfig
                                                            .SHOW_CHAT
                                                            .set(novoValor);

                                                    context.getSource()
                                                            .sendSuccess(
                                                                    () -> Component.translatable(
                                                                            "commands.bubblifymod.show_chat.success",
                                                                            novoValor
                                                                    ),
                                                                    true
                                                            );

                                                    return 1;
                                                })
                                        )
                        )

                        .then(
                                Commands.literal("class")
                                        .then(
                                                Commands.argument(
                                                                "player",
                                                                EntityArgument.player()
                                                        )

                                                        .then(
                                                                Commands.literal("npc")
                                                                        .executes(
                                                                                context -> {
                                                                                    ServerPlayer player =
                                                                                            EntityArgument.getPlayer(
                                                                                                    context,
                                                                                                    "player"
                                                                                            );

                                                                                    setPlayerRole(
                                                                                            player,
                                                                                            ROLE_NPC
                                                                                    );

                                                                                    context.getSource()
                                                                                            .sendSuccess(
                                                                                                    () -> Component.translatable(
                                                                                                            "commands.bubblifymod.class.success",
                                                                                                            player.getName().getString(),
                                                                                                            "NPC"
                                                                                                    ),
                                                                                                    true
                                                                                            );

                                                                                    return 1;
                                                                                }
                                                                        )
                                                        )

                                                        .then(
                                                                Commands.literal("children")
                                                                        .executes(
                                                                                context -> {
                                                                                    ServerPlayer player =
                                                                                            EntityArgument.getPlayer(
                                                                                                    context,
                                                                                                    "player"
                                                                                            );

                                                                                    setPlayerRole(
                                                                                            player,
                                                                                            ROLE_CHILDREN
                                                                                    );

                                                                                    context.getSource()
                                                                                            .sendSuccess(
                                                                                                    () -> Component.translatable(
                                                                                                            "commands.bubblifymod.class.success",
                                                                                                            player.getName().getString(),
                                                                                                            "CHILDREN"
                                                                                                    ),
                                                                                                    true
                                                                                            );

                                                                                    return 1;
                                                                                }
                                                                        )
                                                        )

                                                        .then(
                                                                Commands.literal("clear")
                                                                        .executes(
                                                                                context -> {
                                                                                    ServerPlayer player =
                                                                                            EntityArgument.getPlayer(
                                                                                                    context,
                                                                                                    "player"
                                                                                            );

                                                                                    clearPlayerRole(player);

                                                                                    context.getSource()
                                                                                            .sendSuccess(
                                                                                                    () -> Component.translatable(
                                                                                                            "commands.bubblifymod.class.cleared",
                                                                                                            player.getName().getString()
                                                                                                    ),
                                                                                                    true
                                                                                            );


                                                                                    return 1;
                                                                                }
                                                                        )
                                                        )
                                        )
                        )
        );
    }

    private static void setPlayerRole(
            ServerPlayer player,
            String role
    ) {
        player.getPersistentData().putString(
                BubbleStylePermissions.ROLE_KEY,
                role
        );

        refreshPlayerBubblePermissions(player);
    }

    private static void clearPlayerRole(ServerPlayer player) {
        player.getPersistentData().remove(
                BubbleStylePermissions.ROLE_KEY
        );

        refreshPlayerBubblePermissions(player);
    }

    private static void refreshPlayerBubblePermissions(
            ServerPlayer player
    ) {

        String savedStyleId = player.getPersistentData().contains(
                ServerChatEvents.STYLE_KEY
        )
                ? player.getPersistentData().getString(
                ServerChatEvents.STYLE_KEY
        )
                : BubbleStyle.DEFAULT.getId();

        BubbleStyle requestedStyle =
                BubbleStyleRegistry.getOrDefault(savedStyleId);


        BubbleStyle allowedStyle =
                BubbleStylePermissions.canUseStyle(
                        player,
                        requestedStyle
                )
                        ? requestedStyle
                        : BubbleStyle.DEFAULT;

        player.getPersistentData().putString(
                ServerChatEvents.STYLE_KEY,
                allowedStyle.getId()
        );


        ServerChatEvents.loadPlayerAppearance(player);


        ModMessages.sendToPlayer(
                new AllowedBubbleStylesPacket(
                        BubbleStylePermissions.getAllowedStyleIds(
                                player
                        )
                ),
                player
        );
    }
}