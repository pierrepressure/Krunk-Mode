package com.github.pierrepressure.krunkmode.commands;

import com.github.pierrepressure.krunkmode.features.FisherManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class FishCommand extends CommandBase {


    @Override
    public String getCommandName() {
        return "fish";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "§b/fish §7- §aautomatically fishes based on ding sound §7§o(turn on volume)  ";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)  {
        FisherManager.INSTANCE.toggle();
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}