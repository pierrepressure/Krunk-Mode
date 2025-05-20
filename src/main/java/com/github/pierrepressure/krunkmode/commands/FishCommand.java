package com.github.pierrepressure.krunkmode.commands;

import com.github.pierrepressure.krunkmode.features.FishManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

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
        FishManager.INSTANCE.toggle();
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}