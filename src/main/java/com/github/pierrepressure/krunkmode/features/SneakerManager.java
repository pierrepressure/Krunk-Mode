package com.github.pierrepressure.krunkmode.features;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public enum SneakerManager {

    INSTANCE;

    public static final KeyBinding toggleSneakKey = new KeyBinding("Toggle Sneak", Keyboard.KEY_NONE, "KrunkMode");
    public static final KeyBinding toggleSprintKey = new KeyBinding("Toggle Sprint", Keyboard.KEY_NONE, "KrunkMode");


    public static void trackKeyInputs(InputEvent.KeyInputEvent event) {

        if (toggleSneakKey.isPressed()) {
            GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
            KeyBinding keyBindSneak = gameSettings.keyBindSneak;


            KeyBinding.setKeyBindState(keyBindSneak.getKeyCode(), !keyBindSneak.isKeyDown());

        } else if (toggleSprintKey.isPressed()) {

            GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
            KeyBinding keyBindSprint = gameSettings.keyBindSprint;


            KeyBinding.setKeyBindState(keyBindSprint.getKeyCode(), !keyBindSprint.isKeyDown());
        }
    }

}