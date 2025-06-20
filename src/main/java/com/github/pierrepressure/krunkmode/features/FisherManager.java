package com.github.pierrepressure.krunkmode.features;

import com.github.pierrepressure.krunkmode.VigilanceConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Random;

public enum FisherManager {
    INSTANCE;

    // Configuration (milliseconds)
    private static int MIN_REEL_DELAY = 200;    // 0.1s
    private static int MAX_REEL_DELAY = 400;    // 0.3s
    private static int MIN_CAST_DELAY = 700;   // 1s
    private static int MAX_CAST_DELAY = 1000;   // 2s
    private static final int SLOT_SWITCH_DELAY = 300; // 1 second delay when switching back

    private long lastSoundTime = 0;  // Time when the last sound was processed
    private final int SOUND_COOLDOWN_MS = 1000;  // 0.1 seconds = 100 milliseconds

    private static final Minecraft mc = Minecraft.getMinecraft();
    private final Random random = new Random();

    private static boolean autoKill = false;
    private boolean enabled = false;
    private long nextCastTime = 0;
    private long scheduledReelTime = 0;

    // New fields for tracking hotbar slot
    private int originalRodSlot = -1;  // The slot where the fishing rod was when we last reeled
    private boolean waitingForSlotReturn = false;  // Whether we're waiting for player to return to rod slot
    private long slotReturnTime = 0;  // Time when player returned to rod slot

    // Add these fields to your FisherManager class
    private boolean macroRunning = false;
    private int macroStep = 0;
    private long macroStepTime = 0;
    private int originalSlot = -1;
    private float originalYaw = 0;
    private float originalPitch = 0;
    private float targetYaw = 0;
    private float targetPitch = 0;
    private long interpolationStartTime = 0;
    private static final int INTERPOLATION_DURATION = 150; // milliseconds for smooth rotation
    private static final int STEP_DELAY = 100; // milliseconds between macro steps


    private static VigilanceConfig config;

    public static void init(VigilanceConfig config) {
        FisherManager.config = config;
        MIN_REEL_DELAY = config.reelDelay - 100;
        MAX_REEL_DELAY = config.reelDelay + 100;
        MIN_CAST_DELAY = config.castDelay - 100;
        MAX_CAST_DELAY = config.castDelay + 100;
        autoKill = config.autoKill;
    }

    public void toggle() {
        enabled = !enabled;
        updateSettings();
        mc.thePlayer.addChatMessage(new ChatComponentText(String.format("§l§6[KM] Fishing %s", enabled ? "§a§lENABLED" : "§c§lDISABLED")));
        if (enabled) {
            // Reset slot tracking when toggling on
            originalRodSlot = -1;
            waitingForSlotReturn = false;
            slotReturnTime = 0;
            scheduleCast();
        }
    }

    private void addViewDrift() {
        if (random.nextFloat() > 0.35f) return; // 35% chance to drift

        float yawDrift = (random.nextFloat() - 0.5f) * 0.5f;   // -0.25 to +0.25 degrees
        float pitchDrift = (random.nextFloat() - 0.5f) * 0.25f; // -0.125 to +0.125 degrees

        Minecraft.getMinecraft().thePlayer.rotationYaw += yawDrift;
        Minecraft.getMinecraft().thePlayer.rotationPitch += pitchDrift;
    }

    public void onTick(TickEvent.ClientTickEvent event) {
        if (!enabled || mc.thePlayer == null || event.phase == TickEvent.Phase.END) return;
        // Block if any GUI except chat is open
        if (mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat)) {
            return;
        }

        handleMacroExecution();
        long now = System.currentTimeMillis();

        // Check if player has returned to original rod slot
        checkSlotReturn(now);

        // Handle casting - only if not waiting for slot return or enough time has passed since return
        if (shouldCast(now) && canCast(now)) {
            addViewDrift();
            castRod();
            nextCastTime = 0;
        }

        // Handle delayed reeling
        if (shouldReel(now)) {
            reelRod();
            scheduledReelTime = 0; // Reset after reeling

            if (autoKill) executeHotbarMacro();
        }
    }

    public void onSound(PlaySoundEvent event) {
        if (!enabled || !event.name.equals("note.pling")) return;
        if (mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat)) return;

        long now = System.currentTimeMillis();

        // Ignore sounds within the cooldown window
        if ((now - lastSoundTime) < SOUND_COOLDOWN_MS) return;

        // Otherwise, handle the sound and update the lastSoundTime
        lastSoundTime = now;

        scheduleReel();
    }

    private void castRod() {
        if (!isHoldingRod()) return;

        mc.thePlayer.swingItem();
        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
    }

    private void reelRod() {
        if (!isHoldingRod()) return;

        // Store the current slot as the original rod slot when reeling
        originalRodSlot = mc.thePlayer.inventory.currentItem;

        mc.thePlayer.swingItem();
        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
        scheduleCast();
    }

    private boolean isHoldingRod() {
        return mc.thePlayer.getHeldItem() != null &&
                mc.thePlayer.getHeldItem().getItem() instanceof ItemFishingRod;
    }

    private void checkSlotReturn(long currentTime) {
        if (originalRodSlot == -1) return; // No original slot recorded yet

        int currentSlot = mc.thePlayer.inventory.currentItem;

        if (waitingForSlotReturn) {
            // We were waiting for return to original slot
            if (currentSlot == originalRodSlot && isHoldingRod()) {
                // Player returned to original rod slot
                waitingForSlotReturn = false;
                slotReturnTime = currentTime;
            }
        } else {
            // Check if player switched away from original slot
            if (currentSlot != originalRodSlot) {
                waitingForSlotReturn = true;
                slotReturnTime = 0;
            }
        }
    }

    private boolean canCast(long currentTime) {
        // If we haven't recorded an original slot yet, allow casting
        if (originalRodSlot == -1) return true;

        // If we're waiting for slot return, don't cast
        if (waitingForSlotReturn) return false;

        // If we just returned to the slot, wait for the delay
        if (slotReturnTime > 0 && (currentTime - slotReturnTime) < SLOT_SWITCH_DELAY) {
            return false;
        }

        return true;
    }

    private void scheduleCast() {
        int delay = MIN_CAST_DELAY + random.nextInt(MAX_CAST_DELAY - MIN_CAST_DELAY);
        nextCastTime = System.currentTimeMillis() + delay;
    }

    private void scheduleReel() {
        int delay = MIN_REEL_DELAY + random.nextInt(MAX_REEL_DELAY - MIN_REEL_DELAY);
        scheduledReelTime = System.currentTimeMillis() + delay;
    }

    private boolean shouldCast(long currentTime) {
        return nextCastTime > 0 && currentTime >= nextCastTime;
    }

    private boolean shouldReel(long currentTime) {
        return scheduledReelTime > 0 && currentTime >= scheduledReelTime;
    }

    /**
     * Executes a smooth macro that:
     * 1. Switches to hotbar slot 3 (index 2)
     * 2. Smoothly looks directly down
     * 3. Right clicks
     * 4. Smoothly returns to original view direction
     * 5. Returns to original hotbar slot
     */
    public void executeHotbarMacro() {
        if (macroRunning || mc.thePlayer == null) return;

        macroRunning = true;
        macroStep = 0;
        macroStepTime = System.currentTimeMillis();

        // Store original state
        originalSlot = mc.thePlayer.inventory.currentItem;
        originalYaw = mc.thePlayer.rotationYaw;
        originalPitch = mc.thePlayer.rotationPitch;
    }

    /**
     * Call this in your onTick method to handle the macro execution
     */
    private void handleMacroExecution() {
        if (!macroRunning) return;

        long currentTime = System.currentTimeMillis();

        switch (macroStep) {
            case 0: // Switch to slot 3 (index 2)
                mc.thePlayer.inventory.currentItem = 2;
                advanceToNextStep(currentTime);
                break;

            case 1: // Start smooth look down
                targetYaw = originalYaw; // Keep same yaw
                targetPitch = 90.0f; // Look directly down
                startSmoothRotation(currentTime);
                advanceToNextStep(currentTime);
                break;

            case 2: // Wait for rotation to complete
                if (updateSmoothRotation(currentTime)) {
                    // Rotation complete, advance to first right click step
                    advanceToNextStep(currentTime);
                }
                break;

            case 3: // First right click (only after rotation is complete)
                if (currentTime - macroStepTime >= STEP_DELAY) {
                    if (mc.thePlayer.getHeldItem() != null) {
                        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                    }
                    advanceToNextStep(currentTime);
                }
                break;

            case 4: // Second right click
                if (currentTime - macroStepTime >= STEP_DELAY) {
                    if (mc.thePlayer.getHeldItem() != null) {
                        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                    }
                    advanceToNextStep(currentTime);
                }
                break;

            case 5: // Start smooth return to original view
                targetYaw = originalYaw;
                targetPitch = originalPitch;
                startSmoothRotation(currentTime);
                advanceToNextStep(currentTime);
                break;

            case 6: // Wait for rotation to complete, then return to original slot
                if (updateSmoothRotation(currentTime)) {
                    mc.thePlayer.inventory.currentItem = originalSlot;
                    advanceToNextStep(currentTime);
                }
                break;

            case 7: // Macro complete
                macroRunning = false;
                macroStep = 0;
                break;
        }
    }

    /**
     * Starts a smooth rotation interpolation
     */
    private void startSmoothRotation(long currentTime) {
        interpolationStartTime = currentTime;
    }

    /**
     * Updates the smooth rotation interpolation
     *
     * @return true if interpolation is complete, false if still in progress
     */
    private boolean updateSmoothRotation(long currentTime) {
        long elapsed = currentTime - interpolationStartTime;

        if (elapsed >= INTERPOLATION_DURATION) {
            // Interpolation complete, set final values
            mc.thePlayer.rotationYaw = targetYaw;
            mc.thePlayer.rotationPitch = targetPitch;
            return true;
        }

        // Calculate interpolation progress (0.0 to 1.0)
        float progress = (float) elapsed / INTERPOLATION_DURATION;

        // Use easeInOutQuad for smooth acceleration/deceleration
        progress = easeInOutQuad(progress);

        // Interpolate yaw (handle wrapping around 360 degrees)
        float currentYaw = mc.thePlayer.rotationYaw;
        float yawDiff = targetYaw - currentYaw;

        // Handle yaw wrapping (choose shortest rotation path)
        if (yawDiff > 180) yawDiff -= 360;
        if (yawDiff < -180) yawDiff += 360;

        mc.thePlayer.rotationYaw = currentYaw + (yawDiff * progress);

        // Interpolate pitch (simpler, no wrapping needed)
        float currentPitch = mc.thePlayer.rotationPitch;
        float pitchDiff = targetPitch - currentPitch;
        mc.thePlayer.rotationPitch = currentPitch + (pitchDiff * progress);

        // Clamp pitch to valid range
        mc.thePlayer.rotationPitch = Math.max(-90.0f, Math.min(90.0f, mc.thePlayer.rotationPitch));

        return false; // Still interpolating
    }

    /**
     * Easing function for smooth acceleration and deceleration
     */
    private float easeInOutQuad(float t) {
        return t < 0.5f ? 2 * t * t : 1 - (float) Math.pow(-2 * t + 2, 2) / 2;
    }

    /**
     * Advances to the next macro step with delay
     */
    private void advanceToNextStep(long currentTime) {
        if (currentTime - macroStepTime >= STEP_DELAY) {
            macroStep++;
            macroStepTime = currentTime;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    // If you have methods that update config values:
    public static void updateSettings() {

        if (MIN_CAST_DELAY != config.castDelay - 100) {
            MIN_CAST_DELAY = config.castDelay - 100;
            MAX_CAST_DELAY = config.castDelay + 100;
        }

        if (MIN_REEL_DELAY != config.reelDelay - 100) {
            MIN_REEL_DELAY = config.reelDelay - 100;
            MAX_REEL_DELAY = config.reelDelay + 100;
        }

        if (autoKill != config.autoKill) {
            autoKill = config.autoKill;
        }
    }
}