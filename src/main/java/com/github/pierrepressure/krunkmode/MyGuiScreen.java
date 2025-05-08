package com.github.pierrepressure.krunkmode;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

public class MyGuiScreen extends GuiScreen {

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw the background tint
        drawDefaultBackground();

        // Find the last selected color
        int color = 0;
        if (lastClickedButton == 0) {
            color = 0xFFFF0000;
        } else if (lastClickedButton == 1) {
            color = 0xFF0000FF;
        } else if (lastClickedButton == 2) {
            color = 0xFF00FF00;
        }

        // Draw a colorful rectangle
        drawGradientRect(width / 2 - 65, height / 2 - 20, width / 2 + 65, height / 2 + 20, color, color);

        // Draw buttons
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    int lastClickedButton = 0;

    @Override
    public void initGui() {
        super.initGui();
        // Add buttons to the gui list during gui initialization
        this.buttonList.add(new GuiButton(0, width / 2 - 55, height / 2 - 10, 30, 20, "§cRED"));
        this.buttonList.add(new GuiButton(1, width / 2 - 15, height / 2 - 10, 30, 20, "§9BLUE"));
        this.buttonList.add(new GuiButton(2, width / 2 + 25, height / 2 - 10, 30, 20, "§2GREEN"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        // When a button is clicked saved that last id (or do something else based on the id)
        // You could change a setting here for example
        lastClickedButton = button.id;
    }


}