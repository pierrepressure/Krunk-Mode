package com.github.pierrepressure.krunkmode.commands;

import com.github.pierrepressure.krunkmode.features.FarmCrop;
import com.github.pierrepressure.krunkmode.features.FarmMelon;
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
        // Add new crops here when implemented:
        // CROP_REGISTRY.put("wheat", FarmWheat.INSTANCE);
    }

    @Override
    public String getCommandName() {
        return "farm";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/farm <crop> to toggle farming a crop\n" + "/farm play to resume paused farming";
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

        FarmCrop selectedCrop = CROP_REGISTRY.get(type);
        if (selectedCrop == null) {
            throw new WrongUsageException("Invalid farm type. Available: " + String.join(", ", CROP_REGISTRY.keySet()));
        }

        handleCropSwitch(selectedCrop, player);
    }

    private void handleCropSwitch(FarmCrop newCrop, EntityPlayer player) {
        // Stop previous crop if different and running
        if (currentCrop != null && currentCrop != newCrop && currentCrop.isRunning()) {
            currentCrop.toggle(player);
        }

        // Toggle new crop
        currentCrop = newCrop;
        currentCrop.toggle(player);
    }

    private void handlePlayCommand(EntityPlayer player) {
        if (currentCrop == null) {
            player.addChatMessage(new ChatComponentText("§cNo active crop to resume!"));
            return;
        }

        if (!currentCrop.isRunning()) {
            player.addChatMessage(new ChatComponentText("§cFarm is not paused!"));
            return;
        }

        currentCrop.play();
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