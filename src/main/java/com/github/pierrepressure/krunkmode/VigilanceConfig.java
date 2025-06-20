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

    // ===== AUTOMATION CATEGORY =====

    // Farming automation
    @Property(
            type = PropertyType.SLIDER,
            name = "Auto Pause Loops",
            description = "Number of farming loops before pausing (0 = infinite)",
            category = "Farming",
            min = 0,
            max = 20
    )
    public int autoPauseLoops = 0;

    @Property(
            type = PropertyType.SWITCH,
            name = "Auto Play",
            description = "Automatically resume after pause",
            category = "Farming"
    )
    public boolean autoPlay = false;

    // Auto clicker
    @Property(
            type = PropertyType.SLIDER,
            name = "Clicker CPS",
            description = "Clicks per second for the clicker \n§c⚠ Stay below 10 to avoid triggering anti-cheat. ",
            category = "Clicker",
            min = 1,
            max = 20
    )
    public int cps = 8;

    // Auto fishing
    @Property(
            type = PropertyType.NUMBER,
            name = "Cast Delay",
            description = "Delay in milliseconds before recasting\n&8(Default: 900)",
            category = "Fishing",
            min = 500,
            max = 20000,
            increment = 50
    )
    public int castDelay = 900;

    @Property(
            type = PropertyType.NUMBER,
            name = "Reel Delay",
            description = "Delay in milliseconds before reeling after bite\n&8(Default: 300)",
            category = "Fishing",
            min = 50,
            max = 500,
            increment = 25
    )
    public int reelDelay = 300;

    @Property(
            type = PropertyType.SWITCH,
            name = "Auto Kill Fished Creatures",
            description = "Automatically kill Sea Creatures (with hype, L bassem)",
            category = "Fishing"
    )
    public boolean autoKill = false;

    // Auto experiments
    @Property(
            type = PropertyType.SWITCH,
            name = "Enable Auto Experiments",
            description = "Automatically solve Chronomatron and Ultrasequencer experiments",
            category = "Experiments"
    )
    public boolean autoExperimentsEnabled = false;

    @Property(
            type = PropertyType.SLIDER,
            name = "Click Delay",
            description = "Time in milliseconds between automatic experiment clicks\n&8(Default: 500)",
            category = "Experiments",
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
            category = "Experiments"
    )
    public boolean autoCloseExperiments = true;

    @Property(
            type = PropertyType.NUMBER,
            name = "Serum Count",
            description = "Number of Metaphysical Serums consumed",
            category = "Experiments",
            min = 0,
            max = 3
    )
    public int serumCount = 3;

    // ===== UTILITIES CATEGORY =====

    @Property(
            type = PropertyType.SWITCH,
            name = "Track Lobbies",
            description = "Notifies you if you've already been in this lobby before \n(Useful for Swapping Lobbies)",
            category = "Utilities"
    )
    public boolean lobbyTrackingEnabled = false;

    @Property(
            type = PropertyType.BUTTON,
            name = "Help",
            description = "Runs /khelp to show the full list of Krunk Mode commands",
            category = "Utilities"
    )
    public void helpButton() {
        try {
            HelpCommand helpCmd = new HelpCommand();
            helpCmd.processCommand(Minecraft.getMinecraft().thePlayer, new String[]{});
            Minecraft.getMinecraft().displayGuiScreen(null);
        } catch (Exception e) {
            UChat.chat("Failed to execute help command");
        }
    }

    @Property(
            type = PropertyType.BUTTON,
            name = "Notepad",
            description = "Open a web-based notepad for taking notes",
            category = "Utilities"
    )
    public void writeBox() {
        try {
            URI writeBoxUrl = new URI("https://write-box.appspot.com/");
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(writeBoxUrl);
            } else {
                UChat.chat("Desktop browsing not supported");
            }
        } catch (Exception e) {
            e.printStackTrace();
            UChat.chat("Failed to open notepad");
        }
    }

    @Property(
            type = PropertyType.BUTTON,
            name = "Reset All Settings",
            description = "Reset all configuration values to their defaults",
            category = "Utilities"
    )
    public void resetButton() {
        try {
            // Reset all settings to their default values
            autoPauseLoops = 0;
            autoPlay = false;
            cps = 8;
            reelDelay = 300;
            castDelay = 900;
            autoExperimentsEnabled = false;
            experimentClickDelay = 500;
            autoCloseExperiments = true;
            serumCount = 3;
            lobbyTrackingEnabled = false;

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

    @Property(
            type = PropertyType.SWITCH,
            name = "Chat Emotes",
            description = "Automatically replace text with emotes in chat messages",
            category = "Utilities"
    )
    public boolean chatEmotes = true;


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