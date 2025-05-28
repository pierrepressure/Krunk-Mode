package com.github.pierrepressure.krunkmode.features;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public enum MinerManager {
    INSTANCE;

    private final Minecraft mc = Minecraft.getMinecraft();
    private final KeyBinding attackKey = mc.gameSettings.keyBindAttack;
    public boolean isEnabled = false;
    public void toggle() {
        isEnabled = !isEnabled;

        if (mc.thePlayer != null) {
            String status = isEnabled ? "§a§lENABLED" : "§c§lDISABLED";
            mc.thePlayer.addChatMessage(new ChatComponentText("§l§6[KM] Miner " + status));
        }

        // Always release key when disabling
        if (!isEnabled) {
            KeyBinding.setKeyBindState(attackKey.getKeyCode(), false);
        }
    }

    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;

        if (mc.currentScreen != null && attackKey.isPressed()) {
            KeyBinding.setKeyBindState(attackKey.getKeyCode(), false);
        }

        //Also Hold Break
        if (!mc.gameSettings.keyBindAttack.isPressed()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
        }


    }

    public boolean isEnabled() {
        return isEnabled;
    }
}