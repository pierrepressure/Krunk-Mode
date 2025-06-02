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

    private long lastSoundTime = 0;  // Time when the last sound was processed
    private final int SOUND_COOLDOWN_MS = 1000;  // 0.1 seconds = 100 milliseconds


    private static final Minecraft mc = Minecraft.getMinecraft();
    private final Random random = new Random();

    private boolean enabled = false;
    private long nextCastTime = 0;
    private long scheduledReelTime = 0;

    private static VigilanceConfig config;

    public static void init(VigilanceConfig config) {
        FisherManager.config = config;
        MIN_REEL_DELAY = Math.min(config.minReelDelay,config.maxReelDelay);
        MAX_REEL_DELAY = Math.max(config.minReelDelay,config.maxReelDelay);
        MIN_CAST_DELAY = Math.min(config.minCastDelay,config.maxCastDelay);
        MAX_CAST_DELAY = Math.max(config.minCastDelay,config.maxCastDelay);
    }

    public void toggle() {
        enabled = !enabled;
        updateSettings();
        mc.thePlayer.addChatMessage(new ChatComponentText(String.format("§l§6[KM] Fishing %s", enabled ? "§a§lENABLED" : "§c§lDISABLED")));
        if (enabled) scheduleCast();
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

        long now = System.currentTimeMillis();

        // Handle casting
        if (shouldCast(now)) {
            addViewDrift();
            castRod();
            nextCastTime = 0;
        }

        // Handle delayed reeling
        if (shouldReel(now)) {
            addViewDrift();
            reelRod();
            scheduledReelTime = 0; // Reset after reeling
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

        mc.thePlayer.swingItem();
        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
        scheduleCast();
    }

    private boolean isHoldingRod() {
        return mc.thePlayer.getHeldItem() != null &&
                mc.thePlayer.getHeldItem().getItem() instanceof ItemFishingRod;
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

    public boolean isEnabled() {
        return enabled;
    }

    // If you have methods that update config values:
    public static void updateSettings() {

        if (MIN_CAST_DELAY != config.minCastDelay || MAX_CAST_DELAY != config.maxCastDelay) {
            MIN_CAST_DELAY = Math.min(config.minCastDelay,config.maxCastDelay);
            MAX_CAST_DELAY = Math.max(config.minCastDelay, config.maxCastDelay);
//            mc.thePlayer.addChatMessage(new ChatComponentText("§6§lUPDATED! §6MinCastDelay: §a§l" + MIN_CAST_DELAY+
//                    " §6MaxCastDelay: §a§l" + MAX_CAST_DELAY));
        }


        if (MIN_REEL_DELAY != config.minReelDelay||MAX_REEL_DELAY != config.maxReelDelay) {
            MIN_REEL_DELAY = Math.min(config.minReelDelay,config.maxReelDelay);
            MAX_REEL_DELAY = Math.max(config.minReelDelay,config.maxReelDelay);
//            mc.thePlayer.addChatMessage(new ChatComponentText("§6§lUPDATED! §6MinReelDelay: §a§l" + MIN_REEL_DELAY+
//                    " §6MaxReelDelay: §a§l" + MAX_REEL_DELAY));
        }

    }
}