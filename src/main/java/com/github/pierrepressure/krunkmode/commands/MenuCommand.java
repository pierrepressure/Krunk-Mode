package com.github.pierrepressure.krunkmode.commands;

import com.github.pierrepressure.krunkmode.KrunkMode;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class MenuCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "km";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "§b/km §7- §6displays the Krunk Menu  ";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        KrunkMode.screenToOpenNextTick = KrunkMode.config.gui();

    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
