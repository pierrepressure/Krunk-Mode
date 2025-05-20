package com.github.pierrepressure.krunkmode;

import com.github.pierrepressure.krunkmode.commands.*;
import com.github.pierrepressure.krunkmode.features.FarmCrop;
import com.github.pierrepressure.krunkmode.features.FarmMelon;
import com.github.pierrepressure.krunkmode.features.FishManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;


@Mod(
        modid = "krunkmode",
        name = "Krunk Mode",  // ← Change this
        version = "1.0.4"// ← Change this
)
public class KrunkMode { // .\gradlew.bat           .\gradlew --version

    // Centralized configuration
    public static KrunkModeConfig config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        // Initialize config file using configuration class
        File configFile = new File(event.getModConfigurationDirectory(), "krunkmode.cfg");
        config = new KrunkModeConfig(configFile);

        // Initialize modules with config values
        FarmCrop.init(config);
    }

    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        if (config != null) {
            config.save();
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

        // Register event listeners
        MinecraftForge.EVENT_BUS.register(new EventListener());
        MinecraftForge.EVENT_BUS.register(this);

        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onSound(PlaySoundEvent event) {
                FishManager.INSTANCE.onSound(event);
            }

            @SubscribeEvent
            public void onWorldLoad(WorldEvent.Load event) {
                FarmMelon.INSTANCE.onWorldLoad(event);
            }
        });


        // Register commands
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new FishCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new FarmCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new CrashCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new SigmaCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new HelpCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new MenuCommand());
    }

    // In your main mod class
    public static GuiScreen screenToOpenNextTick = null;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;

        if (screenToOpenNextTick != null) {
            Minecraft.getMinecraft().displayGuiScreen(screenToOpenNextTick);
            screenToOpenNextTick = null;
        }

    }


}

