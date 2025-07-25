package com.github.pierrepressure.krunkmode.commands;


import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Arrays;
import java.util.List;

public class CrashCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "kcr";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "§b/kcr §7- §cinstantly crashes your game";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

            logInfo();
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

    private void logInfo() {
        org.apache.logging.log4j.LogManager.getLogger("CrashCommand").info("Crashing the game as requested.");
    }
}

