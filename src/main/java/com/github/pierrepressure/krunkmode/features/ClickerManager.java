package com.github.pierrepressure.krunkmode.features;

import com.github.pierrepressure.krunkmode.KrunkModeConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.util.Random;

public enum ClickerManager {
    INSTANCE;

    private static final Minecraft mc = Minecraft.getMinecraft();
    private final Random random = new Random();
    private boolean enabled = false;
    private static int maxCps = 10;
    private static int minCps = 6;
    private long nextLeftClickTime = 0;
    private long nextRightClickTime = 0;

    private static KrunkModeConfig config;

    public static void init(KrunkModeConfig config) {
        ClickerManager.config = config;
        maxCps = config.getMaxCps();
        minCps = config.getMinCps();
    }

    public void toggle() {
        enabled = !enabled;
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    "§l§6[KM] Clicker " + (enabled ? "§a§lENABLED" : "§c§lDISABLED")
            ));
        }
    }

    public void onTick(TickEvent.ClientTickEvent event) {
        if (!enabled || mc.thePlayer == null || event.phase == TickEvent.Phase.END) return;
        if (mc.currentScreen != null) return;

        long now = System.currentTimeMillis();

        // Handle left click
        if (Mouse.isButtonDown(0) && now >= nextLeftClickTime) {
            simulateClick(true);
            nextLeftClickTime = now + getRandomDelay();
        }

        // Handle right click
        if (Mouse.isButtonDown(1) && now >= nextRightClickTime) {
            simulateClick(false);
            nextRightClickTime = now + getRandomDelay();
        }
    }

    private int getRandomDelay() {

        int minDelay = (int) (1000.0 / maxCps); // Max cps
        int maxDelay = (int) (1000.0 / minCps); // Min cps


        double mean = (minDelay + maxDelay) / 2.0;
        double standardDeviation = (maxDelay - minDelay) / 6.0;


        double gaussianValue = random.nextGaussian() * standardDeviation + mean;

        return (int) Math.max(minDelay, Math.min(maxDelay, gaussianValue));
    }

    private void simulateClick(boolean isLeftClick) {
        KeyBinding key = isLeftClick ?
                mc.gameSettings.keyBindAttack :
                mc.gameSettings.keyBindUseItem;

        // Press and release in the same tick
        KeyBinding.setKeyBindState(key.getKeyCode(), true);
        KeyBinding.onTick(key.getKeyCode()); // Process action
        KeyBinding.setKeyBindState(key.getKeyCode(), false);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public static void setMaxCps(int cps){
        maxCps = Math.min(cps,20); //Capping cps at 20
        if (config != null) config.setMaxCps(cps);
        mc.thePlayer.addChatMessage(new ChatComponentText(
                String.format("§6[KM] Clicker Max CPS: §a§l%d", maxCps) // Use instance reference
        ));
    }

    public static void setMinCps(int cps){
        minCps = Math.min(Math.max(maxCps-1,0),cps); //Capping max cps
        if (config != null) config.setMinCps(cps);
        mc.thePlayer.addChatMessage(new ChatComponentText(
                String.format("§6[KM] Clicker Min CPS: §a§l%d", minCps) // Use instance reference
        ));
    }

    public static int getMaxCps(){
        return maxCps;
    }

    public static int getMinCps(){
        return minCps;
    }

}