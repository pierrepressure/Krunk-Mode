package com.github.pierrepressure.krunkmode;

import com.github.pierrepressure.krunkmode.features.MinerManager;
import com.google.gson.reflect.TypeToken;

import java.util.*;
import java.io.*;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class WaypointParser {

    public static List<BlockPos> parseWaypoints(String data) throws IllegalArgumentException {
        List<BlockPos> positions;

        if (data.startsWith("<Skytils-Waypoint-Data>(V")) {
            throw new IllegalArgumentException("Unsupported Skytils V1 format");
        } else if (data.startsWith("eyJ")) { // Old Skytils (Base64 JSON)
            positions = parseBase64Json(data);
        } else if (data.startsWith("[{")) { // Soopy array format
            positions = parseSoopyArray(data);
        } else if (data.startsWith("AQAA")) { // Binary Soopy format
            positions = parseBinarySoopy(data);
        } else if (data.startsWith("{")) { // JSON object format
            positions = parseSoopyObject(data);
        } else { // Row-based format
            positions = parseRowBased(data);
        }

        // Add all parsed coordinates to MinerManager
        addCoordinatesToMinerManager(positions);

        return positions;
    }

    private static void addCoordinatesToMinerManager(List<BlockPos> positions) {
        if (positions == null || positions.isEmpty()) {
            return;
        }

        // Convert custom BlockPos to Minecraft BlockPos and add to MinerManager
        for (BlockPos pos : positions) {
            MinerManager.INSTANCE.addTargetCoordinate(pos.x, pos.y, pos.z);
        }

        System.out.println("[WaypointParser] Added " + positions.size() + " coordinates to MinerManager");
    }

    // Method to clear existing coordinates and load new ones
    public static List<BlockPos> replaceWaypoints(String data) throws IllegalArgumentException {
        // Clear existing coordinates first
        MinerManager.INSTANCE.clearTargetCoordinates();
        System.out.println("[WaypointParser] Cleared existing coordinates from MinerManager");

        // Parse and add new coordinates
        return parseWaypoints(data);
    }

    private static List<BlockPos> parseBase64Json(String data) {
        byte[] decodedBytes = Base64.getDecoder().decode(data);
        String json = new String(decodedBytes, StandardCharsets.UTF_8);
        return extractFromJson(json);
    }

    private static List<BlockPos> parseSoopyArray(String data) {
        List<Map<String, Object>> waypoints = parseJson(data, new TypeToken<List<Map<String, Object>>>(){}.getType());
        List<BlockPos> positions = new ArrayList<>();
        for (Map<String, Object> wp : waypoints) {
            positions.add(new BlockPos(
                    getInt(wp.get("x")),
                    getInt(wp.get("y")),
                    getInt(wp.get("z"))
            ));
        }
        return positions;
    }

    private static List<BlockPos> parseBinarySoopy(String data) {
        byte[] bytes = Base64.getDecoder().decode(data);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);

        try {
            if (dis.readByte() != 1) throw new IllegalArgumentException("Invalid data version");
            int count = dis.readInt();
            List<BlockPos> positions = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                dis.readUTF(); // Skip ID
                positions.add(new BlockPos(
                        (int) dis.readFloat(),
                        (int) dis.readFloat(),
                        (int) dis.readFloat()
                ));
                // Skip remaining fields
                dis.skipBytes(3);
                dis.readUTF();
                dis.readUTF();
            }
            return positions;
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid binary format", e);
        }
    }

    private static List<BlockPos> parseSoopyObject(String data) {
        Map<String, Map<String, Object>> waypoints = parseJson(data, new TypeToken<Map<String, Map<String, Object>>>(){}.getType());
        List<BlockPos> positions = new ArrayList<>();
        for (Map<String, Object> wp : waypoints.values()) {
            positions.add(new BlockPos(
                    getInt(wp.get("x")),
                    getInt(wp.get("y")),
                    getInt(wp.get("z"))
            ));
        }
        return positions;
    }

    private static List<BlockPos> parseRowBased(String data) {
        List<BlockPos> positions = new ArrayList<>();
        String[] lines = data.split("\\r?\\n");

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split(" ");
            if (parts.length < 3) continue;

            positions.add(new BlockPos(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])
            ));
        }
        return positions;
    }

    private static List<BlockPos> extractFromJson(String json) {
        Map<String, Object> jsonMap = parseJson(json, new TypeToken<Map<String, Object>>(){}.getType());
        List<Map<String, Object>> categories = (List<Map<String, Object>>) jsonMap.get("categories");
        List<BlockPos> positions = new ArrayList<>();

        for (Map<String, Object> category : categories) {
            List<Map<String, Object>> waypoints = (List<Map<String, Object>>) category.get("waypoints");
            for (Map<String, Object> wp : waypoints) {
                positions.add(new BlockPos(
                        getInt(wp.get("x")),
                        getInt(wp.get("y")),
                        getInt(wp.get("z"))
                ));
            }
        }
        return positions;
    }

    private static int getInt(Object value) {
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) return Integer.parseInt((String) value);
        throw new IllegalArgumentException("Invalid number format");
    }

    // Helper method for JSON parsing
    private static <T> T parseJson(String json, java.lang.reflect.Type type) {
        try {
            // In a real implementation, use Gson/Jackson here
            // This is simplified for demonstration
            return new com.google.gson.Gson().fromJson(json, type);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON format", e);
        }
    }

    public static class BlockPos {
        public final int x, y, z;
        public BlockPos(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public String toString() {
            return String.format("BlockPos(%d, %d, %d)", x, y, z);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            BlockPos blockPos = (BlockPos) obj;
            return x == blockPos.x && y == blockPos.y && z == blockPos.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
    }
}