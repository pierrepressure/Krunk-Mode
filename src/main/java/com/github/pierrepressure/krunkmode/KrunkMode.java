package com.github.pierrepressure.krunkmode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


@Mod(
        modid = "krunkmode",
        name = "Krunk Mode",  // ← Change this
        version = "69.0" // ← Change this
)
public class KrunkMode {

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("Dirt: " + Blocks.dirt.getUnlocalizedName());
		// Below is a demonstration of an access-transformed class access.
        System.out.println("Color State: " + new GlStateManager.Color());


        // register tick event listener
        MinecraftForge.EVENT_BUS.register(new EventListener());

        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new FishCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new TestCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new SigmaCommand());
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new GuiCommand());

        MinecraftForge.EVENT_BUS.register(this);

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

