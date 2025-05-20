// FarmCrop.java
package com.github.pierrepressure.krunkmode.features;

import com.github.pierrepressure.krunkmode.KrunkModeConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public abstract class FarmCrop {
    protected static final Minecraft mc = Minecraft.getMinecraft();

    // Common state fields
    protected static boolean isRunning = false;
    protected static int currentStep = 0;
    protected static long lastStepTime = 0;
    protected static int loopCount = 0;
    protected static boolean wasPaused = false;
    protected static int autoPLoopsCounter = 0;
    protected static long remainingStepTimeWhenPaused = 0;
    protected static boolean isLoopCompleted = false;

    // Add a protected static field to hold the instance
    protected static FarmCrop INSTANCE;

    // Common configuration fields
    protected static int autoPauseLoops = 0;
    protected static boolean autoPlay = false;
    protected static KrunkModeConfig config;

    public abstract void onTick(ClientTickEvent event);

    // Change from static to abstract method
    protected abstract String getCropName();

    // Change from static to abstract method
    protected abstract int getCurrentStepDuration();

    // Change from static to abstract method
    protected abstract void applyCurrentStepKeys();

    public static void init(KrunkModeConfig config) {
        FarmCrop.config = config;
        autoPauseLoops = config.getAutoPauseLoops();
        autoPlay = config.isAutoPlayEnabled();
    }

    public static void toggle(EntityPlayer player) {
        isRunning = !isRunning;
        autoPLoopsCounter = 0;

        if (isRunning) {
            lastStepTime = System.currentTimeMillis();
            currentStep = 0;
        } else {
            wasPaused = false;
            releaseAllKeys();
            currentStep = 0;
            loopCount = 0;
        }

        player.addChatMessage(new ChatComponentText(
                String.format("§l§6[KM] Farming %s %s",
                        FarmCrop.INSTANCE.getCropName(), // Use instance reference
                        isRunning ? "§a§lSTARTED!" : "§c§lSTOPPED!")
        ));
    }

    public void pause() {
        if (isRunning && !wasPaused) {
            long elapsed = System.currentTimeMillis() - lastStepTime;
            remainingStepTimeWhenPaused = Math.max(0, getCurrentStepDuration() - elapsed);
            releaseAllKeys();
            wasPaused = true;
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    String.format("§6[KM] Farming %s §c§lPAUSED", getCropName())
            ));
        }
    }

    public static void play() {
        if (isRunning && wasPaused) {
            isLoopCompleted = false;
            releaseAllKeys();
            wasPaused = false;
            lastStepTime = System.currentTimeMillis() - (
                    FarmCrop.INSTANCE.getCurrentStepDuration() - // Use instance reference
                            remainingStepTimeWhenPaused - 100);
            remainingStepTimeWhenPaused = 0;
            FarmCrop.INSTANCE.applyCurrentStepKeys(); // Use instance reference
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    String.format("§6[KM] Farming %s §a§lUNPAUSED",
                            FarmCrop.INSTANCE.getCropName()) // Use instance reference
            ));
        }
    }

    protected static void releaseAllKeys() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
    }

    public static void setAutoPauseLoops(int num) {
        autoPauseLoops = num;
        if (config != null) config.setAutoPauseLoops(num);
        mc.thePlayer.addChatMessage(new ChatComponentText(
                String.format("§6[KM] Farming %s Max Loops: §a§l%d",
                        FarmCrop.INSTANCE.getCropName(), num) // Use instance reference
        ));
    }

    public static int getAutoPauseLoops() {
        return autoPauseLoops;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public static void toggleAutoPlay() {
        autoPlay = !autoPlay;
        if (config != null) config.setAutoPlayEnabled(autoPlay);
        mc.thePlayer.addChatMessage(new ChatComponentText(
                String.format("§l§6[KM] Auto Play %s", autoPlay ? "§a§lENABLED" : "§c§lDISABLED")
        ));
    }

    public static boolean isAutoPlayEnabled() {
        return autoPlay;
    }


}