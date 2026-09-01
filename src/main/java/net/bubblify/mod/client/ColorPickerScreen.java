package net.bubblify.mod.client;

import net.bubblify.mod.network.ModMessages;
import net.bubblify.mod.network.UpdateColorPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ColorPickerScreen extends Screen {

    private static int savedColor = 0xFFFFFF;
    private static boolean savedTextWhite = true;

    private float red;
    private float green;
    private float blue;

    /*
     * true  = texto branco
     * false = texto preto
     */
    private boolean isTextWhite;

    private int currentColor;

    public ColorPickerScreen() {
        super(Component.translatable(
                "gui.bubblifymod.color_picker.title"
        ));

        loadSavedSelection();
    }

    private void loadSavedSelection() {
        this.currentColor = savedColor;

        this.red = ((savedColor >> 16) & 0xFF) / 255.0F;
        this.green = ((savedColor >> 8) & 0xFF) / 255.0F;
        this.blue = (savedColor & 0xFF) / 255.0F;

        this.isTextWhite = savedTextWhite;
    }

    @Override
    protected void init() {
        super.init();

        updateColorPreview();

        int centerX = this.width / 2;
        int startY = this.height / 2 - 20;

        addRedSlider(centerX, startY);
        addGreenSlider(centerX, startY + 25);
        addBlueSlider(centerX, startY + 50);
        addConfirmButton(centerX, startY + 115);
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
                                                (int) (this.value * 255.0D)
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
                                                (int) (this.value * 255.0D)
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
                                                (int) (this.value * 255.0D)
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

                            /*
                             * Salva a escolha no cache local da sessão.
                             */
                            savedColor = sanitizedColor;
                            savedTextWhite = this.isTextWhite;

                            /*
                             * Envia a configuração do jogador ao servidor:
                             * - Cor RGB da bolha.
                             * - Cor branca/preta do texto.
                             */
                            ModMessages.sendToServer(
                                    new UpdateColorPacket(
                                            sanitizedColor,
                                            this.isTextWhite
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
        int redValue = Math.round(this.red * 255.0F);
        int greenValue = Math.round(this.green * 255.0F);
        int blueValue = Math.round(this.blue * 255.0F);

        this.currentColor =
                (redValue << 16)
                        | (greenValue << 8)
                        | blueValue;
    }

    @Override
    public void onClose() {
        /*
         * Também salva no cache quando a pessoa fecha a tela por ESC.
         * Isso não envia nada ao servidor: somente confirmar aplica
         * a mudança global da bolha.
         */
        savedColor = this.currentColor & 0xFFFFFF;
        savedTextWhite = this.isTextWhite;

        super.onClose();
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        int leftX = this.width / 6;
        int startY = this.height / 2 - 20;
        int textColorY = startY + 25;

        if (button == 0) {
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
        int previewY = this.height / 2 - 80;
        int startY = this.height / 2 - 20;

        graphics.drawCenteredString(
                this.font,
                this.title,
                centerX,
                15,
                0xFFFFFF
        );

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

        /*
         * Borda da prévia.
         */
        graphics.fill(
                centerX - boxSize / 2 - 2,
                previewY - 2,
                centerX + boxSize / 2 + 2,
                previewY + boxSize + 2,
                0xFF000000
        );

        /*
         * Cor atual da bolha.
         */
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

        /*
         * Opção: texto preto.
         */
        graphics.fill(
                leftX - 25,
                textColorY,
                leftX - 5,
                textColorY + 20,
                0xFF000000
        );

        /*
         * Opção: texto branco.
         */
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

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
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