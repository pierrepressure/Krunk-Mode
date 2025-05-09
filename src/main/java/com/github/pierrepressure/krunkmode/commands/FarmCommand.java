package com.github.pierrepressure.krunkmode.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.Arrays;
import java.util.List;

public class FarmCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "farm";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/farm - coming soon";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws WrongUsageException {
        if (args.length < 1) {
            throw new WrongUsageException("Specify a farm type: melon, wheat");
        }

        String type = args[0].toLowerCase();
        EntityPlayer player = (EntityPlayer) sender;

        switch (type) {
            case "melon":
                sender.addChatMessage(new ChatComponentText(String.format("Farming Melons..... (i didnt code this yet)")));
                //FarmMelon(player);
                break;
            case "wheat":

                sender.addChatMessage(new ChatComponentText(String.format("Farming Wheat..... (i didnt code this yet) ")));
                //FarmWheat(player);
                break;
            default:
                throw new WrongUsageException("Invalid farm type");
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "melon", "wheat");
        return Arrays.asList();
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
