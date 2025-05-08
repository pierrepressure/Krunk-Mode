package com.github.pierrepressure.krunkmode;


import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Arrays;
import java.util.List;

public class TestCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "cr";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

            logInfo("Crashing the game as requested.");
            FMLCommonHandler.instance().exitJava(1, false);

    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true; // Always allow for client commands
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("dontcrash");
    }

    private void logInfo(String msg) {
        org.apache.logging.log4j.LogManager.getLogger("CrashCommand").info(msg);
    }
}

