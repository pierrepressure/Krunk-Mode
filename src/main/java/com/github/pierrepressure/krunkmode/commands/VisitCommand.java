package com.github.pierrepressure.krunkmode.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class VisitCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "v";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/v <player>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            // Send usage message if no player name is provided
            sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.RED + "Usage: /v <player>"
            ));
            return;
        }

        // Get the player name from the first argument
        String playerName = args[0];

        // Send the /visit command with the player name
        Minecraft.getMinecraft().thePlayer.sendChatMessage("/visit " + playerName);
    }
}