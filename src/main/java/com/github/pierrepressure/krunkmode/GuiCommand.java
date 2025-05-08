package com.github.pierrepressure.krunkmode;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Arrays;
import java.util.List;

public class GuiCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "gui";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "";
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
