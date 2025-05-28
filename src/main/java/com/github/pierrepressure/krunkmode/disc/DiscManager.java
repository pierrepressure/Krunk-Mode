package com.github.pierrepressure.krunkmode.disc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DiscManager {
    // Put your Discord webhook URL here
    private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/1376973357706383393/TnTBmrL_2-BcQnVNt0OFZQL5EgCFQhth00WHvNrGycF0Ei4LPuHIHFYDOAy2h4DDrKKL";

    // Track player join times
    private static final Map<String, Instant> playerJoinTimes = new ConcurrentHashMap<>();

    public static void sendPlayerJoinMessage(EntityPlayer player) {
        playerJoinTimes.put(player.getName(), Instant.now());
        String serverName = getServerName();

        String message = String.format("🟢 **%s** joined **%s**", player.getName(), serverName);
        sendWebhookMessage(message, 0x00FF00);
    }

    public static void sendPlayerLeaveMessage(EntityPlayer player) {
        try {
            String playerName = player.getName();
            Instant joinTime = playerJoinTimes.get(playerName);
            String serverName;

            try {
                serverName = getServerName();
            } catch (Exception e) {
                // Fallback if server name can't be retrieved (during shutdown)
                serverName = "Unknown Server";
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

            sendWebhookMessage(message, 0xFF0000);

        } catch (Exception e) {
            System.err.println("Error sending leave message: " + e.getMessage());
        }
    }

    private static void sendWebhookMessage(String message, int color) {
        // Run in separate thread to avoid blocking the game
        new Thread(() -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "KrunkMode");
                connection.setDoOutput(true);

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

                // Check response and log details
                int responseCode = connection.getResponseCode();
                if (responseCode != 200 && responseCode != 204) {
                    System.err.println("Discord webhook failed with response code: " + responseCode);

                    // Read error response
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    System.err.println("Error response: " + response.toString());
                } else {
                    //System.out.println("Discord webhook sent successfully for: " + message);
                }

            } catch (Exception e) {
                System.err.println("Failed to send Discord webhook: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
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
                // Get the integrated server instance
                IntegratedServer integratedServer = mc.getIntegratedServer();

                if (integratedServer != null) {
                    // Get the actual world name
                    String worldName = integratedServer.getWorldName();

                    // Clean formatting codes and return if valid
                    if (worldName != null && !worldName.isEmpty()) {
                        return worldName.replaceAll("§.", "").trim() + " (Singleplayer)";
                    }
                }

                // Fallback if no server instance or invalid name
                return "Single Player World";

            } else {
                ServerData serverData = mc.getCurrentServerData();
                if (serverData != null) {
                    // Use server IP as fallback if name isn't available
                    String name = serverData.serverName;

                    if (name == null || name.isEmpty()||name.equalsIgnoreCase("Minecraft Server")) {
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


}