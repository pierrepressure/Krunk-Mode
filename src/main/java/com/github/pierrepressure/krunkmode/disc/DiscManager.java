package com.github.pierrepressure.krunkmode.disc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.integrated.IntegratedServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DiscManager {
    // Put your Discord webhook URLs here
    private static final String LJUrl = "https://discord.com/api/webhooks/1376973357706383393/TnTBmrL_2-BcQnVNt0OFZQL5EgCFQhth00WHvNrGycF0Ei4LPuHIHFYDOAy2h4DDrKKL";
    private static final String DUrl = "https://discord.com/api/webhooks/1377722534245437540/AhxbdhIMj2-7_UexOiUFncC68cV0drggxmsSnNbDLZsDcHnXHVdOp9cgeSKOz5eczyUM";

    // Track player join times
    private static final Map<String, Instant> playerJoinTimes = new ConcurrentHashMap<>();

    // Current player location
    private static String currentLocation = "Unknown";

    // Store last known player info for shutdown scenarios
    private static String lastKnownPlayerName = null;
    private static String lastKnownServerName = null;

    public static void sendPlayerJoinMessage(EntityPlayer player) {
        playerJoinTimes.put(player.getName(), Instant.now());
        String serverName = getServerName();

        // Store for potential shutdown use
        lastKnownPlayerName = player.getName();
        lastKnownServerName = serverName;

        String message = String.format("🟢 **%s** joined **%s**", player.getName(), serverName);
        sendWebhookMessage(message, 0x00FF00, LJUrl, false);
    }

    public static void sendPlayerLeaveMessage(EntityPlayer player) {
        sendPlayerLeaveMessage(player, false);
    }

    public static void sendPlayerLeaveMessage(EntityPlayer player, boolean isShutdown) {
        try {
            String playerName = player != null ? player.getName() : lastKnownPlayerName;
            if (playerName == null) return;

            Instant joinTime = playerJoinTimes.get(playerName);
            String serverName;

            try {
                serverName = getServerName();
                if (serverName == null || serverName.equals("Unknown Server")) {
                    serverName = lastKnownServerName != null ? lastKnownServerName : "Unknown Server";
                }
            } catch (Exception e) {
                serverName = lastKnownServerName != null ? lastKnownServerName : "Unknown Server";
            }

            String message;
            if (joinTime != null) {
                Duration sessionDuration = Duration.between(joinTime, Instant.now());
                String formattedDuration = formatDuration(sessionDuration);
                message = String.format("🔴 **%s** left **%s**  (played for %s)", playerName, serverName, formattedDuration);
                playerJoinTimes.remove(playerName);
            } else {
                message = String.format("🔴 **%s** left **%s**", playerName, serverName);
            }

            sendWebhookMessage(message, 0xFF0000, LJUrl, isShutdown);

        } catch (Exception e) {
            System.out.println("Error sending leave message: " + e.getMessage());
        }
    }

    // Method for shutdown hook with stored player info
    public static void sendShutdownLeaveMessage() {
        try {
            EntityPlayer player = null;
            try {
                player = Minecraft.getMinecraft().thePlayer;
            } catch (Exception e) {
                // Player might be null during shutdown
            }

            sendPlayerLeaveMessage(player, true);

        } catch (Exception e) {
            System.err.println("Error in shutdown leave message: " + e.getMessage());
        }
    }

    public static void sendDeathMessage(String playerName, String deathMessage) {
        try {
            String fullMessage = String.format(deathMessage);
            sendWebhookMessage(fullMessage, 0x8B0000, DUrl, false);
        } catch (Exception e) {
            System.out.println("Error sending death message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void sendWebhookMessage(String message, int color, String diskHook, boolean isShutdown) {
        if (isShutdown) {
            // For shutdown scenarios, use synchronous call with timeout
            sendWebhookMessageSync(message, color, diskHook);
        } else {
            // Normal async behavior
            sendWebhookMessageAsync(message, color, diskHook);
        }
    }

    private static void sendWebhookMessageSync(String message, int color, String diskHook) {
        CountDownLatch latch = new CountDownLatch(1);

        Thread webhookThread = new Thread(() -> {
            try {
                sendWebhookMessageInternal(message, color, diskHook);
            } finally {
                latch.countDown();
            }
        });

        webhookThread.setDaemon(false); // Important: non-daemon thread
        webhookThread.start();

        try {
            // Wait up to 3 seconds for the webhook to complete
            boolean completed = latch.await(3, TimeUnit.SECONDS);
            if (!completed) {
                System.err.println("Webhook timed out during shutdown");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void sendWebhookMessageAsync(String message, int color, String diskHook) {
        Thread webhookThread = new Thread(() -> sendWebhookMessageInternal(message, color, diskHook));
        webhookThread.setDaemon(true);
        webhookThread.start();
    }

    private static void sendWebhookMessageInternal(String message, int color, String diskHook) {
        try {
            URL url = new URL(diskHook);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "KrunkMode");
            connection.setDoOutput(true);

            // Set shorter timeouts for shutdown scenarios
            connection.setConnectTimeout(2000); // 2 seconds
            connection.setReadTimeout(3000);    // 3 seconds

            // Create Discord embed
            JsonObject embed = new JsonObject();
            embed.addProperty("description", message);
            embed.addProperty("color", color);
            embed.addProperty("timestamp", Instant.now().toString());

            // Add footer with mod info
            JsonObject footer = new JsonObject();
            footer.addProperty("text", "Krunk Mode");
            embed.add("footer", footer);

            // Create embeds array
            JsonArray embedsArray = new JsonArray();
            embedsArray.add(embed);

            // Create main payload
            JsonObject payload = new JsonObject();
            payload.addProperty("username", "Krunk Mode");
            payload.add("embeds", embedsArray);

            // Send the request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Check response
            int responseCode = connection.getResponseCode();
            if (responseCode != 200 && responseCode != 204) {
                System.err.println("Discord webhook failed with response code: " + responseCode);

                // Read error response
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    System.err.println("Error response: " + response.toString());
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to send Discord webhook: " + e.getMessage());
        }
    }

    /**
     * Format duration into a human-readable string
     */
    private static String formatDuration(Duration duration) {
        long totalSeconds = duration.getSeconds();

        if (totalSeconds < 60) {
            return totalSeconds + " second" + (totalSeconds == 1 ? "" : "s");
        }

        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        if (minutes < 60) {
            if (seconds == 0) {
                return minutes + " minute" + (minutes == 1 ? "" : "s");
            } else {
                return String.format("%d minute%s and %d second%s",
                        minutes, (minutes == 1 ? "" : "s"),
                        seconds, (seconds == 1 ? "" : "s"));
            }
        }

        long hours = minutes / 60;
        minutes = minutes % 60;

        if (hours < 24) {
            if (minutes == 0) {
                return hours + " hour" + (hours == 1 ? "" : "s");
            } else {
                return String.format("%d hour%s and %d minute%s",
                        hours, (hours == 1 ? "" : "s"),
                        minutes, (minutes == 1 ? "" : "s"));
            }
        }

        long days = hours / 24;
        hours = hours % 24;

        if (hours == 0) {
            return days + " day" + (days == 1 ? "" : "s");
        } else {
            return String.format("%d day%s and %d hour%s",
                    days, (days == 1 ? "" : "s"),
                    hours, (hours == 1 ? "" : "s"));
        }
    }

    private static String getServerName() {
        try {
            Minecraft mc = Minecraft.getMinecraft();

            if (mc.isSingleplayer()) {
                IntegratedServer integratedServer = mc.getIntegratedServer();

                if (integratedServer != null) {
                    String worldName = integratedServer.getWorldName();

                    if (worldName != null && !worldName.isEmpty()) {
                        return worldName.replaceAll("§.", "").trim() + " (Singleplayer)";
                    }
                }

                return "Single Player World";

            } else {
                ServerData serverData = mc.getCurrentServerData();
                if (serverData != null) {
                    String name = serverData.serverName;

                    if (name == null || name.isEmpty() || name.equalsIgnoreCase("Minecraft Server")) {
                        name = serverData.serverIP;
                    }

                    return name.replaceAll("§.", "").trim();
                }
                return "Multiplayer Server";
            }

        } catch (Exception e) {
            System.out.println("Error getting server name: " + e.getMessage());
        }
        return "Multiplayer Server";
    }

    public static void handleDeathMessage(String deathMessage) {
        try {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            if (player == null) return;

            String playerName = player.getName();

            String DEATH_MESSAGE_PREFIX = "§r§c ☠ §r§7You";

            String cleanedMessage = deathMessage.replace(DEATH_MESSAGE_PREFIX, "")
                    .replaceAll("§[0-9a-fk-or]", "")
                    .trim();

            String processedMessage = cleanedMessage
                    .replace("You ", playerName + " ")
                    .replace("were", "was")
                    .replace("your", "their")
                    .replace(".", "")
                    .replace("and became a ghost", "");

            updateCurrentLocation();

            String discordMessage;
            if (!currentLocation.equals("Unknown") && !currentLocation.isEmpty()) {
                discordMessage = String.format("💀 **%s** %s in **%s**", playerName, processedMessage, currentLocation);
            } else {
                discordMessage = String.format("💀 **%s** %s", playerName, processedMessage);
            }

            sendDeathMessage(playerName, discordMessage);

        } catch (Exception e) {
            System.err.println("Error processing death message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void updateCurrentLocation() {
        try {
            Collection<NetworkPlayerInfo> players = Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap();
            for (NetworkPlayerInfo player : players) {
                if (player == null || player.getDisplayName() == null) continue;
                String text = player.getDisplayName().getUnformattedText();
                if (text.startsWith("Area: ") || text.startsWith("Dungeon: ")) {
                    currentLocation = text.substring(text.indexOf(":") + 2);
                    return;
                }
            }
            currentLocation = "Unknown";
        } catch (Exception e) {
            System.err.println("Error getting location from tab: " + e.getMessage());
            currentLocation = "Unknown";
        }
    }
}