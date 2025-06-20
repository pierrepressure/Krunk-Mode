package com.github.pierrepressure.krunkmode.commands.warp;

import net.minecraftforge.client.ClientCommandHandler;
import java.util.ArrayList;
import java.util.List;

public class WarpCommandHandler {
    private final List<WarpCommand> commands;

    /**
     * Constructor of the WarpCommandHandler, it will register all commands when it is created
     */
    public WarpCommandHandler() {
        this.commands = new ArrayList<>();
        registerCommands();
    }

    /**
     * @return List of all registered commands
     */
    public List<WarpCommand> getCommands() {
        return this.commands;
    }

    /**
     * Registers a command to the handler
     * @param warpCommand WarpCommand to register
     */
    public void registerCommand(WarpCommand warpCommand) {
        this.commands.add(warpCommand);
        ClientCommandHandler.instance.registerCommand(warpCommand);
    }

    /**
     * Get a command by its name
     * @param name Name of the command
     * @return WarpCommand with the given name
     */
    public WarpCommand getCommand(String name) {
        for (WarpCommand command : this.commands) {
            if (command.getCommandName().equalsIgnoreCase(name)) {
                return command;
            }
        }
        return null;
    }

    /**
     * Get a command by the class it extends from
     * @param clazz Class to get the command from
     * @return WarpCommand with the given class
     */
    public WarpCommand getCommand(Class<? extends WarpCommand> clazz) {
        for (WarpCommand command : this.commands) {
            if (command.getClass() == clazz) {
                return command;
            }
        }
        return null;
    }

    /**
     * Register all commands
     */
    private void registerCommands() {

        //Hub island
        registerCommand(new WarpCommand("castle"));
        registerCommand(new WarpCommand("da"));
        registerCommand(new WarpCommand("crypt"));
        registerCommand(new WarpCommand("museum"));
        registerCommand(new WarpCommand("wizard"));

        //Dungeon hub island
        registerCommand(new WarpCommand("dun", "dungeon_hub"));
        registerCommand(new WarpCommand("dh"));

        //Barn island
        registerCommand(new WarpCommand("barn"));
        registerCommand(new WarpCommand("desert"));
        registerCommand(new WarpCommand("trapper"));

        //Park island
        registerCommand(new WarpCommand("park"));
        registerCommand(new WarpCommand("jungle"));
        registerCommand(new WarpCommand("howl"));

        //Gold mines island
        registerCommand(new WarpCommand("gold"));

        //Deep caverns island
        registerCommand(new WarpCommand("deep"));

        //Dwarven mines island
        registerCommand(new WarpCommand("mines"));
        registerCommand(new WarpCommand("forge"));
        registerCommand(new WarpCommand("camp"));
        registerCommand(new WarpCommand("base"));

        //Crystal hollows island
        registerCommand(new WarpCommand("ch"));
        registerCommand(new WarpCommand("cn"));

        //Spiders den island
        registerCommand(new WarpCommand("spider"));
        registerCommand(new WarpCommand("top"));
        registerCommand(new WarpCommand("arachne"));

        //End island
        registerCommand(new WarpCommand("end"));
        registerCommand(new WarpCommand("drag"));
        registerCommand(new WarpCommand("void"));

        //Nether island
        registerCommand(new WarpCommand("nether"));
        registerCommand(new WarpCommand("kuudra"));
        registerCommand(new WarpCommand("wasteland"));
        registerCommand(new WarpCommand("dragontail"));
        registerCommand(new WarpCommand("scarleton"));
        registerCommand(new WarpCommand("smold"));

        //Garden island
        registerCommand(new WarpCommand("garden"));

        //Rift
        registerCommand(new WarpCommand("rift"));

        //Bayou island
        registerCommand(new WarpCommand("bayou"));

        //Temporary event islands
        registerCommand(new WarpCommand("carnival"));
        registerCommand(new WarpCommand("jerry"));
        registerCommand(new WarpCommand("stonks"));


    }
}