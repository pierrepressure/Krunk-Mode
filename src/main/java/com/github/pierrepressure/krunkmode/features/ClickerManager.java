package com.github.pierrepressure.krunkmode.features;

import com.github.pierrepressure.krunkmode.VigilanceConfig;
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

    private static VigilanceConfig config;

    public static void init(VigilanceConfig config) {
        ClickerManager.config = config;
        maxCps = config.cps + 2;
        minCps = config.cps - 2;
    }

    public void toggle() {
        enabled = !enabled;
        updateSettings();
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
        double standardDeviation;

        // If delays are equal (minCps == maxCps), add a small artificial deviation
        if (minDelay == maxDelay) {
            standardDeviation = minDelay * 0.45;
        } else {
            standardDeviation = (maxDelay - minDelay) / 6.0;
        }

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


    // If you have methods that update config values:
    public static void updateSettings() {

        if (minCps != config.cps - 2 || maxCps != config.cps + 2) {
            maxCps = config.cps + 2;
            minCps = config.cps - 2;
//            mc.thePlayer.addChatMessage(new ChatComponentText("§6§lUPDATED! §6MinCPS: §a§l"
//                    + minCps+ " §6MaxCPS: §a§l"+maxCps));
        }


    }


}

