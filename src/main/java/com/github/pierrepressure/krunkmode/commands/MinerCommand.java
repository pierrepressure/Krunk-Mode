package com.github.pierrepressure.krunkmode.commands;

import com.github.pierrepressure.krunkmode.features.MinerManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class MinerCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "mine";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "§b/mine §7- §atoggles holding break";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        MinerManager.INSTANCE.toggle();
    }
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

}