package com.github.pierrepressure.krunkmode.commands;

import com.github.pierrepressure.krunkmode.features.MinerManager;
import com.github.pierrepressure.krunkmode.WaypointParser;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

public class MinerCommand extends CommandBase {


    @Override
    public String getCommandName() {
        return "mine";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "§b/mine §7- §atoggles holding break\n§b/mine <load/unload> §7- §aloads/unloads waypoints from clipboard";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            // Default behavior - toggle mining
            MinerManager.INSTANCE.toggle();
        } else if (args.length == 1 && args[0].equalsIgnoreCase("load")) {
            // Load waypoints from clipboard
            loadWaypointsFromClipboard(sender);
        } else if (args.length == 1 && args[0].equalsIgnoreCase("unload")) {
            // Unload all waypoints
            unloadWaypoints(sender);
        } else {
            // Invalid arguments
            sender.addChatMessage(new ChatComponentText("§cInvalid arguments. Use §b/mine§7, §b/mine load§7, or §b/mine unload"));
        }
    }

    private void loadWaypointsFromClipboard(ICommandSender sender) {
        try {
            // Get clipboard content
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            String clipboardData = (String) clipboard.getData(DataFlavor.stringFlavor);

            if (clipboardData == null || clipboardData.trim().isEmpty()) {
                sender.addChatMessage(new ChatComponentText("§cClipboard is empty!"));
                return;
            }

            // Parse waypoints and add to MinerManager
            List<WaypointParser.BlockPos> waypoints = WaypointParser.replaceWaypoints(clipboardData);

            if (waypoints.isEmpty()) {
                sender.addChatMessage(new ChatComponentText("§cNo valid waypoints found in clipboard!"));

                return;
            }

            // Success message
            sender.addChatMessage(new ChatComponentText(String.format("§l§6[KM] Mining: Successfully loaded §a§l"
                    + waypoints.size() + " waypoints!")));

        } catch (UnsupportedFlavorException | IOException e) {
            sender.addChatMessage(new ChatComponentText("§cFailed to read clipboard: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            sender.addChatMessage(new ChatComponentText("§cFailed to parse waypoints: " + e.getMessage()));
        } catch (Exception e) {
            sender.addChatMessage(new ChatComponentText("§cUnexpected error: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private void unloadWaypoints(ICommandSender sender) {
        // Get current waypoint count
        int waypointCount = MinerManager.INSTANCE.getTargetCoordinates().size();

        if (waypointCount == 0) {
            sender.addChatMessage(new ChatComponentText("§l§6[KM] Mining: No waypoints are currently loaded!"));
            return;
        }

        // Clear all waypoints
        MinerManager.INSTANCE.clearTargetCoordinates();

        // Success message
        sender.addChatMessage(new ChatComponentText(String.format("§l§6[KM] Mining: Successfully unloaded §c§l"
                + waypointCount + " waypoints!")));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}