package com.github.pierrepressure.krunkmode;

import com.github.pierrepressure.krunkmode.disc.DiscManager;
import com.github.pierrepressure.krunkmode.features.farming.FarmCrop;
import com.github.pierrepressure.krunkmode.features.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class EventListener {
    private static final java.util.Random RANDOM = new java.util.Random();
    private static final int CHANCE = 1728000;
    private static final String[] trollMsg = {"§aFriend > §r§cJayavarmen §r§eleft.§r",
            "§f                     §7You have §a0 §7items stashed away!§r\n" +
                    "§f                §6§l>>> §6§lCLICK HERE§e to pick them up! §6§l<<<§r",
    "§r§aYour §r§5Great Spook Tree §r§ahas just sprouted a fruit!§r",
    "§r§c[Important] §r§eThis server will restart soon: §r§bScheduled Reboot§r\n" +
            "§eYou have §a10 seconds §eto warp out! §a§l§nCLICK§e to warp now!§r",
            "§b§l---------------------------------------------\n" +
                    "§aYou have just received §60 coins §aas bank interest!\n" +
                    "§b§l---------------------------------------------",
            "§6§lALLOWANCE! §eYou earned §6§l-2,000 coins§e!",
            "§r§8[§r§f42§r§8] §r§7Olly46§7§r§7: anyone gifting vip?§r",
            "§aFriend > §r§bBlueSquire §r§ejoined.§r",
            "§r§6§lRARE DROP! §r§a                   §r§b(+§r§b246% §r§b✯ Magic Find§r§b)§r",
            "§r§cPest Repellent MAX expires in §r§e30s§r§c§r"
    };



    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {

        // Only call FisherManager if it's enabled
        if (FisherManager.INSTANCE.isEnabled()) {
            FisherManager.INSTANCE.onTick(event);
        }

        // Only call FarmMelon if it's running
        if (FarmCrop.isRunning() && FarmCrop.getInstance() != null) {
            FarmCrop.getInstance().onTick(event);
        }

        // Only call Clicker if it's running
        if (ClickerManager.INSTANCE.isEnabled()) ClickerManager.INSTANCE.onTick(event);

        // Only call Clicker if it's running
        if (MinerManager.INSTANCE.isEnabled()) MinerManager.INSTANCE.onTick(event);

        //trolling
        if (Minecraft.getMinecraft().thePlayer != null &&
                Minecraft.getMinecraft().thePlayer.getName().equals("BluesSquire") &&
                RANDOM.nextInt(CHANCE) == 0) {

            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(trollMsg[RANDOM.nextInt(trollMsg.length)]));

        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (FarmCrop.isRunning()) {
            FarmCrop.INSTANCE.onWorldLoad(event);
        }
    }

    @SubscribeEvent
    public void onSound(PlaySoundEvent event) {
        if (FisherManager.INSTANCE.isEnabled()) {
            FisherManager.INSTANCE.onSound(event);
        }
    }

    @SubscribeEvent
    public void trackKeyInputs(InputEvent.KeyInputEvent event) {
        SneakerManager.trackKeyInputs(event);
    }


    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            DiscManager.sendPlayerLeaveMessage(player);
            System.out.println("DISCONNECT: message sent");
        }
    }

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        // Delay join message to ensure player entity exists
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Wait for player entity to load
                EntityPlayer player = Minecraft.getMinecraft().thePlayer;
                if (player != null) {
                    DiscManager.sendPlayerJoinMessage(player);
                }
            } catch (Exception e) {
                System.err.println("Error sending join message: " + e.getMessage());
            }
        }).start();
    }

    // Add shutdown hook in your main mod class initialization
    public static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {

                EntityPlayer player = Minecraft.getMinecraft().thePlayer;
                if (player != null) {

                    DiscManager.sendPlayerLeaveMessage(player);

                }
            } catch (Exception e) {
                System.err.println("Error in shutdown hook: " + e.getMessage());
            }
        }));
    }

}
