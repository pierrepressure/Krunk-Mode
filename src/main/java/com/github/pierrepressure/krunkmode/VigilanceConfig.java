package com.github.pierrepressure.krunkmode;

import com.github.pierrepressure.krunkmode.commands.HelpCommand;
import gg.essential.universal.UChat;
import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyType;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.io.File;
import java.net.URI;

public class VigilanceConfig extends Vigilant {

    // Farming settings - matching your existing config
    @Property(
            type = PropertyType.SLIDER,
            name = "Auto Pause Loops",
            description = "Number of farming loops before pausing (0 = infinite)",
            category = "Settings",
            subcategory = "Farming",
            min = 0,
            max = 20
    )
    public int autoPauseLoops = 0;

    @Property(
            type = PropertyType.SWITCH,
            name = "Auto Play",
            description = "Automatically resume after pause",
            category = "Settings",
            subcategory = "Farming"
    )
    public boolean autoPlay = false;


    // Clicker settings - matching your existing config
    @Property(
            type = PropertyType.SLIDER,
            name = "Max CPS",
            description = "Maximum clicks per second for the clicker \n§c⚠ Stay below 11 to avoid triggering anti-cheat. ",
            category = "Settings",
            min = 1,
            max = 20,
            subcategory = "Clicker"
    )
    public int maxCps = 10;

    @Property(
            type = PropertyType.SLIDER,
            name = "Min CPS",
            description = "Minimum clicks per second for the clicker",
            category = "Settings",
            min = 1,
            max = 20,
            subcategory = "Clicker"
    )
    public int minCps = 6;


    // Fishing delay settings
    @Property(
            type = PropertyType.SLIDER,
            name = "Min Reel Delay",
            description = "Minimum delay in milliseconds before reeling after bite\n&8(Default: 200)",
            category = "Settings",
            subcategory = "Fishing §o(Dont Change Unless Something is Broken)",
            min = 50,
            max = 500,
            increment = 25
    )
    public int minReelDelay = 200;

    @Property(
            type = PropertyType.SLIDER,
            name = "Max Reel Delay",
            description = "Maximum delay in milliseconds before reeling after bite\n&8(Default: 400)",
            category = "Settings",
            subcategory = "Fishing §o(Dont Change Unless Something is Broken)",
            min = 100,
            max = 1000,
            increment = 25
    )
    public int maxReelDelay = 400;

    @Property(
            type = PropertyType.SLIDER,
            name = "Min Cast Delay",
            description = "Minimum delay in milliseconds before recasting\n&8(Default: 700)",
            category = "Settings",
            subcategory = "Fishing §o(Dont Change Unless Something is Broken)",
            min = 300,
            max = 1500,
            increment = 25
    )
    public int minCastDelay = 700;

    @Property(
            type = PropertyType.SLIDER,
            name = "Max Cast Delay",
            description = "Maximum delay in milliseconds before recasting\n&8(Default: 1000)",
            category = "Settings",
            subcategory = "Fishing §o(Dont Change Unless Something is Broken)",
            min = 500,
            max = 2000,
            increment = 25
    )
    public int maxCastDelay = 1000;

    // Auto Experiments settings
    @Property(
            type = PropertyType.SWITCH,
            name = "Enable Auto Experiments",
            description = "Automatically solve Chronomatron and Ultrasequencer experiments",
            category = "Settings",
            subcategory = "Experiments"
    )
    public boolean autoExperimentsEnabled = false;

    @Property(
            type = PropertyType.SLIDER,
            name = "Click Delay",
            description = "Time in milliseconds between automatic experiment clicks\n&8(Default: 500)",
            category = "Settings",
            subcategory = "Experiments",
            min = 200,
            max = 1000,
            increment = 25,
            decimalPlaces = 0
    )
    public int experimentClickDelay = 500;

    @Property(
            type = PropertyType.SWITCH,
            name = "Auto Close Experiments",
            description = "Automatically close the GUI after completing the experiment",
            category = "Settings",
            subcategory = "Experiments"
    )
    public boolean autoCloseExperiments = true;

    @Property(
            type = PropertyType.NUMBER,
            name = "Serum Count",
            description = "Number of Metaphysical Serums consumed",
            category = "Settings",
            subcategory = "Experiments",
            min = 0,
            max = 3
    )
    public int serumCount = 3;

    @Property(
            type = PropertyType.BUTTON,
            name = "Button",
            description = "Dont Press.",
            category = "Settings",
            subcategory = "Buttons"
    )
    public void trollButton() {
        try {
            // Replace with your YouTube link
            URI youtubeUri = new URI("https://www.youtube.com/watch?v=dQw4w9WgXcQ");

            // Check if the Desktop API is supported
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(youtubeUri);
            } else {
                UChat.chat("Youre Gay");
            }
        } catch (Exception e) {
            e.printStackTrace();
            UChat.chat("Youre Ugly");
        }
    }

    @Property(
            type = PropertyType.BUTTON,
            name = "Help Button",
            description = "Click to run /khelp for the list of commands",
            category = "Settings",
            subcategory = "Buttons"
    )
    public void helpButton() {
        try {
            // Then execute it
            HelpCommand helpCmd = new HelpCommand();
            helpCmd.processCommand(Minecraft.getMinecraft().thePlayer, new String[]{});
            Minecraft.getMinecraft().displayGuiScreen(null);
        } catch (Exception e) {
            UChat.chat("Failed to execute help command");
        }

    }

    @Property(
            type = PropertyType.BUTTON,
            name = "Reset Settings",
            description = "Reset all settings to their default values",
            category = "Settings",
            subcategory = "Buttons"
    )
    public void resetButton() {
        try {
            // Reset all settings to their default values
            autoPauseLoops = 0;
            autoPlay = false;
            maxCps = 10;
            minCps = 6;
            minReelDelay = 200;
            maxReelDelay = 400;
            minCastDelay = 700;
            maxCastDelay = 1000;
            autoExperimentsEnabled = false;
            experimentClickDelay = 500;
            autoCloseExperiments = true;
            serumCount = 3;

            // Save the changes
            markDirty();
            writeData();

            UChat.chat("§aAll settings have been reset to default values!");
            Minecraft.getMinecraft().displayGuiScreen(null);
        } catch (Exception e) {
            e.printStackTrace();
            UChat.chat("§cFailed to reset settings");
        }
    }



    public VigilanceConfig() {
        super(new File("./config/krunkmode.toml"), "§6§lKrunk Mode");
        initialize();
    }

    // CONFIG TEST
    public void setAutoPauseLoops(int loops) {
        this.autoPauseLoops = loops;
        markDirty();
        writeData();
    }

    public void setAutoPlayEnabled(boolean enabled) {
        this.autoPlay = enabled;
        markDirty();
        writeData();
    }


    // Method to save config (for compatibility)
    public void save() {
        writeData();
    }


}