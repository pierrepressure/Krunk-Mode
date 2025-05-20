package com.github.pierrepressure.krunkmode;

import com.github.pierrepressure.krunkmode.features.FarmCrop;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class KrunkMenu extends GuiScreen {

    // Minecraft game instance for accessing game settings and rendering
    private final Minecraft mc = Minecraft.getMinecraft();

    // Dimensions of the settings panel
    private final int panelWidth = 220;
    private final int panelHeight = 180;

    // Calculated X and Y position for centering the panel
    private int panelX;
    private int panelY;

    // Title text displayed at the top of the panel
    private final String title = "§l§nKrunk Mode Settings";

    // Text field for user to input number of loops
    private GuiTextField numLoopsField;

    @Override
    public void initGui() {
        // Call parent method to ensure Minecraft GUI state is initialized
        super.initGui();

        // Center the panel on the screen
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;

        // Common button dimensions
        int buttonWidth = 150;
        int buttonHeight = 20;

        // Calculate position for the close (X) button based on title width
        int titleWidth = fontRendererObj.getStringWidth(title);
        int titleY = panelY + 15;
        int closeX = (width / 2 - titleWidth / 2) - buttonHeight - 10;
        int closeY = titleY - (buttonHeight / 2);

        // Add a close button to the GUI
        this.buttonList.add(new GuiButton(3, closeX, closeY, buttonHeight, buttonHeight, "§cX"));

        // Calculate positions for toggle buttons in a column
        int toggleX = panelX + (panelWidth - buttonWidth) / 2;
        int firstToggleY = titleY + 30;
        int spacing = 25;

        // Add three feature toggle buttons with default "Off" state
        this.buttonList.add(new GuiButton(0, toggleX, firstToggleY + spacing * 0, buttonWidth, buttonHeight, "§6Amogus: Off"));
        this.buttonList.add(new GuiButton(1, toggleX, firstToggleY + spacing * 1, buttonWidth, buttonHeight, "§2Low Taper Fade: Off"));
        this.buttonList.add(new GuiButton(2, toggleX, firstToggleY + spacing * 2, buttonWidth, buttonHeight, "§dProperty in Egypt: Off"));

        // Position for the number-of-loops text field and apply button
        int textFieldY = firstToggleY + spacing * 4;
        int fieldWidth = 40;
        int applyButtonWidth = 45;
        int autoplayButtonWidth = 60;

        // Initialize number-of-loops text field - now using the config system
        this.numLoopsField = new GuiTextField(4, this.fontRendererObj, toggleX, textFieldY, fieldWidth, buttonHeight);
        numLoopsField.setMaxStringLength(4);
        numLoopsField.setValidator(s -> s.matches("\\d*"));
        numLoopsField.setText(String.valueOf(FarmCrop.getAutoPauseLoops()));

        // Add an "Apply" button next to the text field
        this.buttonList.add(new GuiButton(5, toggleX + fieldWidth + 5, textFieldY, applyButtonWidth, buttonHeight, "Apply"));

        // Add an "Autoplay" button next to the text field - now using config system status
        String autoPlayLabel = FarmCrop.isAutoPlayEnabled() ? "§aAutoPlay" : "§cAutoPlay";
        this.buttonList.add(new GuiButton(6, toggleX + fieldWidth + 5 + applyButtonWidth + 5, textFieldY, autoplayButtonWidth, buttonHeight, autoPlayLabel));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw panel rectangle with semi-transparent fill
        drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xAA1A1A1A);

        // Draw centered title string at top of panel
        drawCenteredString(fontRendererObj, title, width / 2, panelY + 15, 0xFFFFFF);

        // Draw label for the number-of-loops field
        int labelY = panelY + 45 + 25 * 4 - 10;
        String label = "Melon Settings";
        fontRendererObj.drawString(label, panelX + (panelWidth - 150) / 2, labelY, 0xFFFFFF);

        // Render the text field itself
        numLoopsField.drawTextBox();

        // Render buttons and other GUI elements
        super.drawScreen(mouseX, mouseY, partialTicks);

        // If hovering over the text field, show tooltip with instructions
        if (isMouseHovering(mouseX, mouseY, numLoopsField.xPosition, numLoopsField.yPosition, numLoopsField.width, numLoopsField.height)) {
            List<String> tooltip = Arrays.asList("Number of Loops Before Pausing","(0 = infinity)");
            this.drawHoveringText(tooltip, mouseX, mouseY);
        }

        //If Hovering Over Certain Buttons
        for (GuiButton button : this.buttonList) {
            if (isMouseHovering(mouseX, mouseY, button.xPosition, button.yPosition, button.width, button.height)) {
                switch (button.id) {
                    case 5: //Hovering Over Apply Button
                        this.drawHoveringText(Arrays.asList("Apply Changes"), mouseX, mouseY);
                        break;

                    case 6: // Hovering over "AutoPlay" button
                        this.drawHoveringText(Arrays.asList("Toggle Automatically Resuming", "After Closing Chat"), mouseX, mouseY);
                        break;
                }
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        // Handle toggle buttons (IDs 0-2)
        if (button.id <= 2) {
            String[] colors = {"§6", "§2", "§d"};
            // Flip display from Off to On or vice versa
            if (button.displayString.contains("Off")) {
                button.displayString = button.displayString.replace("Off", "On").replaceAll("§[0-9a-f]", colors[button.id]);
            } else {
                button.displayString = button.displayString.replace("On", "Off").replaceAll("§[0-9a-f]", colors[button.id]);
            }
        }
        // Handle close button (ID 3)
        else if (button.id == 3) {
            mc.displayGuiScreen(null);
        }
        // Handle apply button for number-of-loops (ID 5)
        else if (button.id == 5) {
            applyNumLoops();
        }
        else if (button.id == 6) {
            FarmCrop.toggleAutoPlay();
            button.displayString = FarmCrop.isAutoPlayEnabled() ? "§aAutoPlay" : "§cAutoPlay";
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        // Pass key event to parent for basic handling (e.g. ESC)
        super.keyTyped(typedChar, keyCode);
        // Forward key event to text field for input
        numLoopsField.textboxKeyTyped(typedChar, keyCode);
        // If Enter is pressed, apply the number-of-loops immediately
        if (keyCode == 28 || keyCode == 156) {
            applyNumLoops();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // Pass click event to parent (handles buttons)
        super.mouseClicked(mouseX, mouseY, mouseButton);
        // Also send click event to text field to focus it
        numLoopsField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean doesGuiPauseGame() {
        // Keep the game running in the background while GUI is open
        return false;
    }

    /**
     * Reads the text field and updates the farming loops setting
     */
    private void applyNumLoops() {
        try {
            // Parse number, default to 0 if empty
            int num = numLoopsField.getText().isEmpty() ? 0 : Integer.parseInt(numLoopsField.getText());
            FarmCrop.setAutoPauseLoops(num);
        } catch (NumberFormatException e) {
            // Invalid input ignored; no crash
        }
    }

    /**
     * Checks if the mouse is hovering over a rectangular GUI element
     */
    private boolean isMouseHovering(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}