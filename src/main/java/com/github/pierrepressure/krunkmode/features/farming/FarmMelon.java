package com.github.pierrepressure.krunkmode.features.farming;

import java.util.Random;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.gameevent.TickEvent;


public class FarmMelon extends FarmCrop {

    public static final FarmMelon INSTANCE = new FarmMelon();
    private final int[] STEP_DURATIONS;
    private static boolean[][] STEP_KEYS;
    private final int randomDelay = new Random().nextInt(300);

    private FarmMelon() {
        // Durations for each step in milliseconds
        STEP_DURATIONS = new int[]{
                500,     // 0: Start
                72000 + randomDelay,//71650,  // 1: Move left (hold A)
                100,   // 2: Release A
                500,   // 3: Move forward (hold W)
                100,   // 4: Release W
                72000 + randomDelay,//71650,  // 5: Move right (hold D)
                100,   // 6: Release D
                500,   // 7: Move forward (hold W)
                100   // 8: Release W
        };

        // Flags to track which keys should be pressed in each step
        STEP_KEYS = new boolean[][]{
                {false, false, false},  // 0: Nothing only (left, right, forward, )
                {true, false, false},   // 1: Move left
                {false, false, false},  // 2:  Nothing only
                {false, false, true},   // 3: Move forward +
                {false, false, false},  // 4: Nothing only
                {false, true, false},   // 5: Move right
                {false, false, false},  // 6:  Nothing only
                {false, false, true},   // 7: Move forward +
                {false, false, false}   // 8:  Nothing only
        };

    }

    // Override method without static keyword
    @Override
    protected String getCropName() {
        return "Melon";
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
            if (currentStep >= STEP_DURATIONS.length) {
                currentStep = 0;
                loopCount++;
                if (loopCount >= 4) {
                    mc.thePlayer.sendChatMessage("/warp garden");
                    loopCount = 0;
                    autoPLoopsCounter++;

                    if (autoPauseLoops != 0 && (autoPLoopsCounter >= autoPauseLoops)) {
                        mc.thePlayer.addChatMessage(new ChatComponentText(String.format("§6[KM] Farming Melon Completed §a§l%d §6Loops", autoPauseLoops)));
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

        if (mc.gameSettings.keyBindForward.isKeyDown() != STEP_KEYS[currentStep][2]) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), STEP_KEYS[currentStep][2]);
        }
    }

    /**
     * Toggle the farming routine on or off
     */
    public void toggle(EntityPlayer player) {
        if (player.getName().equals("BlueSquire")) {
            STEP_KEYS[1] = new boolean[]{false, true, false};
            STEP_KEYS[5] = new boolean[]{true, false, false};
            mc.thePlayer.addChatMessage(new ChatComponentText("§6[KM] §bBlueSquire §6Detected!"));
        }

        FarmCrop.toggle(player, this);
    }


}