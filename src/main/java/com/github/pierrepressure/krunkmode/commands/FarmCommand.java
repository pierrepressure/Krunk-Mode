package com.github.pierrepressure.krunkmode.commands;

import com.github.pierrepressure.krunkmode.features.farming.*;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FarmCommand extends CommandBase {
    private static FarmCrop currentCrop = null;
    private static final Map<String, FarmCrop> CROP_REGISTRY = new HashMap<>();

    static {
        // Register all available crops
        CROP_REGISTRY.put("melon", FarmMelon.INSTANCE);
        CROP_REGISTRY.put("wart", FarmWart.INSTANCE);
        CROP_REGISTRY.put("potato", FarmPotato.INSTANCE);
        CROP_REGISTRY.put("carrot", FarmCarrot.INSTANCE);
        CROP_REGISTRY.put("wheat", FarmWheat.INSTANCE);
        CROP_REGISTRY.put("pumpkin", FarmPumpkin.INSTANCE);
        CROP_REGISTRY.put("cane", FarmCane.INSTANCE);
        CROP_REGISTRY.put("coco", FarmCocoa.INSTANCE);
        // Add new crops here when implemented:
        // CROP_REGISTRY.put("wheat", FarmWheat.INSTANCE);
    }

    @Override
    public String getCommandName() {
        return "farm";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "§b/farm <crop> §7- §atoggle farming a specific crop\n"
                + "§b/farm <pause/play> §7- §apause or resume a farming cycle";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws WrongUsageException {
        if (args.length < 1) {
            throw new WrongUsageException("Specify a farm type: melon, wheat");
        }

        String type = args[0].toLowerCase();
        EntityPlayer player = (EntityPlayer) sender;

        if (type.equals("play")) {
            handlePlayCommand(player);
            return;
        }

        if (type.equals("pause")) {
            handlePauseCommand(player);
            return;
        }


        FarmCrop selectedCrop = CROP_REGISTRY.get(type);
        if (selectedCrop == null) {
            throw new WrongUsageException("Invalid farm type. Available: " + String.join(", ", CROP_REGISTRY.keySet()));
        }

        handleCropSwitch(selectedCrop, player);
    }

    private void handleCropSwitch(FarmCrop newCrop, EntityPlayer player) {

        // Stop previous crop if different and running
        if (currentCrop != null && currentCrop != newCrop && FarmCrop.isRunning()) {
            FarmCrop.toggle(player, currentCrop);
        }

        // Toggle new crop
        currentCrop = newCrop;
        newCrop.toggle(player);
    }

    private void handlePlayCommand(EntityPlayer player) {
        if (currentCrop == null) {
            player.addChatMessage(new ChatComponentText("§cNo active crop to resume!"));
            return;
        }

        if (!FarmCrop.isPaused()) {
            player.addChatMessage(new ChatComponentText("§cFarm is not paused!"));
            return;
        }

        FarmCrop.play();
    }

    private void handlePauseCommand(EntityPlayer player) {
        if (currentCrop == null) {
            player.addChatMessage(new ChatComponentText("§cNo active crop to pause!"));
            return;
        }

        if (FarmCrop.isAutoPaused()) {
            player.addChatMessage(new ChatComponentText("§cFarm is already paused!"));
            return;
        }

        FarmCrop.setAutoPaused(true);
        FarmCrop.INSTANCE.pause();
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, CROP_REGISTRY.keySet().toArray(new String[0]));
        }
        return Arrays.asList();
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    public static FarmCrop getCurrentCrop() {
        return currentCrop;
    }
}