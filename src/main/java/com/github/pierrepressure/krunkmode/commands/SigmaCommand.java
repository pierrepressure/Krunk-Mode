package com.github.pierrepressure.krunkmode.commands;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class SigmaCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "sigma";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/sigma - sends \"what the sigma\" in a random color";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayerSP) {
            EntityPlayerSP player = (EntityPlayerSP) sender;

            // List of all Minecraft color codes
            List<String> colors = Arrays.asList(
                    "§0", // Black
                    "§1", // Dark Blue
                    "§2", // Dark Green
                    "§3", // Dark Aqua
                    "§4", // Dark Red
                    "§5", // Dark Purple
                    "§6", // Gold
                    "§7", // Gray
                    "§8", // Dark Gray
                    "§9", // Blue
                    "§a", // Green
                    "§b", // Aqua
                    "§c", // Red
                    "§d", // Light Purple
                    "§e", // Yellow
                    "§f"  // White
            );

            // Randomly select a color
            String color = colors.get(new Random().nextInt(colors.size()));
            // Send message directly to chat
            sender.addChatMessage(new ChatComponentText(String.format("%s§l what the sigma ",color)));
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
