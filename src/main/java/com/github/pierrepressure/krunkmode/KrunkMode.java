package com.github.pierrepressure.krunkmode;

import com.github.pierrepressure.krunkmode.commands.*;
import com.github.pierrepressure.krunkmode.commands.warp.WarpCommandHandler;
import com.github.pierrepressure.krunkmode.features.ExperimentManager;
import com.github.pierrepressure.krunkmode.features.FisherManager;
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


@Mod(
        modid = "krunkmode",
        name = "Krunk Mode",
        version = "1.0.6"
)
public class KrunkMode {

    // Replace the old config with Vigilance config
    public static VigilanceConfig config;
    // Warp command handler
    public static WarpCommandHandler warpCommandHandler;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Initialize Vigilance config (it handles file creation automatically)
        config = new VigilanceConfig();

        // Initialize modules with config values
        // Note: You may need to update these methods to use the new config structure
        FarmCrop.init(config);
        ClickerManager.init(config);
        FisherManager.init(config);
        ExperimentManager.init(config);
    }


    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        if (config != null) {
            config.save(); // Save config on server stop
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

        // Register key bindings
        ClientRegistry.registerKeyBinding(SneakerManager.toggleSneakKey);
        ClientRegistry.registerKeyBinding(SneakerManager.toggleSprintKey);

        // Register all commands
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new FishCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new FarmCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new CrashCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new SigmaCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new HelpCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new MenuCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new ClickerCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new MinerCommand());
    }

    // Screen scheduling for safe GUI opening
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