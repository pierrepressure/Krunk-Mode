package com.github.pierrepressure.krunkmode.commands;

import com.github.pierrepressure.krunkmode.SimpleFishManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class FishCommand extends CommandBase {


    @Override
    public String getCommandName() {
        return "fish";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/fish - automatically fishes based on ding sound (turn on volume)";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)  {
        boolean now = SimpleFishManager.INSTANCE.toggle();
        String status = now ? "§aEnabled" : "§cDisabled";

            sender.addChatMessage(new ChatComponentText(String.format("§l§6[KMFish] %s", status)));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}