package com.github.pierrepressure.krunkmode;

import com.github.pierrepressure.krunkmode.commands.*;
import com.github.pierrepressure.krunkmode.commands.warp.WarpCommandHandler;
import com.github.pierrepressure.krunkmode.features.farming.FarmCrop;
import com.github.pierrepressure.krunkmode.features.ClickerManager;
import com.github.pierrepressure.krunkmode.features.SneakerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
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
        version = "1.0.5"// ← Change this
)
public class KrunkMode { // .\gradlew.bat           .\gradlew --version

    // Centralized configuration
    public static KrunkModeConfig config;

    // Warp command handler
    public static WarpCommandHandler warpCommandHandler;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        // Initialize config file using configuration class
        File configFile = new File(event.getModConfigurationDirectory(), "krunkmode.cfg");
        config = new KrunkModeConfig(configFile);

        // Initialize modules with config values
        FarmCrop.init(config);
        ClickerManager.init(config);
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

        // Initialize and register warp commands
        warpCommandHandler = new WarpCommandHandler();

        // Add shutdown hook for handling forced closures
        EventListener.addShutdownHook();

        ClientRegistry.registerKeyBinding(SneakerManager.toggleSneakKey);
        ClientRegistry.registerKeyBinding(SneakerManager.toggleSprintKey);

        // Register commands
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new FishCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new FarmCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new CrashCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new SigmaCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new HelpCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new MenuCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new ClickerCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new MinerCommand());
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

