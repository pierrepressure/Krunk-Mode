package com.github.pierrepressure.krunkmode.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.Arrays;
import java.util.List;

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
                new MenuCommand(),
                new FarmCommand(),
                new FishCommand(),
                new ClickerCommand(),
                new MinerCommand(),
                new SigmaCommand(),
                new CrashCommand()
                // Add future commands here
        );

        sender.addChatMessage(new ChatComponentText("§e§l- Krunk Mode Command List -"));

        for (ICommand cmd : commands) {
            sender.addChatMessage(new ChatComponentText("§e" + cmd.getCommandUsage(sender)));
        }
    }


    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
