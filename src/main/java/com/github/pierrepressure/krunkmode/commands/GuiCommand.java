package com.github.pierrepressure.krunkmode.commands;

import com.github.pierrepressure.krunkmode.KrunkMode;
import com.github.pierrepressure.krunkmode.MyGuiScreen;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class GuiCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "gui";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/gui - displays a gui menu (useless)";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        //sender.addChatMessage(new ChatComponentText("hello i am sigma"));
        KrunkMode.screenToOpenNextTick = new MyGuiScreen();

    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
