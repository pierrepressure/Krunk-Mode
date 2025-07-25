package com.github.pierrepressure.krunkmode.features.farming;

import java.util.Random;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.gameevent.TickEvent;


public class FarmPotato extends FarmCrop {

    public static final FarmPotato INSTANCE = new FarmPotato();
    private final int[] STEP_DURATIONS;
    private static boolean[][] STEP_KEYS;
    private final int randomDelay = new Random().nextInt(300);

    private FarmPotato() {

        // Durations for each step in milliseconds
        STEP_DURATIONS = new int[]{
                500,     // 0: Start
                119500 + randomDelay,  // 1: Move left (hold A)
                100,   // 2: Release
                119500 + randomDelay,  // 3: Move right (hold D)
                100,   // 4: Release
                119500 + randomDelay,  // 5: Move left (hold A)
                100,   // 6: Release
                119500 + randomDelay,  // 7: Move right (hold D)
                100,   // 8: Release
                119500 + randomDelay,  // 9: Move left (hold A)
                100,   // 10: Release
        };

        // Flags to track which keys should be pressed in each step
        STEP_KEYS = new boolean[][]{
                {false, false, false},  // 0: Start
                {false, true, false},   // 1: Move Right
                {false, false, false},  // 2: Release
                {true, false, false},   // 3: Move Left
                {false, false, false},  // 4: Release
                {false, true, false},   // 5: Move Right
                {false, false, false},  // 6: Release
                {true, false, false},  // 7: Move Left
                {false, false, false},  // 8: Release
                {false, true, false},  // 9: Move Right
                {false, false, false}   // 10: Release

        };

    }

    // Override method without static keyword
    @Override
    protected String getCropName() {
        return "Potato";
    }

    // Override method without static keyword
    @Override
    protected int getCurrentStepDuration() {
        if (currentStep >= 0 && currentStep < STEP_DURATIONS.length) {
            return STEP_DURATIONS[currentStep];
        }
        return 0;
    }

    @Override
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!isRunning || event.phase != TickEvent.Phase.END) return;

        // Check if player or Minecraft instance is null (safety check)
        if (mc == null || mc.thePlayer == null) return;

        //If Paused, update pause duration
        if (wasPaused) {
            if (autoPlay && mc.currentScreen == null && !autoPaused)
                play();
            return;
        }

        // If Gui Opens, then Pause Farm
        if (mc.currentScreen != null) {
            pause();
            return;
        }

        // We're active, so apply the current step's key states every tick
        applyCurrentStepKeys();

        //Also Hold Break
        if (!mc.gameSettings.keyBindAttack.isPressed()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
        }

        // Check if it's time to advance to the next step
        long now = System.currentTimeMillis();
        if (now - lastStepTime >= STEP_DURATIONS[currentStep]) {
            // Time to move to next step
            lastStepTime = now;
            currentStep++;

            if (currentStep >= STEP_DURATIONS.length) {

                // Completed all steps, trigger warp and auto-pause logic
                mc.thePlayer.sendChatMessage("/warp garden");
                autoPLoopsCounter++;

                if (autoPauseLoops != 0 && (autoPLoopsCounter >= autoPauseLoops)) {
                    mc.thePlayer.addChatMessage(new ChatComponentText(String.format("§6[KM] Farming Potato Completed §a§l%d §6Loops", autoPauseLoops)));
                    autoPaused = true;
                    pause();
                    autoPLoopsCounter = 0;
                }

                currentStep = 0; // Reset step for next start
            }
        }
    }

    /**
     * Apply the key states for the current step on every tick
     */
    @Override
    protected void applyCurrentStepKeys() {
        // Ensure we're within bounds
        if (currentStep < 0 || currentStep >= STEP_KEYS.length) {
            currentStep = 0; // Reset if somehow out of bounds
        }

        if (mc.gameSettings.keyBindLeft.isKeyDown() != STEP_KEYS[currentStep][0]) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), STEP_KEYS[currentStep][0]);
        }

        if (mc.gameSettings.keyBindRight.isKeyDown() != STEP_KEYS[currentStep][1]) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), STEP_KEYS[currentStep][1]);
        }

        if (mc.gameSettings.keyBindForward.isKeyDown() != STEP_KEYS[currentStep][2]) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), STEP_KEYS[currentStep][2]);
        }
    }

    /**
     * Toggle the farming routine on or off
     */
    public void toggle(EntityPlayer player) {
        if (player.getName().equals("BlueSquire")) {
            STEP_KEYS[1] = new boolean[]{true, false, false}; //1: Move Left
            STEP_KEYS[3] = new boolean[]{false, true, false}; //3: Move Right
            STEP_KEYS[5] = new boolean[]{true, false, false}; //5: Move Left
            STEP_KEYS[7] = new boolean[]{false, true, false}; //7: Move Right
            STEP_KEYS[9] = new boolean[]{true, false, false}; //9: Move Left
            mc.thePlayer.addChatMessage(new ChatComponentText("§6[KM] §bBlueSquire §6Detected!"));
        }
        FarmCrop.toggle(player,this);
    }


}