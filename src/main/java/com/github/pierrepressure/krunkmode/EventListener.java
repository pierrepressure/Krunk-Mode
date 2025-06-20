package com.github.pierrepressure.krunkmode;

import com.github.pierrepressure.krunkmode.disc.DiscManager;
import com.github.pierrepressure.krunkmode.features.farming.FarmCrop;
import com.github.pierrepressure.krunkmode.features.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
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

    private static final Minecraft mc = Minecraft.getMinecraft();

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

        // Call LobbyTracker if it's enabled
        if (KrunkMode.config.lobbyTrackingEnabled) {
            LobbyTracker.INSTANCE.onTick(event);
        }

        // Call LobbyTracker if it's enabled
        if (KrunkMode.config.chatEmotes) {
            EmoteManager.INSTANCE.onTick(event);
        }

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

        if (KrunkMode.config.lobbyTrackingEnabled) {
            LobbyTracker.INSTANCE.onWorldLoad(event);
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
        EntityPlayer player = mc.thePlayer;
        if (player != null) {
            DiscManager.sendPlayerLeaveMessage(player);
        }
    }

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        // Delay join message to ensure player entity exists
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Wait for player entity to load
                EntityPlayer player = mc.thePlayer;

                if (player != null) {
                    DiscManager.sendPlayerJoinMessage(player);
                }

            } catch (Exception e) {
                System.err.println("Error sending join message: " + e.getMessage());
            }
        }).start();
    }


    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (event.type != 0) return; // Only handle normal chat messages

        IChatComponent message = event.message;
        String formattedMessage = message.getFormattedText();

        // Pattern 2: Check unformatted message for skull symbol and "You"
        if (formattedMessage.contains("☠") && formattedMessage.contains("You") && !formattedMessage.contains(":")&& !formattedMessage.contains("Onyx")) {
            DiscManager.handleDeathMessage(formattedMessage);
        }
    }


    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if(KrunkMode.config.autoExperimentsEnabled) {
            ExperimentManager.onGuiOpen(event);
        }

    }

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if(KrunkMode.config.autoExperimentsEnabled) {
            ExperimentManager.onGuiDraw(event);
        }
    }

//    @SubscribeEvent
//    public void onGuiOpen(GuiOpenEvent event) {
//
//        // Check if the GUI is closing and KrunkMode config is available
//        if (event.gui != null || KrunkMode.config == null || mc.thePlayer == null) return;
//
//        // Check if both are non-null and of the same class
//        if (mc.currentScreen != null && KrunkMode.config.gui() != null &&
//                (mc.currentScreen instanceof gg.essential.vigilance.gui.SettingsGui ||
//                        mc.currentScreen.getClass().getName().equals("cc.polyfrost.oneconfig.gui.OneConfigGui"))) {
//
//            ClickerManager.updateSettings();
//            FarmCrop.updateSettings();
//        }
//    }

    // Add shutdown hook in your main mod class initialization
    public static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Shutdown hook triggered - sending leave message");

                // Use the new shutdown-specific method
                if(mc.theWorld!=null) DiscManager.sendShutdownLeaveMessage();


                // Give a small buffer to ensure the message is sent
                Thread.sleep(100);

            } catch (Exception e) {
                System.err.println("Error in shutdown hook: " + e.getMessage());
            }
        }));
    }

}
