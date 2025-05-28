package com.github.pierrepressure.krunkmode.features.farming;

import java.util.Random;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.gameevent.TickEvent;


public class FarmCane extends FarmCrop {

    public static final FarmCane INSTANCE = new FarmCane();
    private int[] STEP_DURATIONS;
    private static boolean[][] STEP_KEYS;
    private final int randomDelay = new Random().nextInt(300);
    private int delaySwitchHotbarTicks = -1;
    private int caneLoops=5;
    private int caneLoopStep = 1;

    private FarmCane() {
        // Durations for each step in milliseconds
        STEP_DURATIONS = new int[]{
                500,     // 0: Start
                100,                // 1: Release
                48500 + randomDelay,  // 2: Forward
                100,                // 3: Release
                49200 + randomDelay,  // 4: Left
                100,   // 5: Release
        };

        // Flags to track which keys should be pressed in each step
        STEP_KEYS = new boolean[][]{
                {false, false, false,false},  // 0: Start
                {false, false, false,false},  // 1: Release
                {false, false, false,true},   // 2: Forward
                {false, false, false,false},  // 3: Release
                {true, false, false,false},   // 4: Left
                {false, false, false,false}   // 5: Release

        };

    }

    // Override method without static keyword
    @Override
    protected String getCropName() {
        return "Cane";
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

        // Handle delayed hotbar switching
        if (delaySwitchHotbarTicks > 0) {
            delaySwitchHotbarTicks--;
        } else if (delaySwitchHotbarTicks == 0) {
            if (mc != null && mc.thePlayer != null && mc.thePlayer.inventory != null) {
                mc.thePlayer.inventory.currentItem = 8; // Select slot 9
            }
            delaySwitchHotbarTicks = -1; // Reset the delay
        }

        // Check if player or Minecraft instance is null (safety check)
        if (mc == null || mc.thePlayer == null) return;

        //If Paused, update pause duration
        if (wasPaused) {
            if (autoPlay && mc.currentScreen == null && !autoPaused) play();
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

            //If it's the last step, increment the loop
            if (currentStep >= STEP_DURATIONS.length) {
                currentStep = caneLoopStep; //Change this
                loopCount++;

                //If it's the last loop, increment the autopause
                if (loopCount >= caneLoops) { //Change this
                    mc.thePlayer.sendChatMessage("/warp garden");
                    loopCount = 0;
                    autoPLoopsCounter++;

                    //If autopause is done, then pause
                    if (autoPauseLoops != 0 && (autoPLoopsCounter >= autoPauseLoops)) {
                        mc.thePlayer.addChatMessage(new ChatComponentText(String.format("§6[KM] Farming Cane Completed §a§l%d §6Loops", autoPauseLoops)));
                        pause();
                        autoPLoopsCounter = 0;
                        autoPaused = true;
                    }
                }
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

        if (mc.gameSettings.keyBindBack.isKeyDown() != STEP_KEYS[currentStep][2]) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), STEP_KEYS[currentStep][2]);
        }

        if (mc.gameSettings.keyBindForward.isKeyDown() != STEP_KEYS[currentStep][3]) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), STEP_KEYS[currentStep][3]);
        }
    }

    /**
     * Toggle the farming routine on or off
     */
    public void toggle(EntityPlayer player) {
        if (player.getName().equals("BlueSquire")) {
            STEP_DURATIONS = new int[]{
                    500,     // 0: Start
                    20000 + randomDelay,  // 1: Forward+right
                    100,                // 2: Release
                    20000 + randomDelay,  // 3: back
                    100,                // 4: Release
                    20000 + randomDelay,  // 5: Forward+right
                    100,                // 6: Release
            };

            STEP_KEYS = new boolean[][]{
                    {false, false, false,false},  // 0: Start
                    {false, true, false,true},   // 1: Forward+Right
                    {false, false, false,false},  // 2: Release
                    {false, false, true,false},   // 3: Back
                    {false, false, false,false},  // 4: Release
                    {false, true, false,true},   // 5: Forward+Right
                    {false, false, false,false},  // 6: Release
            };

            caneLoops=15;
            caneLoopStep = 3;
            mc.thePlayer.addChatMessage(new ChatComponentText("§6[KM] §bBlueSquire §6Detected!"));
        }
        FarmCrop.toggle(player, this);
    }

}
