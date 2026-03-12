package net.bubblify.mod.client;

import net.bubblify.mod.BubblifyConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.bubblify.mod.network.ModMessages;
import net.bubblify.mod.network.UpdateColorPacket;

public class ColorPickerScreen extends Screen {

    private float red;
    private float green;
    private float blue;

    private int currentColor;

    public ColorPickerScreen() {
        super(Component.translatable("gui.bubblifymod.color_picker.title"));
    }

    @Override
    protected void init() {
        super.init();

        this.red = BubblifyConfig.RED.get() / 255.0F;
        this.green = BubblifyConfig.GREEN.get() / 255.0F;
        this.blue = BubblifyConfig.BLUE.get() / 255.0F;
        updateColorPreview();

        int centerX = this.width / 2;
        int startY = this.height / 2 - 20;

        this.addRenderableWidget(new AbstractSliderButton(centerX - 75, startY, 150, 20, Component.empty(), this.red) {
            {
                this.updateMessage();
            }
            @Override
            protected void updateMessage() {
                this.setMessage(Component.translatable("gui.bubblifymod.color_picker.red").append(": " + (int)(this.value * 255)));
            }
            @Override
            protected void applyValue() {
                red = (float) this.value;
                updateColorPreview();
            }
        });

        this.addRenderableWidget(new AbstractSliderButton(centerX - 75, startY + 25, 150, 20, Component.empty(), this.green) {
            {
                this.updateMessage();
            }
            @Override
            protected void updateMessage() {
                this.setMessage(Component.translatable("gui.bubblifymod.color_picker.green").append(": " + (int)(this.value * 255)));
            }
            @Override
            protected void applyValue() {
                green = (float) this.value;
                updateColorPreview();
            }
        });

        this.addRenderableWidget(new AbstractSliderButton(centerX - 75, startY + 50, 150, 20, Component.empty(), this.blue) {
            {
                this.updateMessage();
            }
            @Override
            protected void updateMessage() {
                this.setMessage(Component.translatable("gui.bubblifymod.color_picker.blue").append(": " + (int)(this.value * 255)));
            }
            @Override
            protected void applyValue() {
                blue = (float) this.value;
                updateColorPreview();
            }
        });

        this.addRenderableWidget(Button.builder(Component.translatable("gui.bubblifymod.color_picker.confirmed"), button -> {

            salvarConfiguracao();

            ModMessages.sendToServer(new UpdateColorPacket(this.currentColor));
            this.minecraft.setScreen(null);
        }).bounds(centerX - 50, startY + 85, 100, 20).build());
    }

    private void updateColorPreview() {
        int r = (int) (this.red * 255);
        int g = (int) (this.green * 255);
        int b = (int) (this.blue * 255);
        this.currentColor = (r << 16) | (g << 8) | b;
    }


    private void salvarConfiguracao() {
        BubblifyConfig.RED.set((int) (this.red * 255));
        BubblifyConfig.GREEN.set((int) (this.green * 255));
        BubblifyConfig.BLUE.set((int) (this.blue * 255));
        BubblifyConfig.SPEC.save();
    }


    @Override
    public void onClose() {
        salvarConfiguracao();
        super.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int centerX = this.width / 2;
        int previewY = this.height / 2 - 80;

        graphics.drawCenteredString(this.font, this.title, centerX, 15, 0xFFFFFF);

        graphics.drawCenteredString(this.font, Component.translatable("gui.bubblifymod.color_picker.choose"), centerX, previewY - 15, 0xAAAAAA);

        int boxSize = 40;
        graphics.fill(centerX - boxSize/2 - 2, previewY - 2, centerX + boxSize/2 + 2, previewY + boxSize + 2, 0xFF000000);
        graphics.fill(centerX - boxSize/2, previewY, centerX + boxSize/2, previewY + boxSize, 0xFF000000 | this.currentColor);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        graphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}