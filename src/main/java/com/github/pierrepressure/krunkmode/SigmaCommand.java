package com.github.pierrepressure.krunkmode;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;


public class SigmaCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "sigma";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/sigma";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayerSP) {
            EntityPlayerSP player = (EntityPlayerSP) sender;
            // Send message directly to chat
            player.sendChatMessage("i like men");
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
