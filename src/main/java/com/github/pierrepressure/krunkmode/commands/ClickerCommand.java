package com.github.pierrepressure.krunkmode.commands;

import com.github.pierrepressure.krunkmode.features.ClickerManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class ClickerCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "clicker";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "§b/clicker §7- §atoggles the clicker";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        ClickerManager.INSTANCE.toggle();
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
