package com.github.pierrepressure.krunkmode;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

public class MyGuiScreen extends GuiScreen {

    // Width and height of our settings panel
    private int panelWidth = 220;
    private int panelHeight = 180;
    // Top-left corner of the panel
    private int panelX;
    private int panelY;
    // The formatted title string
    private final String title = "§l§nKrunk Mode Settings";

    @Override
    public void initGui() {
        super.initGui();

        // Compute panel top-left so it's centered on screen
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;

        // Button dimensions
        int buttonWidth = 150;
        int buttonHeight = 20;
        // Vertical position of the title (same as drawScreen uses)
        int titleY = panelY + 15;
        // Compute where the title starts so we can offset the close-button
        int titleWidth = fontRendererObj.getStringWidth(title);

        // Horizontal center of the screen
        int centerX = width / 2;
        // X position for the close '✕' button: just to the left of the title
        int closeX = (centerX - titleWidth / 2) - buttonHeight - 10;
        // Y position for the close '✕': vertically centered on the title line
        int closeY = titleY - (buttonHeight / 2);

        // Add close button (id = 3)
        this.buttonList.add(new GuiButton(3, closeX, closeY, buttonHeight, buttonHeight, "§c✕"));

        // Compute X so toggle buttons are centered within the panel
        int toggleX = panelX + (panelWidth - buttonWidth) / 2;
        // Start Y for the first toggle button, just below the title
        int firstToggleY = titleY + 30;
        // Vertical spacing between each toggle
        int spacing = 25;

        // Add three toggle buttons, id 0–2
        this.buttonList.add(new GuiButton(0, toggleX, firstToggleY + spacing * 0, buttonWidth, buttonHeight, "§6Amogus: Off"));
        this.buttonList.add(new GuiButton(1, toggleX, firstToggleY + spacing * 1, buttonWidth, buttonHeight, "§2Low Taper Fade: Off"));
        this.buttonList.add(new GuiButton(2, toggleX, firstToggleY + spacing * 2, buttonWidth, buttonHeight, "§dProperty in Egypt: Off"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw the darkened background behind our panel
        drawDefaultBackground();

        // Draw a semi-transparent gray rectangle for the panel
        drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xAA1A1A1A);

        // Draw the title centered at the top of our panel
        drawCenteredString(fontRendererObj, title, width / 2, panelY + 15, 0xFFFFFF);

        // Draw all buttons
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        // Toggle the first three buttons between On/Off with the proper color code
        if (button.id <= 2) {
            String[] colors = {"§6", "§b", "§d"};
            if (button.displayString.contains("Off")) {
                button.displayString = button.displayString.replace("Off", "On")
                        .replaceAll("§[0-9a-f]", colors[button.id]);
            } else {
                button.displayString = button.displayString.replace("On", "Off")
                        .replaceAll("§[0-9a-f]", colors[button.id]);
            }
        }

        // Close the GUI when '✕' is pressed
        if (button.id == 3) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        // Let the game continue running while this screen is open
        return false;
    }
}
