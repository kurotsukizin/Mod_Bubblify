package net.bubblify.mod.client;

import net.bubblify.mod.bubble.BubbleStyle;
import net.bubblify.mod.bubble.BubbleStyleRegistry;
import net.bubblify.mod.network.ModMessages;
import net.bubblify.mod.network.UpdateColorPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

public class ColorPickerScreen extends Screen {

    private static int savedColor = 0xFFFFFF;
    private static boolean savedTextWhite = true;

    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int STYLE_START_Y = 90;
    private static final int STYLE_SPACING = 24;

    private float red;
    private float green;
    private float blue;

    private boolean isTextWhite;
    private int currentColor;

    private Tab currentTab = Tab.COLOR;
    private BubbleStyle selectedStyle;

    private enum Tab {
        COLOR,
        STYLE
    }

    public ColorPickerScreen() {
        super(Component.translatable(
                "gui.bubblifymod.color_picker.title"
        ));

        loadSavedSelection();

        this.selectedStyle =
                BubbleStyleClientData.getSelectedStyle();

        if (this.selectedStyle == null) {
            this.selectedStyle = BubbleStyle.DEFAULT;
        }

        /*
         * Caso o jogador comum tenha ficado com algum estilo
         * especial salvo localmente, força default.
         */
        if (!BubbleStyleClientData.isStyleAllowed(
                this.selectedStyle
        )) {
            this.selectedStyle = BubbleStyle.DEFAULT;
        }
    }

    private void loadSavedSelection() {
        this.currentColor = savedColor;

        this.red =
                ((savedColor >> 16) & 0xFF) / 255.0F;

        this.green =
                ((savedColor >> 8) & 0xFF) / 255.0F;

        this.blue =
                (savedColor & 0xFF) / 255.0F;

        this.isTextWhite = savedTextWhite;
    }

    @Override
    protected void init() {
        super.init();

        updateColorPreview();

        /*
         * Nunca permite que a tela comece em Style
         * para quem não recebeu a permissão.
         */
        if (this.currentTab == Tab.STYLE
                && !BubbleStyleClientData.canUseStyleTab()) {
            this.currentTab = Tab.COLOR;
        }

        int centerX = this.width / 2;

        addTabButtons(centerX);

        if (this.currentTab == Tab.COLOR) {
            initColorTab(centerX);
        } else {
            initStyleTab(centerX);
        }

        addConfirmButton(
                centerX,
                this.height - 35
        );
    }

    private void addTabButtons(int centerX) {
        int tabY = 35;

        /*
         * Para jogadores comuns, Color fica centralizado.
         * Para quem pode usar Style, os dois botões aparecem.
         */
        int colorTabX =
                BubbleStyleClientData.canUseStyleTab()
                        ? centerX - 105
                        : centerX - 50;

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Color"),
                        button -> switchTab(Tab.COLOR)
                ).bounds(
                        colorTabX,
                        tabY,
                        100,
                        20
                ).build()
        );

        if (BubbleStyleClientData.canUseStyleTab()) {
            this.addRenderableWidget(
                    Button.builder(
                            Component.literal("Style"),
                            button -> switchTab(Tab.STYLE)
                    ).bounds(
                            centerX + 5,
                            tabY,
                            100,
                            20
                    ).build()
            );
        }
    }

    private void switchTab(Tab tab) {
        if (tab == Tab.STYLE
                && !BubbleStyleClientData.canUseStyleTab()) {
            return;
        }

        if (this.currentTab == tab) {
            return;
        }

        this.currentTab = tab;
        this.rebuildWidgets();
    }

    private void initColorTab(int centerX) {
        int startY = this.height / 2 - 20;

        addRedSlider(centerX, startY);
        addGreenSlider(centerX, startY + 25);
        addBlueSlider(centerX, startY + 50);
    }

    private void initStyleTab(int centerX) {
        /*
         * Lista somente os IDs permitidos pelo servidor.
         *
         * O registry continua importante porque o cliente só
         * consegue criar BubbleStyle para texturas que ele possui.
         */
        List<BubbleStyle> allowedStyles =
                BubbleStyleClientData.getAllowedStyles();

        for (int index = 0;
             index < allowedStyles.size();
             index++) {

            BubbleStyle style = allowedStyles.get(index);

            this.addRenderableWidget(
                    Button.builder(
                            Component.literal(
                                    formatStyleName(
                                            style.getId()
                                    )
                            ),
                            button -> selectStyle(style)
                    ).bounds(
                            centerX - BUTTON_WIDTH / 2,
                            STYLE_START_Y
                                    + (index * STYLE_SPACING),
                            BUTTON_WIDTH,
                            BUTTON_HEIGHT
                    ).build()
            );
        }
    }

    private void selectStyle(BubbleStyle style) {
        if (style == null
                || !BubbleStyleClientData.isStyleAllowed(
                style
        )) {
            return;
        }

        this.selectedStyle = style;

        BubbleStyleClientData.setSelectedStyle(
                style
        );

        this.rebuildWidgets();
    }

    private void addRedSlider(int centerX, int y) {
        this.addRenderableWidget(
                new AbstractSliderButton(
                        centerX - 75,
                        y,
                        150,
                        20,
                        Component.empty(),
                        this.red
                ) {
                    {
                        this.updateMessage();
                    }

                    @Override
                    protected void updateMessage() {
                        this.setMessage(
                                Component.translatable(
                                        "gui.bubblifymod.color_picker.red"
                                ).append(": ").append(
                                        String.valueOf(
                                                (int) (
                                                        this.value
                                                                * 255.0D
                                                )
                                        )
                                )
                        );
                    }

                    @Override
                    protected void applyValue() {
                        red = (float) this.value;
                        updateColorPreview();
                    }
                }
        );
    }

    private void addGreenSlider(int centerX, int y) {
        this.addRenderableWidget(
                new AbstractSliderButton(
                        centerX - 75,
                        y,
                        150,
                        20,
                        Component.empty(),
                        this.green
                ) {
                    {
                        this.updateMessage();
                    }

                    @Override
                    protected void updateMessage() {
                        this.setMessage(
                                Component.translatable(
                                        "gui.bubblifymod.color_picker.green"
                                ).append(": ").append(
                                        String.valueOf(
                                                (int) (
                                                        this.value
                                                                * 255.0D
                                                )
                                        )
                                )
                        );
                    }

                    @Override
                    protected void applyValue() {
                        green = (float) this.value;
                        updateColorPreview();
                    }
                }
        );
    }

    private void addBlueSlider(int centerX, int y) {
        this.addRenderableWidget(
                new AbstractSliderButton(
                        centerX - 75,
                        y,
                        150,
                        20,
                        Component.empty(),
                        this.blue
                ) {
                    {
                        this.updateMessage();
                    }

                    @Override
                    protected void updateMessage() {
                        this.setMessage(
                                Component.translatable(
                                        "gui.bubblifymod.color_picker.blue"
                                ).append(": ").append(
                                        String.valueOf(
                                                (int) (
                                                        this.value
                                                                * 255.0D
                                                )
                                        )
                                )
                        );
                    }

                    @Override
                    protected void applyValue() {
                        blue = (float) this.value;
                        updateColorPreview();
                    }
                }
        );
    }

    private void addConfirmButton(int centerX, int y) {
        this.addRenderableWidget(
                Button.builder(
                        Component.translatable(
                                "gui.bubblifymod.color_picker.confirmed"
                        ),
                        button -> {
                            int sanitizedColor =
                                    this.currentColor & 0xFFFFFF;

                            savedColor = sanitizedColor;
                            savedTextWhite = this.isTextWhite;

                            BubbleStyle finalStyle =
                                    this.selectedStyle;

                            if (finalStyle == null
                                    || !BubbleStyleClientData
                                    .isStyleAllowed(finalStyle)) {
                                finalStyle = BubbleStyle.DEFAULT;
                            }

                            this.selectedStyle = finalStyle;

                            BubbleStyleClientData.setSelectedStyle(
                                    finalStyle
                            );

                            ModMessages.sendToServer(
                                    new UpdateColorPacket(
                                            sanitizedColor,
                                            this.isTextWhite,
                                            finalStyle.getId()
                                    )
                            );

                            if (this.minecraft != null) {
                                this.minecraft.setScreen(null);
                            }
                        }
                ).bounds(
                        centerX - 50,
                        y,
                        100,
                        20
                ).build()
        );
    }

    private void updateColorPreview() {
        int redValue =
                Math.round(this.red * 255.0F);

        int greenValue =
                Math.round(this.green * 255.0F);

        int blueValue =
                Math.round(this.blue * 255.0F);

        this.currentColor =
                (redValue << 16)
                        | (greenValue << 8)
                        | blueValue;
    }

    @Override
    public void onClose() {
        savedColor = this.currentColor & 0xFFFFFF;
        savedTextWhite = this.isTextWhite;

        BubbleStyle finalStyle = this.selectedStyle;

        if (finalStyle == null
                || !BubbleStyleClientData.isStyleAllowed(
                finalStyle
        )) {
            finalStyle = BubbleStyle.DEFAULT;
        }

        this.selectedStyle = finalStyle;

        BubbleStyleClientData.setSelectedStyle(
                finalStyle
        );

        super.onClose();
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (this.currentTab == Tab.COLOR
                && button == 0) {

            int leftX = this.width / 6;
            int startY = this.height / 2 - 20;
            int textColorY = startY + 25;

            boolean blackBoxClicked =
                    mouseX >= leftX - 25
                            && mouseX <= leftX - 5
                            && mouseY >= textColorY
                            && mouseY <= textColorY + 20;

            if (blackBoxClicked) {
                this.isTextWhite = false;
                return true;
            }

            boolean whiteBoxClicked =
                    mouseX >= leftX + 5
                            && mouseX <= leftX + 25
                            && mouseY >= textColorY
                            && mouseY <= textColorY + 20;

            if (whiteBoxClicked) {
                this.isTextWhite = true;
                return true;
            }
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        this.renderBackground(graphics);

        int centerX = this.width / 2;

        graphics.drawCenteredString(
                this.font,
                this.title,
                centerX,
                15,
                0xFFFFFF
        );

        renderTabState(graphics, centerX);

        if (this.currentTab == Tab.COLOR) {
            renderColorTab(graphics, centerX);
        } else {
            renderStyleTab(graphics, centerX);
        }

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    private void renderTabState(
            GuiGraphics graphics,
            int centerX
    ) {
        int tabY = 35;
        int selectedOutlineColor = 0xFF00FF00;

        if (!BubbleStyleClientData.canUseStyleTab()) {
            graphics.renderOutline(
                    centerX - 51,
                    tabY - 1,
                    102,
                    22,
                    selectedOutlineColor
            );
            return;
        }

        if (this.currentTab == Tab.COLOR) {
            graphics.renderOutline(
                    centerX - 106,
                    tabY - 1,
                    102,
                    22,
                    selectedOutlineColor
            );
        } else {
            graphics.renderOutline(
                    centerX + 4,
                    tabY - 1,
                    102,
                    22,
                    selectedOutlineColor
            );
        }
    }

    private void renderColorTab(
            GuiGraphics graphics,
            int centerX
    ) {
        int previewY = this.height / 2 - 80;
        int startY = this.height / 2 - 20;

        graphics.drawCenteredString(
                this.font,
                Component.translatable(
                        "gui.bubblifymod.color_picker.choose"
                ),
                centerX,
                previewY - 15,
                0xAAAAAA
        );

        int boxSize = 40;

        graphics.fill(
                centerX - boxSize / 2 - 2,
                previewY - 2,
                centerX + boxSize / 2 + 2,
                previewY + boxSize + 2,
                0xFF000000
        );

        graphics.fill(
                centerX - boxSize / 2,
                previewY,
                centerX + boxSize / 2,
                previewY + boxSize,
                0xFF000000 | this.currentColor
        );

        int leftX = this.width / 6;
        int textColorY = startY + 25;

        graphics.drawCenteredString(
                this.font,
                Component.translatable(
                        "gui.bubblifymod.color_picker.text_color"
                ),
                leftX,
                textColorY - 15,
                0xFFFFFF
        );

        graphics.fill(
                leftX - 25,
                textColorY,
                leftX - 5,
                textColorY + 20,
                0xFF000000
        );

        graphics.fill(
                leftX + 5,
                textColorY,
                leftX + 25,
                textColorY + 20,
                0xFFFFFFFF
        );

        int selectedOutlineColor = 0xFF00FF00;

        if (this.isTextWhite) {
            graphics.renderOutline(
                    leftX + 4,
                    textColorY - 1,
                    22,
                    22,
                    selectedOutlineColor
            );
        } else {
            graphics.renderOutline(
                    leftX - 26,
                    textColorY - 1,
                    22,
                    22,
                    selectedOutlineColor
            );
        }
    }

    private void renderStyleTab(
            GuiGraphics graphics,
            int centerX
    ) {
        BubbleStyle style = this.selectedStyle;

        if (style == null
                || !BubbleStyleClientData.isStyleAllowed(
                style
        )) {
            style = BubbleStyle.DEFAULT;
        }

        graphics.drawCenteredString(
                this.font,
                Component.literal("Selected Style"),
                centerX,
                65,
                0xFFFFFF
        );

        graphics.drawCenteredString(
                this.font,
                Component.literal(
                        formatStyleName(style.getId())
                ),
                centerX,
                78,
                0xAAAAAA
        );

        List<BubbleStyle> allowedStyles =
                BubbleStyleClientData.getAllowedStyles();

        int selectedOutlineColor = 0xFF00FF00;

        for (int index = 0;
             index < allowedStyles.size();
             index++) {

            BubbleStyle availableStyle =
                    allowedStyles.get(index);

            if (!availableStyle.getId().equals(
                    style.getId()
            )) {
                continue;
            }

            graphics.renderOutline(
                    centerX - BUTTON_WIDTH / 2 - 1,
                    STYLE_START_Y
                            + (index * STYLE_SPACING) - 1,
                    BUTTON_WIDTH + 2,
                    BUTTON_HEIGHT + 2,
                    selectedOutlineColor
            );
        }
    }

    private String formatStyleName(String styleId) {
        if (styleId == null || styleId.isBlank()) {
            return "Default";
        }

        String[] words = styleId.split("_");
        StringBuilder formattedName = new StringBuilder();

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }

            if (!formattedName.isEmpty()) {
                formattedName.append(" ");
            }

            formattedName.append(
                    Character.toUpperCase(
                            word.charAt(0)
                    )
            );

            if (word.length() > 1) {
                formattedName.append(
                        word.substring(1)
                                .toLowerCase(Locale.ROOT)
                );
            }
        }

        return formattedName.isEmpty()
                ? "Default"
                : formattedName.toString();
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        graphics.fillGradient(
                0,
                0,
                this.width,
                this.height,
                0xC0000000,
                0xD0000000
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}   