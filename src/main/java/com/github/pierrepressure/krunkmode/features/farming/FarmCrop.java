// FarmCrop.java
package com.github.pierrepressure.krunkmode.features.farming;

import com.github.pierrepressure.krunkmode.KrunkModeConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import java.util.Random;

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
    protected static boolean autoPaused = false;
    protected static int delaySwitchHotbarTicks = -1;

    // Add a protected static field to hold the instance
    public static FarmCrop INSTANCE;

    // Common configuration fields
    protected static int autoPauseLoops = 0;
    protected static boolean autoPlay = false;
    protected static KrunkModeConfig config;
    private static long loadWorldTime;

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

    public abstract void toggle(EntityPlayer player);

    // Change to use instance method from the provided instance
    public static void toggle(EntityPlayer player, FarmCrop cropInstance) {
        INSTANCE = cropInstance;
        isRunning = !isRunning;
        autoPaused = false;
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
                        cropInstance.getCropName(), // Use the provided instance
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
                    String.format("§6[KM] Farming %s §c§lPAUSED", getCropName())));
        }
    }

    public static void play() {
        if (isRunning && wasPaused) {
            autoPaused = false;
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
                String.format("§6[KM] Farming Auto Pause Loops: §a§l%d"
                        , num) // Use instance reference
        ));
    }

    public static int getAutoPauseLoops() {
        return autoPauseLoops;
    }

    public static boolean isRunning() {
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

    public static FarmCrop getInstance(){
        return INSTANCE;
    }

    public static boolean isPaused(){
        return wasPaused;
    }

    public static void setAutoPaused(boolean p){
        autoPaused = p;
    }

    public static boolean isAutoPaused(){
        return autoPaused;
    }

    public void onWorldLoad(WorldEvent.Load event) {
        long now = System.currentTimeMillis();

        // Ignore loads within the cooldown window
        if ((now - loadWorldTime) < 10000) return;

        loadWorldTime = now;

        if (event.world.isRemote && !wasPaused) { // Client-side world load

            pause();

            // Schedule hotbar switch to slot 9 (index 8) after a few ticks
            delaySwitchHotbarTicks = new Random().nextInt(100) + 100; // 100 to 199 ticks delay

            if (autoPlay) {
                autoPlay = false;
                mc.thePlayer.addChatMessage(new ChatComponentText("§6[KM] Detected world change! Auto Play §c§lDISABLED"));
            } else {
                mc.thePlayer.addChatMessage(new ChatComponentText("§6[KM] Detected world change!"));
            }
        }
    }


}