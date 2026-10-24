package net.bubblify.mod.bubble;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.bubblify.mod.BubblifyMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class BubbleStyleRegistry {

    private static final String STYLE_DIRECTORY =
            "bubble_styles";

    private static final Gson GSON =
            new Gson();

    private static final Map<String, BubbleStyle> STYLES =
            new LinkedHashMap<>();

    private BubbleStyleRegistry() {
    }

    /*
     * Chamado no common setup e também antes de descobrir estilos
     * do resource pack. É seguro em servidor dedicado porque não
     * acessa Minecraft.getInstance() nem classes client-only.
     */
    public static void registerDefaults() {
        STYLES.clear();

        register(
                BubbleStyle.DEFAULT
        );
    }

    public static void register(
            BubbleStyle style
    ) {
        if (style == null) {
            return;
        }

        String id =
                BubbleStyle.normalizeId(
                        style.getId()
                );

        STYLES.put(id, style);
    }

    public static BubbleStyle getOrDefault(
            String styleId
    ) {
        if (styleId == null || styleId.isBlank()) {
            return BubbleStyle.DEFAULT;
        }

        return STYLES.getOrDefault(
                BubbleStyle.normalizeId(styleId),
                BubbleStyle.DEFAULT
        );
    }

    public static boolean contains(
            String styleId
    ) {
        if (styleId == null || styleId.isBlank()) {
            return false;
        }

        return STYLES.containsKey(
                BubbleStyle.normalizeId(styleId)
        );
    }

    public static Collection<BubbleStyle> getAllStyles() {
        return Collections.unmodifiableList(
                new ArrayList<>(
                        STYLES.values()
                )
        );
    }

    public static List<BubbleStyle> getStylesByCategory(
            BubbleStyle.Category category
    ) {
        List<BubbleStyle> result =
                new ArrayList<>();

        for (BubbleStyle style : STYLES.values()) {
            if (style.getCategory() == category) {
                result.add(style);
            }
        }

        return Collections.unmodifiableList(result);
    }

    /*
     * Descobre style.json em todos os resource packs ativos.
     *
     * Exemplo de caminho:
     * assets/bubblifymod/bubble_styles/ocean_turtle/style.json
     */
    public static void discoverAndRegisterStyles(
            ResourceManager resourceManager
    ) {
        if (resourceManager == null) {
            return;
        }

        Map<ResourceLocation, Resource> foundStyles =
                resourceManager.listResources(
                        STYLE_DIRECTORY,
                        path -> path.getPath()
                                .endsWith("/style.json")
                );

        int loadedStyles = 0;

        for (Map.Entry<ResourceLocation, Resource> entry
                : foundStyles.entrySet()) {

            try {
                BubbleStyle style =
                        loadStyle(
                                resourceManager,
                                entry.getKey(),
                                entry.getValue()
                        );

                if (style == null) {
                    continue;
                }

                register(style);
                loadedStyles++;

                BubblifyMod.LOGGER.info(
                        "Estilo Bubblify '{}' carregado de {}",
                        style.getId(),
                        entry.getKey()
                );

            } catch (Exception exception) {
                BubblifyMod.LOGGER.error(
                        "Falha ao carregar estilo Bubblify '{}'.",
                        entry.getKey(),
                        exception
                );
            }
        }

        BubblifyMod.LOGGER.info(
                "Bubblify encontrou {} estilo(s) em resource packs.",
                loadedStyles
        );
    }

    private static BubbleStyle loadStyle(
            ResourceManager resourceManager,
            ResourceLocation definitionLocation,
            Resource definitionResource
    ) throws IOException {
        BubbleStyleDefinition definition;

        try (Reader reader =
                     new InputStreamReader(
                             definitionResource.open(),
                             StandardCharsets.UTF_8
                     )) {

            definition =
                    GSON.fromJson(
                            reader,
                            BubbleStyleDefinition.class
                    );
        }

        validateDefinition(
                definition,
                definitionLocation
        );

        String styleId =
                BubbleStyle.normalizeId(
                        definition.id
                );

        String styleDirectory =
                getParentDirectory(
                        definitionLocation.getPath()
                );

        ResourceLocation topLocation =
                createPartLocation(
                        definitionLocation.getNamespace(),
                        styleDirectory,
                        definition.top
                );

        ResourceLocation bodyLocation =
                createPartLocation(
                        definitionLocation.getNamespace(),
                        styleDirectory,
                        definition.body
                );

        ResourceLocation tailLocation =
                createPartLocation(
                        definitionLocation.getNamespace(),
                        styleDirectory,
                        definition.tail
                );

        ensureResourceExists(
                resourceManager,
                topLocation,
                "top"
        );

        ensureResourceExists(
                resourceManager,
                bodyLocation,
                "body"
        );

        ensureResourceExists(
                resourceManager,
                tailLocation,
                "tail"
        );

        BubbleStyle.Category category =
                parseCategory(
                        definition.category
                );

        String displayName =
                definition.displayName == null
                        || definition.displayName.isBlank()
                        ? styleId
                        : definition.displayName;

        return new BubbleStyle(
                styleId,
                displayName,
                category,
                false,
                topLocation,
                bodyLocation,
                tailLocation,
                definition.sourceWidth,
                definition.topHeight,
                definition.bodyHeight,
                definition.tailWidth,
                definition.tailHeight,
                definition.leftSlice,
                definition.rightSlice,
                definition.paddingLeft,
                definition.paddingRight,
                definition.paddingTop,
                definition.paddingBottom,
                definition.heightOffset
        );
    }

    private static void validateDefinition(
            BubbleStyleDefinition definition,
            ResourceLocation definitionLocation
    ) {
        if (definition == null) {
            throw new IllegalArgumentException(
                    "style.json vazio ou JSON inválido: "
                            + definitionLocation
            );
        }

        if (definition.id == null
                || definition.id.isBlank()) {
            throw new IllegalArgumentException(
                    "Campo obrigatório ausente: id."
            );
        }

        if (BubbleStyle.DEFAULT.getId().equalsIgnoreCase(
                definition.id
        )) {
            throw new IllegalArgumentException(
                    "O id 'default' é reservado."
            );
        }

        validateFileName(
                definition.top,
                "top"
        );

        validateFileName(
                definition.body,
                "body"
        );

        validateFileName(
                definition.tail,
                "tail"
        );

        if (definition.sourceWidth <= 0.0F) {
            throw new IllegalArgumentException(
                    "sourceWidth deve ser maior que zero."
            );
        }

        if (definition.topHeight <= 0.0F) {
            throw new IllegalArgumentException(
                    "topHeight deve ser maior que zero."
            );
        }

        if (definition.bodyHeight <= 0.0F) {
            throw new IllegalArgumentException(
                    "bodyHeight deve ser maior que zero."
            );
        }

        if (definition.tailWidth <= 0.0F) {
            throw new IllegalArgumentException(
                    "tailWidth deve ser maior que zero."
            );
        }

        if (definition.tailHeight <= 0.0F) {
            throw new IllegalArgumentException(
                    "tailHeight deve ser maior que zero."
            );
        }

        if (definition.leftSlice < 0.0F
                || definition.rightSlice < 0.0F) {
            throw new IllegalArgumentException(
                    "leftSlice e rightSlice não podem ser negativos."
            );
        }

        if (definition.leftSlice
                + definition.rightSlice
                >= definition.sourceWidth) {
            throw new IllegalArgumentException(
                    "leftSlice + rightSlice deve ser menor"
                            + " que sourceWidth."
            );
        }
    }

    /*
     * Protege contra '../', caminhos absolutos e arquivos externos.
     * Cada imagem deve permanecer na pasta do estilo.
     */
    private static void validateFileName(
            String fileName,
            String fieldName
    ) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException(
                    "Campo obrigatório ausente: "
                            + fieldName
            );
        }

        if (fileName.contains("..")
                || fileName.contains("\\")
                || fileName.startsWith("/")
                || fileName.startsWith(".")) {
            throw new IllegalArgumentException(
                    "Nome de arquivo inválido para "
                            + fieldName
                            + ": "
                            + fileName
            );
        }

        if (!fileName.endsWith(".png")) {
            throw new IllegalArgumentException(
                    "O arquivo de "
                            + fieldName
                            + " deve terminar em .png."
            );
        }
    }

    private static String getParentDirectory(
            String path
    ) {
        int lastSlash =
                path.lastIndexOf('/');

        if (lastSlash < 0) {
            return "";
        }

        return path.substring(
                0,
                lastSlash
        );
    }

    private static ResourceLocation createPartLocation(
            String namespace,
            String styleDirectory,
            String fileName
    ) {
        String fullPath =
                styleDirectory.isEmpty()
                        ? fileName
                        : styleDirectory
                          + "/"
                          + fileName;

        return ResourceLocation.fromNamespaceAndPath(
                namespace,
                fullPath
        );
    }

    private static void ensureResourceExists(
            ResourceManager resourceManager,
            ResourceLocation location,
            String partName
    ) {
        Optional<Resource> resource =
                resourceManager.getResource(location);

        if (resource.isEmpty()) {
            throw new IllegalArgumentException(
                    "Textura '"
                            + partName
                            + "' não encontrada: "
                            + location
            );
        }
    }

    private static BubbleStyle.Category parseCategory(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return BubbleStyle.Category.DEFAULT;
        }

        try {
            return BubbleStyle.Category.valueOf(
                    value.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Categoria inválida: '" + value
                            + "'. Use DEFAULT, ADMIN, NPC ou CHILDREN.",
                    exception
            );
        }
    }
}