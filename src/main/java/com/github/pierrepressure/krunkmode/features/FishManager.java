package com.github.pierrepressure.krunkmode.features;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Random;

public enum FishManager {
    INSTANCE;

    // Configuration (milliseconds)
    private final int MIN_REEL_DELAY = 100;    // 0.1s
    private final int MAX_REEL_DELAY = 300;    // 0.3s
    private final int MIN_CAST_DELAY = 500;   // 1s
    private final int MAX_CAST_DELAY = 1000;   // 2s

    private long lastSoundTime = 0;  // Time when the last sound was processed
    private final int SOUND_COOLDOWN_MS = 1000;  // 0.1 seconds = 100 milliseconds


    private final Minecraft mc = Minecraft.getMinecraft();
    private final Random random = new Random();

    private boolean enabled = false;
    private long nextCastTime = 0;
    private long scheduledReelTime = 0;

    public void toggle() {
        enabled = !enabled;
        mc.thePlayer.addChatMessage(new ChatComponentText(String.format("§l§6[KM] Fishing %s", enabled ? "§a§lENABLED" : "§c§lDISABLED")));
        if(enabled) scheduleCast();
    }

    private void addViewDrift() {
        if (random.nextFloat() > 0.35f) return; // 35% chance to drift

        float yawDrift = (random.nextFloat() - 0.5f) * 0.5f;   // -0.25 to +0.25 degrees
        float pitchDrift = (random.nextFloat() - 0.5f) * 0.25f; // -0.125 to +0.125 degrees

        Minecraft.getMinecraft().thePlayer.rotationYaw += yawDrift;
        Minecraft.getMinecraft().thePlayer.rotationPitch += pitchDrift;
    }


    public void onTick(TickEvent.ClientTickEvent event) {
        if(!enabled || mc.thePlayer == null || event.phase == TickEvent.Phase.END) return;
        // Block if any GUI except chat is open
        if(mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat)) {
            return;
        }

        long now = System.currentTimeMillis();

        // Handle casting
        if(shouldCast(now)) {
            addViewDrift();
            castRod();
            nextCastTime = 0;
        }

        // Handle delayed reeling
        if(shouldReel(now)) {
            addViewDrift();
            reelRod();
            scheduledReelTime = 0; // Reset after reeling
        }
    }

    @SubscribeEvent
    public void onSound(PlaySoundEvent event) {
        if(!enabled || !event.name.equals("note.pling")) return;
        if(mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat)) return;

        long now = System.currentTimeMillis();

        // Ignore sounds within the cooldown window
        if((now - lastSoundTime) < SOUND_COOLDOWN_MS) return;

        // Otherwise, handle the sound and update the lastSoundTime
        lastSoundTime = now;

        scheduleReel();
    }

    private void castRod() {
        if(!isHoldingRod()) return;

        mc.thePlayer.swingItem();
        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
    }

    private void reelRod() {
        if(!isHoldingRod()) return;

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
}