package com.github.pierrepressure.krunkmode;

import com.github.pierrepressure.krunkmode.features.farming.FarmCrop;
import com.github.pierrepressure.krunkmode.features.ClickerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;
import java.util.Arrays;

public class KrunkMenu extends GuiScreen {

    // Minecraft game instance for accessing game settings and rendering
    private final Minecraft mc = Minecraft.getMinecraft();

    // Dimensions of the settings panel
    private final int panelWidth = 220;
    private final int panelHeight = 200;

    // Calculated X and Y position for centering the panel
    private int panelX;
    private int panelY;

    // Title text displayed at the top of the panel
    private final String title = "§6§lKrunk Mode Settings";

    // Text field for user to input number of loops
    private GuiTextField numLoopsField;
    private GuiTextField maxCpsField;
    private GuiTextField minCpsField;

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
        int spacing = 50;

        // Calculate position for the close (X) button based on title width
        int titleWidth = fontRendererObj.getStringWidth(title);
        int titleY = panelY + 20;
        int closeX = (width / 2 - titleWidth / 2) - buttonHeight - 10;
        int closeY = titleY - (buttonHeight / 2);

        // Add a close button to the GUI
        this.buttonList.add(new GuiButton(0, closeX, closeY, buttonHeight, buttonHeight, "§c§lX"));
        this.buttonList.add(new GuiButton(1, (width / 2 - titleWidth / 2) + titleWidth + 10, closeY, buttonHeight, buttonHeight, "§b§lⓘ"));

        // Calculate positions for toggle buttons in a column
        int toggleX = panelX + (panelWidth - buttonWidth) / 2;
        int firstToggleY = titleY + spacing - 10;

        // Position for the number-of-loops text field and apply button
        int fieldWidth = (buttonWidth - 10) / 2;

        // Initialize number-of-loops text field - now using the config system
        this.numLoopsField = new GuiTextField(2, this.fontRendererObj, toggleX, firstToggleY, fieldWidth, buttonHeight);
        numLoopsField.setMaxStringLength(4);
        numLoopsField.setValidator(s -> s.matches("\\d*"));
        numLoopsField.setText(String.valueOf(FarmCrop.getAutoPauseLoops()));

        // Add an "Autoplay" button next to the text field - now using config system status
        String autoPlayLabel = FarmCrop.isAutoPlayEnabled() ? "§aAutoPlay" : "§cAutoPlay";
        this.buttonList.add(new GuiButton(3, toggleX + fieldWidth + 5, firstToggleY, fieldWidth, buttonHeight, autoPlayLabel));

        // Position for CPS fields below existing elements
        int cpsY = titleY + spacing * 2; // Adjust spacing as needed

        // Max CPS Text Field
        this.maxCpsField = new GuiTextField(4, this.fontRendererObj, toggleX + fieldWidth + 5, cpsY, fieldWidth, buttonHeight);
        maxCpsField.setMaxStringLength(2);
        maxCpsField.setValidator(s -> s.matches("\\d*"));
        maxCpsField.setText(String.valueOf(ClickerManager.getMaxCps()));

        // Min CPS Text Field
        this.minCpsField = new GuiTextField(5, this.fontRendererObj, toggleX, cpsY, fieldWidth, buttonHeight);
        minCpsField.setMaxStringLength(2);
        minCpsField.setValidator(s -> s.matches("\\d*"));
        minCpsField.setText(String.valueOf(ClickerManager.getMinCps()));

        // Add an "Apply" button to the bottom
        this.buttonList.add(new GuiButton(6, toggleX, titleY + (spacing * 3) - 10, buttonWidth, buttonHeight, "Apply Changes"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw panel rectangle with semi-transparent fill
        drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xAA1A1A1A);

        // Draw centered title string at top of panel
        drawCenteredString(fontRendererObj, title, width / 2, panelY + 15, 0xFFFFFF);


        String label = "Farming Settings";
        fontRendererObj.drawString(label, panelX + (panelWidth - 150) / 2, numLoopsField.yPosition - 15, 0xFFFFFF);

        // Render the text field itself
        numLoopsField.drawTextBox();

        // Draw CPS labels
        String maxLabel = "Clicker CPS Settings";
        int labelX = panelX + (panelWidth - 150) / 2;
        fontRendererObj.drawString(maxLabel, labelX, maxCpsField.yPosition - 15, 0xFFFFFF);

        // Render CPS text fields
        maxCpsField.drawTextBox();
        minCpsField.drawTextBox();

        // Render buttons and other GUI elements
        super.drawScreen(mouseX, mouseY, partialTicks);

        // If hovering over the text field, show tooltip with instructions
        if (isMouseHovering(mouseX, mouseY, numLoopsField.xPosition, numLoopsField.yPosition, numLoopsField.width, numLoopsField.height)) {
            this.drawHoveringText(Arrays.asList("Number of Loops Before Pausing", "§o(0 = infinity)"), mouseX, mouseY);
        }

        // Tooltips
        if (isMouseHovering(mouseX, mouseY, maxCpsField.xPosition, maxCpsField.yPosition, maxCpsField.width, maxCpsField.height)) {
            drawHoveringText(Arrays.asList("Maximum clicks per second", "§c§o(Stay under 12 just to be safe)"), mouseX, mouseY);
        }
        if (isMouseHovering(mouseX, mouseY, minCpsField.xPosition, minCpsField.yPosition, minCpsField.width, minCpsField.height)) {
            drawHoveringText(Arrays.asList("Minimum clicks per second", "§8§o(must be less than max)"), mouseX, mouseY);
        }

        //If Hovering Over Certain Buttons
        for (GuiButton button : this.buttonList) {
            if (isMouseHovering(mouseX, mouseY, button.xPosition, button.yPosition, button.width, button.height)) {
                switch (button.id) {

                    case 0: //Hovering Over Delete Button
                        this.drawHoveringText(Arrays.asList("Close"), mouseX, mouseY);
                        break;

                    case 1: //Hovering Over Info Button
                        this.drawHoveringText(Arrays.asList("Information", "click to run /khelp for ", "a full list of commands"), mouseX, mouseY);
                        break;

                    case 3: // Hovering over "AutoPlay" button
                        this.drawHoveringText(Arrays.asList("Toggle Automatically Resuming", "After Closing Chat"), mouseX, mouseY);
                        break;

                    case 6: //Hovering Over Apply Button
                        this.drawHoveringText(Arrays.asList("Apply Your Setting Changes"), mouseX, mouseY);
                        break;


                }
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {

        switch (button.id) {
            case 0: //Clicked On Delete Button
                mc.displayGuiScreen(null);
                break;

            case 1: //Clicked On Info Button
                net.minecraftforge.client.ClientCommandHandler.instance.executeCommand(mc.thePlayer, "/khelp");
                break;

            case 3: //Clicked On Autoplay Button
                FarmCrop.toggleAutoPlay();
                button.displayString = FarmCrop.isAutoPlayEnabled() ? "§aAutoPlay" : "§cAutoPlay";
                break;

            case 6: // Clicked On "Apply" button
                applyNumLoops();
                applyCpsSettings();
                break;
        }

    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        // Pass key event to parent for basic handling (e.g. ESC)
        super.keyTyped(typedChar, keyCode);

        // Forward key event to text field for input
        numLoopsField.textboxKeyTyped(typedChar, keyCode);
        maxCpsField.textboxKeyTyped(typedChar, keyCode);
        minCpsField.textboxKeyTyped(typedChar, keyCode);

    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // Pass click event to parent (handles buttons)
        super.mouseClicked(mouseX, mouseY, mouseButton);
        // Also send click event to text field to focus it
        numLoopsField.mouseClicked(mouseX, mouseY, mouseButton);
        maxCpsField.mouseClicked(mouseX, mouseY, mouseButton);
        minCpsField.mouseClicked(mouseX, mouseY, mouseButton);
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

            if (FarmCrop.getAutoPauseLoops() != num) FarmCrop.setAutoPauseLoops(num);
        } catch (NumberFormatException e) {
            // Invalid input ignored; no crash
        }
    }

    private void applyCpsSettings() {
        try {
            int max = Integer.parseInt(maxCpsField.getText());
            max = Math.min(max, 20);

            int min = Integer.parseInt(minCpsField.getText());
            min = Math.min(Math.max(max - 1, 0), min);

            // Ensure min <= max
            if (min > max) {
                maxCpsField.setText(String.valueOf(max));
                minCpsField.setText(String.valueOf(min));
            }

            // Update ClickerManager and Config
            if (ClickerManager.getMaxCps() != max) ClickerManager.setMaxCps(max);
            if (ClickerManager.getMinCps() != min) ClickerManager.setMinCps(min);

        } catch (NumberFormatException e) {
            // Handle invalid input
        }
    }

    /**
     * Checks if the mouse is hovering over a rectangular GUI element
     */
    private boolean isMouseHovering(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}