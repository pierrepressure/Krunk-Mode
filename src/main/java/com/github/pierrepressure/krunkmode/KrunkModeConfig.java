package com.github.pierrepressure.krunkmode;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class KrunkModeConfig {
    private final Configuration config;

    // Farming settings
    private int autoPauseLoops;
    private boolean autoPlay;

    //Clicker settings
    private int maxCps;
    private int minCps;

    /**
     * Create a new configuration instance
     *
     * @param configFile The file to save/load configuration from
     */
    public KrunkModeConfig(File configFile) {
        this.config = new Configuration(configFile);
        loadConfig();
    }





    /**
     * Load configuration values from file or set defaults
     */
    private void loadConfig() {
        config.load();

        // Load farming settings
        autoPauseLoops = config.getInt(
                "autoPauseLoops",
                "farming",
                0,
                0,
                Integer.MAX_VALUE,
                "Number of melon farming loops before pausing (0 = infinite)"
        );

        autoPlay = config.getBoolean(
                "autoPlay",
                "farming",
                false,
                "Automatically resume after pause"
        );

        // Load clicker settings
        maxCps = config.getInt(
                "maxCps",
                "clicker",
                10,
                1,
                20,
                "Maximum clicks per second for the clicker"
        );

        minCps = config.getInt(
                "minCps",
                "clicker",
                6,
                1,
                20,
                "Minimum clicks per second for the clicker"
        );

        // Save if changes were made
        if (config.hasChanged()) {
            config.save();
        }
    }

    /**
     * Save configuration to disk
     */
    public void save() {
        if (config != null) {
            config.save();
        }
    }


    // Getters and Setters for CPS
    public int getMaxCps() {
        return maxCps;
    }

    public void setMaxCps(int max) {
        maxCps = max;
        config.get("clicker", "maxCps", 10).set(max);
        save();
    }

    public int getMinCps() {
        return minCps;
    }

    public void setMinCps(int min) {
        minCps = min;
        config.get("clicker", "minCps", 6).set(min);
        save();
    }

    /**
     * Get the number of melon farming loops before auto-pausing
     *
     * @return Number of loops (0 = infinite)
     */
    public int getAutoPauseLoops() {
        return autoPauseLoops;
    }

    /**
     * Set the number of melon farming loops before auto-pausing
     *
     * @param loops Number of loops (0 = infinite)
     */
    public void setAutoPauseLoops(int loops) {
        autoPauseLoops = loops;
        config.get("farming", "autoPauseLoops", 0).set(loops);
        save();
    }

    /**
     * Get whether auto-play is enabled
     *
     * @return True if auto-play is enabled
     */
    public boolean isAutoPlayEnabled() {
        return autoPlay;
    }

    /**
     * Set whether auto-play is enabled
     *
     * @param enabled True to enable auto-play
     */
    public void setAutoPlayEnabled(boolean enabled) {
        autoPlay = enabled;
        config.get("farming", "autoPlay", false).set(enabled);
        save();
    }

}