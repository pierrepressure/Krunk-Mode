package com.github.pierrepressure.krunkmode.features;

import com.github.pierrepressure.krunkmode.KrunkMode;
import com.github.pierrepressure.krunkmode.VigilanceConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ExperimentManager {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Random random = new Random();

    private static HashMap<Integer, Integer> ultrasequencerOrder = new HashMap<>();
    private static ArrayList<Integer> chronomatronOrder = new ArrayList<>(28);
    private static long lastClickTime = 0L;
    private static boolean hasAdded = false;
    private static int lastAdded = 0;
    private static int clicks = 0;
    private static int currentRandomDelay = 0; // Store the random delay for current click

    // Config values - will be set from VigilanceConfig
    private static int clickDelay = 200;
    private static boolean autoClose = true;
    private static int serumCount = 0;

    private static VigilanceConfig config;

    // Method to update settings with chat notifications
    public static void updateSettings() {
        if (clickDelay != config.experimentClickDelay) {
            clickDelay = config.experimentClickDelay;
//            if (mc.thePlayer != null) {
//                mc.thePlayer.addChatMessage(new ChatComponentText("§6§lUPDATED! §6Experiment Click Delay: §a§l" + clickDelay + "ms"));
//            }
        }

        if (autoClose != config.autoCloseExperiments) {
            autoClose = config.autoCloseExperiments;
//            if (mc.thePlayer != null) {
//                mc.thePlayer.addChatMessage(new ChatComponentText("§6§lUPDATED! §6Auto Close Experiments: §a§l" + (autoClose ? "ON" : "OFF")));
//            }
        }

        if (serumCount != config.serumCount) {
            serumCount = config.serumCount;
//            if (mc.thePlayer != null) {
//                mc.thePlayer.addChatMessage(new ChatComponentText("§6§lUPDATED! §6Serum Count: §a§l" + serumCount));
//            }
        }
    }

    public static void init(VigilanceConfig config) {
        ExperimentManager.config = config;
        clickDelay = config.experimentClickDelay;
        autoClose = config.autoCloseExperiments;
        serumCount = config.serumCount;
    }

    private static void reset() {
        ultrasequencerOrder.clear();
        chronomatronOrder.clear();
        hasAdded = false;
        lastAdded = 0;
        clicks = 0;
        lastClickTime = 0L; // Reset to 0 to allow immediate first click
        currentRandomDelay = getRandomDelay(); // Set initial random delay
    }

    public static void onGuiOpen(GuiOpenEvent event) {
        if (event.gui == null && mc.thePlayer != null) {
            updateSettings();
            reset();
        }
    }

    public static void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        // Check if experiments are enabled in config
        if (!KrunkMode.config.autoExperimentsEnabled) return;

        if (!(event.gui instanceof GuiChest)) return;

        GuiChest guiChest = (GuiChest) event.gui;
        if (!(guiChest.inventorySlots instanceof ContainerChest)) return;

        ContainerChest containerChest = (ContainerChest) guiChest.inventorySlots;
        String containerName = containerChest.getLowerChestInventory().getDisplayName().getUnformattedText();

        // Check if we're in a private island (simplified check)
        if (!isInPrivateIsland()) return;

        List<Slot> invSlots = containerChest.inventorySlots;

        if (containerName.startsWith("Chronomatron (")) {
            solveChronomatron(invSlots);
        } else if (containerName.startsWith("Ultrasequencer (")) {
            solveUltraSequencer(invSlots);
        }
    }

    private static boolean isInPrivateIsland() {
        // Simplified check - you might want to implement proper location detection
        // This is a basic implementation that should work for most cases
        return true; // You can implement proper island detection here
    }

    private static void solveChronomatron(List<Slot> invSlots) {
        if (invSlots.size() <= 49) return;

        Slot slot49 = invSlots.get(49);

        // Check if experiment is complete (glowstone in slot 49)
        if (slot49.getStack() != null &&
                slot49.getStack().getItem() == Item.getItemFromBlock(Blocks.glowstone) &&
                lastAdded < invSlots.size() &&
                invSlots.get(lastAdded).getStack() != null &&
                !invSlots.get(lastAdded).getStack().isItemEnchanted()) {

            if (autoClose && chronomatronOrder.size() > 11 - serumCount) {
                mc.thePlayer.closeScreen();
            }
            hasAdded = false;
        }

        // Look for new enchanted item to add to sequence
        if (!hasAdded && slot49.getStack() != null && slot49.getStack().getItem() == Items.clock) {
            for (Slot slot : invSlots) {
                if (slot.slotNumber >= 10 && slot.slotNumber <= 43 &&
                        slot.getStack() != null && slot.getStack().isItemEnchanted()) {

                    chronomatronOrder.add(slot.slotNumber);
                    lastAdded = slot.slotNumber;
                    hasAdded = true;
                    clicks = 0;
                    lastClickTime = System.currentTimeMillis(); // Reset timer when new sequence starts
                    currentRandomDelay = getRandomDelay(); // Get new random delay for first click
                    break;
                }
            }
        }

        // Execute clicks with human-like timing
        if (hasAdded && slot49.getStack() != null && slot49.getStack().getItem() == Items.clock &&
                chronomatronOrder.size() > clicks) {

            long currentTime = System.currentTimeMillis();
            long timeSinceLastClick = currentTime - lastClickTime;
            long requiredDelay = clickDelay + currentRandomDelay;

            if (timeSinceLastClick >= requiredDelay) {
                windowClick(chronomatronOrder.get(clicks)); // Middle click
                lastClickTime = currentTime;
                clicks++;
                currentRandomDelay = getRandomDelay(); // Get new random delay for next click
            }
        }
    }

    private static void solveUltraSequencer(List<Slot> invSlots) {
        if (invSlots.size() <= 49) return;

        Slot slot49 = invSlots.get(49);

        if (slot49.getStack() != null && slot49.getStack().getItem() == Items.clock) {
            hasAdded = false;
        }

        // Setup sequence when glowstone appears
        if (!hasAdded && slot49.getStack() != null &&
                slot49.getStack().getItem() == Item.getItemFromBlock(Blocks.glowstone)) {

            if (invSlots.size() <= 44 || invSlots.get(44).getStack() == null) return;

            ultrasequencerOrder.clear();

            for (Slot slot : invSlots) {
                if (slot.slotNumber >= 9 && slot.slotNumber <= 44 &&
                        slot.getStack() != null && slot.getStack().getItem() == Items.dye) {

                    ultrasequencerOrder.put(slot.getStack().stackSize - 1, slot.slotNumber);
                }
            }

            hasAdded = true;
            clicks = 0;
            lastClickTime = System.currentTimeMillis(); // Reset timer when new sequence starts
            currentRandomDelay = getRandomDelay(); // Get new random delay for first click

            if (ultrasequencerOrder.size() > 9 - serumCount && autoClose) {
                mc.thePlayer.closeScreen();
                return;
            }
        }

        // Execute clicks in sequence with human-like timing
        if (slot49.getStack() != null && slot49.getStack().getItem() == Items.clock &&
                ultrasequencerOrder.containsKey(clicks)) {

            long currentTime = System.currentTimeMillis();
            long timeSinceLastClick = currentTime - lastClickTime;
            long requiredDelay = clickDelay + currentRandomDelay;

            if (timeSinceLastClick >= requiredDelay) {
                Integer slotNumber = ultrasequencerOrder.get(clicks);
                if (slotNumber != null) {
                    windowClick(slotNumber); // Middle click
                    lastClickTime = currentTime;
                    clicks++;
                    currentRandomDelay = getRandomDelay(); // Get new random delay for next click
                }
            }
        }
    }

    // Add small random delay to make clicks more human-like
    private static int getRandomDelay() {
        return random.nextInt(101); // Random delay between -25ms and +25ms
    }

    private static void windowClick(int slotNumber) {
        if (mc.thePlayer != null && mc.thePlayer.openContainer != null) {
            // This automatically handles both client and server side
            mc.playerController.windowClick(
                    mc.thePlayer.openContainer.windowId,
                    slotNumber,
                    0, // mouse button (0 = left, 1 = right)
                    0, // click mode (0 = normal, 1 = shift, etc.)
                    mc.thePlayer
            );
        }
    }
    
}