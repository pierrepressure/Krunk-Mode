package com.github.pierrepressure.krunkmode;

import com.github.pierrepressure.krunkmode.commands.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


@Mod(
        modid = "krunkmode",
        name = "Krunk Mode",  // ← Change this
        version = "1.0.1" // ← Change this
)
public class KrunkMode {

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

        // Register event listeners
        MinecraftForge.EVENT_BUS.register(new EventListener());
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(SimpleFishManager.INSTANCE);


        // Register commands
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new FishCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new FarmCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new CrashCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new SigmaCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new HelpCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new GuiCommand());
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

