package com.github.pierrepressure.krunkmode.features;

import com.github.pierrepressure.krunkmode.KrunkMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class LobbyTracker {
    // Change this to lazy initialization
    private static LobbyTracker instance;

    // Add this method for lazy initialization
    public static LobbyTracker getInstance() {
        if (instance == null) {
            instance = new LobbyTracker();
        }
        return instance;
    }

    // Keep the old INSTANCE field for compatibility but make it lazy
    public static final LobbyTracker INSTANCE = getInstance();

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Pattern COLOR_PATTERN = Pattern.compile("§[0-9a-fk-or]");

    private boolean looking = false;
    private int attempt = 0;
    private boolean found = false;
    private String area = "";
    private String server = "";
    private String currentServer = ""; // Track current server to detect changes
    private String previousServer = ""; // Track the server we just left
    private Map<String, Long> serverLeaveTimes = new ConcurrentHashMap<>(); // Temporary storage - resets on client restart
    private boolean sentMessage = false;
    private int tickCounter = 0;

    private LobbyTracker() {
        // Initialize with empty map - will reset on each client restart
        serverLeaveTimes = new ConcurrentHashMap<>();
    }

    public void onWorldLoad(WorldEvent.Load event) {
        // Add null check for config
        if (KrunkMode.config == null || !KrunkMode.config.lobbyTrackingEnabled) return;

        // Reset all tracking state
        found = false;
        looking = true;
        attempt = 0;
        sentMessage = false;
        tickCounter = 0;
        area = "";
        server = "";
        // Don't reset currentServer here - we want to track server changes
    }

    public void onTick(TickEvent.ClientTickEvent event) {
        // Add null check for config
        if (KrunkMode.config == null || !KrunkMode.config.lobbyTrackingEnabled || event.phase != TickEvent.Phase.START) return;

        tickCounter++;

        // Check every 10 ticks (0.5 seconds) instead of every tick for performance
        if (tickCounter % 10 != 0) return;

        if (looking) {
            checkTabListForServerInfo();

            if (attempt > 15) {
                looking = false;
            }
            attempt++;
        }

        if (found && !sentMessage) {
            processServerInfo();
        }
    }

    private void checkTabListForServerInfo() {
        if (mc.thePlayer == null || mc.thePlayer.sendQueue == null) return;

        try {
            GuiPlayerTabOverlay tabOverlay = mc.ingameGUI.getTabList();
            Collection<NetworkPlayerInfo> playerInfos = mc.thePlayer.sendQueue.getPlayerInfoMap();

            List<String> tabNames = new ArrayList<>();

            for (NetworkPlayerInfo info : playerInfos) {
                String displayName = tabOverlay.getPlayerName(info);
                if (displayName != null) {
                    String cleanName = stripColors(displayName);
                    if (cleanName.contains("Area: ") || cleanName.contains("Server: ")) {
                        tabNames.add(cleanName);
                    }
                }
            }

            if (!tabNames.isEmpty()) {
                looking = false;
                found = true;
                sentMessage = false;

                // Parse the server information
                for (String name : tabNames) {
                    if (name.contains("Area: ")) {
                        area = name.replace("Area: ", "").trim();
                    } else if (name.contains("Server: ")) {
                        server = name.replace("Server: ", "").replace(" ", "").trim();
                    }
                }
            }

        } catch (Exception e) {
            // Silently handle any exceptions to avoid spam
        }
    }

    private void processServerInfo() {
        if (server != null && !server.isEmpty()) {
            // Check if we've switched to a different server
            boolean serverChanged = !server.equals(currentServer);

            if (serverChanged) {
                // Record the leave time for the previous server (if there was one)
                if (!currentServer.isEmpty()) {
                    serverLeaveTimes.put(currentServer, System.currentTimeMillis());
                    previousServer = currentServer;
                }

                // Check if we're returning to a server we've left before (only in current session)
                if (serverLeaveTimes.containsKey(server)) {
                    long lastLeft = serverLeaveTimes.get(server);
                    String timeAgo = getReadableTime(System.currentTimeMillis() - lastLeft);

                    //Return if it's been too long
                    if((System.currentTimeMillis() - lastLeft)>3600000) return;

                    // Use the new chat message format
                    mc.thePlayer.addChatMessage(new ChatComponentText(String.format("§6[KM] You've been on this server! §a%s"+"\n"+"§6Last here: §a%s §6ago", server, timeAgo)));
                }

                // Update current server
                currentServer = server;
            }

            sentMessage = true;
        }
    }

    private String stripColors(String text) {
        if (text == null) return "";
        return COLOR_PATTERN.matcher(text).replaceAll("");
    }

    private String getReadableTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " day" + (days == 1 ? "" : "s");
        } else if (hours > 0) {
            return hours + " hour" + (hours == 1 ? "" : "s");
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes == 1 ? "" : "s");
        } else {
            return seconds + " second" + (seconds == 1 ? "" : "s");
        }
    }

    private void sendChatMessage(String message) {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(message));
        }
    }

    // Public method to clear server history (clears current session data)
    public void clearServerHistory() {
        serverLeaveTimes.clear();
        currentServer = "";
        previousServer = "";
        mc.thePlayer.addChatMessage(new ChatComponentText("§6[KM] §aServer history cleared!"));
    }

    // Public method to get current server info (for debugging)
    public String getCurrentServerInfo() {
        return "Area: " + area + ", Server: " + server;
    }
}