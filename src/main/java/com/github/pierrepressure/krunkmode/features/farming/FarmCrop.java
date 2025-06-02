// FarmCrop.java
package com.github.pierrepressure.krunkmode.features.farming;

import com.github.pierrepressure.krunkmode.VigilanceConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;


public abstract class FarmCrop  {
    protected static final Minecraft mc = Minecraft.getMinecraft();

    // Common state fields
    public static boolean isRunning = false;
    protected static int currentStep = 0;
    protected static long lastStepTime = 0;
    protected static int loopCount = 0;
    protected static boolean wasPaused = false;
    protected static long remainingStepTimeWhenPaused = 0;
    protected static boolean autoPaused = false;
    protected static int autoPauseLoops = 0;
    protected static boolean autoPlay = false;
    protected static int autoPLoopsCounter = 0;

    // Add a protected static field to hold the instance
    public static FarmCrop INSTANCE;

    // Common configuration fields
    protected static VigilanceConfig config;

    public static boolean isRunning() {
        return isRunning;
    }

    public abstract void onTick(ClientTickEvent event);

    // Change from static to abstract method
    protected abstract String getCropName();

    // Change from static to abstract method
    protected abstract int getCurrentStepDuration();

    // Change from static to abstract method
    protected abstract void applyCurrentStepKeys();

    public static void init(VigilanceConfig config) {
        FarmCrop.config = config;
        autoPauseLoops = config.autoPauseLoops;
        autoPlay = config.autoPlay;

    }

    public abstract void toggle(EntityPlayer player);

    // Change to use instance method from the provided instance
    public static void toggle(EntityPlayer player, FarmCrop cropInstance) {
        INSTANCE = cropInstance;
        isRunning = !isRunning;
        autoPaused = false;
        autoPLoopsCounter = 0;
        updateSettings();

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
        updateSettings();

        if (isRunning && wasPaused) {
            autoPaused = false;
            releaseAllKeys();
            wasPaused = false;
            lastStepTime = System.currentTimeMillis() - (
                    FarmCrop.INSTANCE.getCurrentStepDuration() - // Use instance reference
                            remainingStepTimeWhenPaused - 100);
            remainingStepTimeWhenPaused = 0;

            if(mc.currentScreen==null) {
                FarmCrop.INSTANCE.applyCurrentStepKeys(); // Use instance reference
            }

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

    // If you have methods that update config values:
    public static void updateSettings() {

        if (autoPauseLoops != config.autoPauseLoops){
            autoPauseLoops = config.autoPauseLoops;
//            mc.thePlayer.addChatMessage(new ChatComponentText("§6§lUPDATED! §6AutPauseLoops: §a§l" + autoPauseLoops));
        }

        if(autoPlay != config.autoPlay){
            autoPlay = config.autoPlay;
//            mc.thePlayer.addChatMessage(new ChatComponentText("§6§lUPDATED! §6Autoplay: §a§l" + autoPlay));
        }

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

        if (event.world.isRemote && !wasPaused) { // Client-side world load

            if (autoPlay) {
                autoPlay = false;
                mc.thePlayer.addChatMessage(new ChatComponentText("§6[KM] Detected world change! Auto Play §c§lDISABLED"));
            } else {
                mc.thePlayer.addChatMessage(new ChatComponentText("§6[KM] Detected world change!"));
            }

            pause();

            // Delay Hotbar Swap
            new Thread(() -> {
                try {
                    Thread.sleep(3000); // Wait for player entity to load
                    if (mc.thePlayer != null && mc.thePlayer.inventory != null) {
                        mc.thePlayer.inventory.currentItem = 8; // Select slot 9
                    }

                } catch (Exception e) {
                    System.err.println("Error Selecting Hotbar: " + e.getMessage());
                }
            }).start();
        }
    }


}