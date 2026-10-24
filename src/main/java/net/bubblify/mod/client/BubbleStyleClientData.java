package net.bubblify.mod.client;

import net.bubblify.mod.bubble.BubbleStyle;
import net.bubblify.mod.bubble.BubbleStyleRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class BubbleStyleClientData {

    private static final String DEFAULT_STYLE_ID =
            BubbleStyle.DEFAULT.getId();

    /*
     * IDs enviados e autorizados pelo servidor para o jogador atual.
     *
     * Exemplo de jogador comum:
     * ["default"]
     *
     * Exemplo de NPC:
     * ["default", "npc_guard", "npc_royal"]
     *
     * Exemplo de Children:
     * ["default", "children_cloud", "children_star"]
     *
     * Exemplo de admin:
     * ["default", "npc_guard", "children_star", ...]
     */
    private static final Set<String> ALLOWED_STYLE_IDS =
            new LinkedHashSet<>();

    /*
     * Define se a aba Style aparece no menu.
     *
     * O padrão é false para que, antes de o servidor informar
     * permissões, jogadores comuns tenham somente a aba Color.
     */
    private static boolean styleTabAllowed = false;

    /*
     * Seleção atual no cliente.
     */
    private static BubbleStyle selectedStyle =
            BubbleStyle.DEFAULT;

    static {
        resetToDefault();
    }

    private BubbleStyleClientData() {
    }

    public static BubbleStyle getSelectedStyle() {
        if (selectedStyle == null
                || !isStyleAllowed(selectedStyle)) {
            selectedStyle = BubbleStyle.DEFAULT;
        }

        return selectedStyle;
    }

    public static void setSelectedStyle(
            BubbleStyle style
    ) {
        if (style == null
                || !isStyleAllowed(style)) {
            selectedStyle = BubbleStyle.DEFAULT;
            return;
        }

        selectedStyle = style;
    }

    public static boolean canUseStyleTab() {
        return styleTabAllowed;
    }

    /*
     * Método usado pelo AllowedBubbleStylesPacket.
     *
     * Como o pacote atual aparentemente transmite apenas
     * List<String>, a aba Style é liberada quando a lista
     * recebida contém pelo menos um ID além de "default".
     *
     * Lista ["default"]:
     * - jogador normal
     * - aba Style escondida
     *
     * Lista ["default", "children_star"]:
     * - role Children ou admin
     * - aba Style visível
     */
    public static void setAllowedStyleIds(
            List<String> allowedStyleIds
    ) {
        boolean hasSpecialStyle = false;

        if (allowedStyleIds != null) {
            for (String styleId : allowedStyleIds) {
                String normalizedId =
                        normalizeStyleId(styleId);

                if (normalizedId == null) {
                    continue;
                }

                if (!DEFAULT_STYLE_ID.equals(normalizedId)) {
                    hasSpecialStyle = true;
                    break;
                }
            }
        }

        setServerPermissions(
                hasSpecialStyle,
                allowedStyleIds
        );
    }

    /*
     * Versão mais completa para um futuro pacote que envie
     * diretamente a flag da aba Style.
     */
    public static void setServerPermissions(
            boolean canUseStyleTab,
            List<String> allowedStyleIds
    ) {
        styleTabAllowed = canUseStyleTab;

        ALLOWED_STYLE_IDS.clear();

        /*
         * Default existe para todos, mesmo quando o servidor
         * acidentalmente não o inclui na lista.
         */
        ALLOWED_STYLE_IDS.add(DEFAULT_STYLE_ID);

        if (allowedStyleIds != null) {
            for (String styleId : allowedStyleIds) {
                String normalizedId =
                        normalizeStyleId(styleId);

                if (normalizedId == null) {
                    continue;
                }

                /*
                 * A lista vinda do servidor define a permissão.
                 * O registry garante que o cliente conhece
                 * a textura/estilo antes de mostrar o botão.
                 */
                if (DEFAULT_STYLE_ID.equals(normalizedId)
                        || BubbleStyleRegistry.contains(
                        normalizedId
                )) {
                    ALLOWED_STYLE_IDS.add(normalizedId);
                }
            }
        }

        /*
         * Sem permissão para Style, não conserva uma textura
         * especial que tenha ficado salva localmente.
         */
        if (!styleTabAllowed) {
            selectedStyle = BubbleStyle.DEFAULT;
            return;
        }

        /*
         * Caso o servidor tenha removido a permissão do estilo
         * selecionado atualmente, retorna para o padrão.
         */
        if (!isStyleAllowed(selectedStyle)) {
            selectedStyle = BubbleStyle.DEFAULT;
        }
    }

    /*
     * Útil apenas enquanto você está testando localmente
     * no runClient e não terminou o pacote de permissões.
     *
     * Não deixe sendo chamado em servidor real: permissões
     * precisam vir do servidor pelo AllowedBubbleStylesPacket.
     */
    public static void enableLocalStyleTest() {
        List<String> ids = new ArrayList<>();

        ids.add(DEFAULT_STYLE_ID);

        for (BubbleStyle style
                : BubbleStyleRegistry.getAllStyles()) {

            if (style == null) {
                continue;
            }

            ids.add(style.getId());
        }

        setServerPermissions(
                true,
                ids
        );
    }

    public static List<BubbleStyle> getAllowedStyles() {
        List<BubbleStyle> styles =
                new ArrayList<>();

        /*
         * Default vem sempre em primeiro lugar.
         */
        styles.add(BubbleStyle.DEFAULT);

        /*
         * Sem acesso à aba Style:
         * a lista retorna apenas Default.
         */
        if (!styleTabAllowed) {
            return Collections.unmodifiableList(styles);
        }

        for (String styleId : ALLOWED_STYLE_IDS) {
            if (DEFAULT_STYLE_ID.equals(styleId)) {
                continue;
            }

            BubbleStyle style =
                    BubbleStyle.findRegistered(styleId);

            /*
             * Não exibe estilos que o cliente não conseguiu
             * descobrir/carregar como textura válida.
             */
            if (style == null || style.isDefault()) {
                continue;
            }

            styles.add(style);
        }

        return Collections.unmodifiableList(styles);
    }

    public static List<String> getAllowedStyleIds() {
        return Collections.unmodifiableList(
                new ArrayList<>(ALLOWED_STYLE_IDS)
        );
    }

    public static boolean isStyleAllowed(
            BubbleStyle style
    ) {
        if (style == null) {
            return false;
        }

        return isStyleAllowed(style.getId());
    }

    public static boolean isStyleAllowed(
            String styleId
    ) {
        String normalizedId =
                normalizeStyleId(styleId);

        if (normalizedId == null) {
            return false;
        }

        /*
         * Todo jogador pode usar a bolha colorida Default.
         */
        if (DEFAULT_STYLE_ID.equals(normalizedId)) {
            return true;
        }

        /*
         * Jogador sem acesso à aba Style só pode usar Default.
         */
        if (!styleTabAllowed) {
            return false;
        }

        /*
         * Além da permissão do servidor, exige que o estilo
         * tenha sido registrado localmente a partir de um PNG.
         */
        return ALLOWED_STYLE_IDS.contains(normalizedId)
                && BubbleStyleRegistry.contains(normalizedId);
    }

    public static void resetToDefault() {
        styleTabAllowed = false;

        ALLOWED_STYLE_IDS.clear();
        ALLOWED_STYLE_IDS.add(DEFAULT_STYLE_ID);

        selectedStyle = BubbleStyle.DEFAULT;
    }

    private static String normalizeStyleId(
            String styleId
    ) {
        if (styleId == null || styleId.isBlank()) {
            return null;
        }

        String normalizedId = styleId
                .trim()
                .toLowerCase(Locale.ROOT);

        if (normalizedId.length() > 64) {
            return null;
        }

        if (!normalizedId.matches("[a-z0-9_./-]+")) {
            return null;
        }

        if (normalizedId.contains("..")
                || normalizedId.startsWith(".")
                || normalizedId.startsWith("/")) {
            return null;
        }

        return normalizedId;
    }
}