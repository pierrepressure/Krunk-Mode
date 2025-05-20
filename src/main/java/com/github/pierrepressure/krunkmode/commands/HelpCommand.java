package com.github.pierrepressure.krunkmode.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HelpCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "khelp";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/khelp - Lists all KrunkMode commands and their usage.";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        // List all command instances here manually for now
        List<ICommand> commands = Arrays.asList(
                new CrashCommand(),
                new FarmCommand(),
                new FishCommand(),
                new MenuCommand(),
                new SigmaCommand()
                // Add future commands here
        );

        sender.addChatMessage(new ChatComponentText("§e§l- Krunk Mode Command List -" ));

        for (ICommand cmd : commands) {
            sender.addChatMessage(new ChatComponentText("§e" + cmd.getCommandUsage(sender)));
        }
    }



    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
