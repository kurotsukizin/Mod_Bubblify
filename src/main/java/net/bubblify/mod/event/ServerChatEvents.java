package net.bubblify.mod.event;

import net.bubblify.mod.BubblifyMod;
import net.bubblify.mod.BubblifyServerConfig;
import net.bubblify.mod.bubble.BubbleStyle;
import net.bubblify.mod.network.BubbleMessagePacket;
import net.bubblify.mod.network.ModMessages;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(
        modid = BubblifyMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class ServerChatEvents {

    /*
     * Chaves persistentes usadas no player.getPersistentData().
     *
     * BubblifyCommand já usa STYLE_KEY, por isso ela precisa
     * existir publicamente nesta classe.
     */
    public static final String COLOR_KEY =
            "bubblify_chat_color";

    public static final String TEXT_WHITE_KEY =
            "bubblify_text_white";

    public static final String STYLE_KEY =
            "bubblify_style";

    public static final String ROLE_KEY =
            "bubblify_role";

    public static final Map<UUID, Integer> PLAYER_COLORS =
            new HashMap<>();

    public static final Map<UUID, Boolean> PLAYER_TEXT_WHITE =
            new HashMap<>();

    public static final Map<UUID, String> PLAYER_STYLES =
            new HashMap<>();

    private ServerChatEvents() {
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();

        if (player == null) {
            return;
        }

        String message = event.getRawText();

        if (message == null || message.isBlank()) {
            return;
        }

        UUID playerId = player.getUUID();

        int playerColor = PLAYER_COLORS.getOrDefault(
                playerId,
                0xFFFFFF
        );

        boolean textoBranco = PLAYER_TEXT_WHITE.getOrDefault(
                playerId,
                true
        );

        String storedStyleId = PLAYER_STYLES.getOrDefault(
                playerId,
                BubbleStyle.DEFAULT.getId()
        );

        String styleId = resolveAllowedStyle(
                player,
                storedStyleId
        );

        boolean showNormalChat =
                BubblifyServerConfig.SHOW_CHAT.get();

        if (!showNormalChat) {
            event.setCanceled(true);
        }

        if (!showNormalChat || hasBubbleRole(player)) {
            ModMessages.sendToAllPlayers(
                    new BubbleMessagePacket(
                            playerId,
                            message,
                            playerColor,
                            textoBranco,
                            styleId
                    )
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(
            PlayerEvent.PlayerLoggedInEvent event
    ) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        loadPlayerAppearance(player);
    }

    @SubscribeEvent
    public static void onPlayerClone(
            PlayerEvent.Clone event
    ) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        copyPersistentAppearance(
                event.getOriginal(),
                player
        );

        loadPlayerAppearance(player);
    }

    /*
     * Método chamado por BubblifyCommand após alterar o estilo
     * de um jogador e também no evento de login.
     */
    public static void loadPlayerAppearance(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        UUID playerId = player.getUUID();

        int color = player.getPersistentData().contains(
                COLOR_KEY
        )
                ? player.getPersistentData().getInt(COLOR_KEY)
                : 0xFFFFFF;

        boolean textoBranco =
                !player.getPersistentData().contains(
                        TEXT_WHITE_KEY
                )
                        || player.getPersistentData().getBoolean(
                        TEXT_WHITE_KEY
                );

        String styleId =
                player.getPersistentData().contains(STYLE_KEY)
                        ? player.getPersistentData().getString(
                        STYLE_KEY
                )
                        : BubbleStyle.DEFAULT.getId();

        styleId = resolveAllowedStyle(
                player,
                styleId
        );

        PLAYER_COLORS.put(
                playerId,
                color & 0xFFFFFF
        );

        PLAYER_TEXT_WHITE.put(
                playerId,
                textoBranco
        );

        PLAYER_STYLES.put(
                playerId,
                styleId
        );

        /*
         * Corrige dados inválidos persistidos antigamente.
         */
        player.getPersistentData().putString(
                STYLE_KEY,
                styleId
        );
    }

    /*
     * Chamado por UpdateColorPacket.
     *
     * O servidor salva a aparência depois de validar
     * estilo, role e permissões.
     */
    public static void savePlayerPreferences(
            ServerPlayer player,
            int color,
            boolean textoBranco,
            String requestedStyleId
    ) {
        if (player == null) {
            return;
        }

        UUID playerId = player.getUUID();

        int safeColor = color & 0xFFFFFF;

        String safeStyleId = resolveAllowedStyle(
                player,
                requestedStyleId
        );

        PLAYER_COLORS.put(
                playerId,
                safeColor
        );

        PLAYER_TEXT_WHITE.put(
                playerId,
                textoBranco
        );

        PLAYER_STYLES.put(
                playerId,
                safeStyleId
        );

        player.getPersistentData().putInt(
                COLOR_KEY,
                safeColor
        );

        player.getPersistentData().putBoolean(
                TEXT_WHITE_KEY,
                textoBranco
        );

        player.getPersistentData().putString(
                STYLE_KEY,
                safeStyleId
        );
    }

    /*
     * Regra de acesso aos estilos:
     *
     * Admin/op nível 2 ou maior:
     * - qualquer ID válido.
     *
     * NPC:
     * - default
     * - npc_*.png
     *
     * Children:
     * - default
     * - children_*.png
     *
     * Jogador comum:
     * - somente default.
     *
     * A existência da textura é validada no cliente,
     * pois as texturas são descobertas no ResourceManager
     * do cliente/resource pack.
     */
    public static String resolveAllowedStyle(
            ServerPlayer player,
            String requestedStyleId
    ) {
        String safeStyleId =
                sanitizeStyleId(requestedStyleId);

        if (player == null) {
            return BubbleStyle.DEFAULT.getId();
        }

        if (player.hasPermissions(2)) {
            return safeStyleId;
        }

        String role = getPlayerRole(player);

        if ("npc".equals(role)) {
            if (BubbleStyle.DEFAULT.getId().equals(
                    safeStyleId
            ) || safeStyleId.startsWith("npc_")) {
                return safeStyleId;
            }

            return BubbleStyle.DEFAULT.getId();
        }

        if ("children".equals(role)) {
            if (BubbleStyle.DEFAULT.getId().equals(
                    safeStyleId
            ) || safeStyleId.startsWith("children_")) {
                return safeStyleId;
            }

            return BubbleStyle.DEFAULT.getId();
        }

        return BubbleStyle.DEFAULT.getId();
    }

    public static boolean hasBubbleRole(
            ServerPlayer player
    ) {
        if (player == null) {
            return false;
        }

        if (player.hasPermissions(2)) {
            return true;
        }

        String role = getPlayerRole(player);

        return "npc".equals(role)
                || "children".equals(role);
    }

    public static String getPlayerRole(
            ServerPlayer player
    ) {
        if (player == null) {
            return "";
        }

        return player.getPersistentData()
                .getString(ROLE_KEY)
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private static String sanitizeStyleId(
            String styleId
    ) {
        if (styleId == null || styleId.isBlank()) {
            return BubbleStyle.DEFAULT.getId();
        }

        String normalizedId = styleId
                .trim()
                .toLowerCase(Locale.ROOT);

        if (normalizedId.length() > 64) {
            return BubbleStyle.DEFAULT.getId();
        }

        if (!normalizedId.matches("[a-z0-9_./-]+")) {
            return BubbleStyle.DEFAULT.getId();
        }

        if (normalizedId.contains("..")
                || normalizedId.startsWith(".")
                || normalizedId.startsWith("/")) {
            return BubbleStyle.DEFAULT.getId();
        }

        return normalizedId;
    }

    private static void copyPersistentAppearance(
            net.minecraft.world.entity.player.Player source,
            ServerPlayer target
    ) {
        if (source.getPersistentData().contains(COLOR_KEY)) {
            target.getPersistentData().putInt(
                    COLOR_KEY,
                    source.getPersistentData().getInt(
                            COLOR_KEY
                    )
            );
        }

        if (source.getPersistentData().contains(
                TEXT_WHITE_KEY
        )) {
            target.getPersistentData().putBoolean(
                    TEXT_WHITE_KEY,
                    source.getPersistentData().getBoolean(
                            TEXT_WHITE_KEY
                    )
            );
        }

        if (source.getPersistentData().contains(STYLE_KEY)) {
            target.getPersistentData().putString(
                    STYLE_KEY,
                    source.getPersistentData().getString(
                            STYLE_KEY
                    )
            );
        }

        if (source.getPersistentData().contains(ROLE_KEY)) {
            target.getPersistentData().putString(
                    ROLE_KEY,
                    source.getPersistentData().getString(
                            ROLE_KEY
                    )
            );
        }
    }
}