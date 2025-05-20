package com.github.pierrepressure.krunkmode;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class KrunkModeConfig {
    private final Configuration config;

    // Farming settings
    private int autoPauseLoops;
    private boolean autoPlay;

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