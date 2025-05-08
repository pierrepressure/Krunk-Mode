package com.github.pierrepressure.krunkmode;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class FishCommand extends CommandBase {


    @Override
    public String getCommandName() {
        return "fish";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        boolean now = AutoFishManager.INSTANCE.toggleEnabled();
        String status = now ? "§aEnabled" : "§cDisabled";

            sender.addChatMessage(new ChatComponentText(String.format("[KMFish] %s", status)));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}