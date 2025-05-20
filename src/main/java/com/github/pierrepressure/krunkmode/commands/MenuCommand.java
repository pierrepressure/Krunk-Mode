package com.github.pierrepressure.krunkmode.commands;

import com.github.pierrepressure.krunkmode.KrunkMode;
import com.github.pierrepressure.krunkmode.KrunkMenu;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class MenuCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "km";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/km - displays the Krunk Menu";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        //sender.addChatMessage(new ChatComponentText("hello i am sigma"));
        KrunkMode.screenToOpenNextTick = new KrunkMenu();

    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
