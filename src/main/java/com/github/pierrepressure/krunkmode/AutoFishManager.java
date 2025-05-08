package com.github.pierrepressure.krunkmode;

// Import required Minecraft and Forge classes
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

public enum AutoFishManager {
    INSTANCE;  // Singleton instance

    // === Constants used for tuning auto-fishing behavior ===
    private static final int TICKS_PER_SECOND = 20;               // Minecraft ticks per second
    private static final int REEL_DELAY_MIN = 1;                  // Minimum reel delay (0.15s)
    private static final int REEL_DELAY_MAX = 4;                  // Maximum reel delay (0.30s)
    private static final int CAST_DELAY_BASE = 10;                // Base cast delay (0.5s)
    private static final int CAST_DELAY_VARIANCE = 10;            // Cast delay randomness (±0.5s)
    private static final double BOBBER_DROP_THRESHOLD = -0.01;    // Y-velocity for bite detection
    private static final double SPLASH_DETECTION_RADIUS = 4.0;    // Max splash detection distance
    private static final int FRESH_CAST_GRACE_PERIOD = 10;        // 10 tick protection after casting
    private static final int HOOK_TIMEOUT = 30 * TICKS_PER_SECOND;// 30s hook expiration timeout
    private static final int DETECTION_TIMEOUT = 7 * TICKS_PER_SECOND; // 5s detection validity
    private static final int WAKE_DELAY = 30;                     // 1.5s delay after water wake

    // === Core state and utility ===
    private final Minecraft mc = Minecraft.getMinecraft();           // Minecraft client instance
    private final Random random = new Random();                      // Random number generator

    // === Internal state tracking ===
    private boolean enabled = false;              // Module activation state
    private long nextCastTime = 0L;               // Scheduled cast timestamp
    private long nextReelTime = 0L;               // Scheduled reel timestamp
    private long lastCastTime = 0L;               // Last successful cast time
    private long lastSplashTime = 0L;             // Last valid splash detection
    private long lastWakeTime = 0L;               // Last water wake detection
    private long lastXpTime = 0L;                 // Last XP orb collection time

    // Toggle the enabled state and schedule first cast if turning on
    public boolean toggleEnabled() {
        enabled = !enabled;
        if (enabled && mc.theWorld != null) {
            scheduleNextCast(mc.theWorld.getTotalWorldTime());
        }
        return enabled;
    }

    // Main tick logic called each frame on the client
    public void onClientTick(TickEvent.ClientTickEvent event) {
        // Only run on the START phase of each tick
        if (event.phase != TickEvent.Phase.START) return;

        EntityPlayerSP player = mc.thePlayer;
        if (player == null || mc.theWorld == null || !enabled) return;

        // Get the current world time
        long currentTime = mc.theWorld.getTotalWorldTime();

        // Check if a GUI is open that would block inputs
        boolean guiBlocking = isBlockingGuiVisible();

        // Execute fishing logic
        handleFishingCycle(player, currentTime, guiBlocking);
    }

    // Record time when water wake is detected (used to detect fish activity)
    public void onWaterWake(WorldTickEvent event) {
        EntityPlayerSP player = mc.thePlayer;
        if (player != null && isHookValid(player)) {
            lastWakeTime = mc.theWorld.getTotalWorldTime();
        }
    }

    // Handle detection of splash sounds and note block sounds
    public void onSplashSound(PlaySoundEvent event) {
        if (event.sound == null) return;

        handleWaterSplashDetection(event);
        handleNoteBlockDetection(event);
    }

    // Record XP orb pickup near player (usually after reeling in fish)
    public void onXpOrbCollected(EntityXPOrb orb) {
        EntityPlayerSP player = mc.thePlayer;
        if (player != null && orb.getDistanceSqToEntity(player) < 4.0D) {
            lastXpTime = mc.theWorld.getTotalWorldTime();
        }
    }

    // Core logic run each tick when enabled
    private void handleFishingCycle(EntityPlayerSP player, long currentTime, boolean guiBlocking) {
        // If hook has been out too long, reel in and reset
        checkHookTimeout(player, currentTime);

        // Check for bite indicators
        if (isHoldingRod(player)) {
            detectBiteAttempt(player, currentTime);
        }

        // Perform reel or cast if their timers have elapsed
        processScheduledActions(player, currentTime, guiBlocking);
    }

    // Reel in the line if hook has timed out (too long without a bite)
    private void checkHookTimeout(EntityPlayerSP player, long currentTime) {
        if (player.fishEntity != null && lastCastTime > 0
                && (currentTime - lastCastTime) > HOOK_TIMEOUT) {
            performReel(player);
            scheduleNextCast(currentTime);
            lastCastTime = 0;
        }
    }

    // Check for signs of a fish bite and schedule reel
    private void detectBiteAttempt(EntityPlayerSP player, long currentTime) {
        if (!isHookValid(player) || nextReelTime != 0L) return;

        if (isBiteDetected(player, currentTime)) {
            scheduleReelAttempt(currentTime);
        }
    }

    // Handles scheduled reel or cast if the time has arrived
    private void processScheduledActions(EntityPlayerSP player, long currentTime, boolean guiBlocking) {
        // Reel if it's time and no GUI is blocking
        if (shouldPerformReel(currentTime, guiBlocking)) {
            performReel(player);
            nextReelTime = 0L;
            scheduleNextCast(currentTime);
        }

        // Cast if it's time and no GUI is blocking
        if (shouldPerformCast(currentTime, guiBlocking)) {
            performCast(player);
            resetDetectionStates();
        }
    }

    // Determine if a bite has occurred using multiple heuristics
    private boolean isBiteDetected(EntityPlayerSP player, long currentTime) {
        EntityFishHook hook = player.fishEntity;
        if (player.fishEntity == null || currentTime - lastCastTime < 15) return false;
        if (hook == null || hook.isDead || hook.caughtEntity != null) return false;

        if ((currentTime - lastCastTime) <= FRESH_CAST_GRACE_PERIOD) return false;

        return checkBobberMotion(hook) ||
                checkRecentSplash(currentTime) ||
                checkDelayedWake(currentTime) ||
                checkRecentXp(currentTime);
    }

    // Check if bobber suddenly dropped (typical sign of bite)
    private boolean checkBobberMotion(EntityFishHook hook) {
        return hook.motionX == 0 && hook.motionZ == 0 && hook.motionY < BOBBER_DROP_THRESHOLD;
    }

    // Check if a splash sound occurred near the hook recently
    private boolean checkRecentSplash(long currentTime) {
        return lastSplashTime > 0 && (currentTime - lastSplashTime) < DETECTION_TIMEOUT;
    }

    // Check if water was disturbed and enough time passed to consider it a bite
    private boolean checkDelayedWake(long currentTime) {
        return lastWakeTime > 0 && currentTime > (lastWakeTime + WAKE_DELAY)
                && (currentTime - lastWakeTime) < DETECTION_TIMEOUT;
    }

    // Check if XP orb was picked up recently (indicates fish caught)
    private boolean checkRecentXp(long currentTime) {
        return lastXpTime > 0 && (currentTime - lastXpTime) < TICKS_PER_SECOND;
    }

    // Trigger a cast action by swinging the rod
    private void performCast(EntityPlayerSP player) {
        player.swingItem();
        mc.playerController.sendUseItem(player, mc.theWorld, player.getHeldItem());
        lastCastTime = mc.theWorld.getTotalWorldTime();
    }

    // Trigger a reel action by swinging the rod
    private void performReel(EntityPlayerSP player) {
        player.swingItem();
        mc.playerController.sendUseItem(player, mc.theWorld, player.getHeldItem());
        if (player.fishEntity != null) {
            player.fishEntity.setDead(); // Properly destroy hook
            player.fishEntity = null;
        }
    }

    // Schedule a future cast with some delay and randomness
    private void scheduleNextCast(long currentTime) {
        int variance = random.nextInt(CAST_DELAY_VARIANCE + 1);
        nextCastTime = currentTime + CAST_DELAY_BASE + variance;
    }

    // Schedule a future reel attempt after a bite is detected
    private void scheduleReelAttempt(long currentTime) {
        int delay = REEL_DELAY_MIN + random.nextInt(REEL_DELAY_MAX - REEL_DELAY_MIN + 1);
        nextReelTime = currentTime + delay;
    }

    // Reset detection flags and timers
    private void resetDetectionStates() {
        nextCastTime = 0L;
        lastWakeTime = 0L;
        lastSplashTime = 0L;
        lastXpTime = 0L;
    }

    // Check if the hook is in a valid state and over water
    private boolean isHookValid(EntityPlayerSP player) {
        EntityFishHook hook = player.fishEntity;
        if (hook == null) return false;

        BlockPos pos = new BlockPos(hook.posX, hook.posY, hook.posZ);
        IBlockState state = mc.theWorld.getBlockState(pos);

        return state.getBlock() == Blocks.water ||
                mc.theWorld.getBlockState(pos.down()).getBlock() == Blocks.water;
    }

    // Check if the player is holding a fishing rod
    private boolean isHoldingRod(EntityPlayerSP player) {
        return player.getHeldItem() != null &&
                player.getHeldItem().getItem() instanceof ItemFishingRod;
    }

    // Detect if a GUI is open that would block mouse input
    private boolean isBlockingGuiVisible() {
        return mc.currentScreen != null &&
                !(mc.currentScreen instanceof GuiChat) &&
                !isNonBlockingOverlay(mc.currentScreen);
    }

    // Identify GUIs that are visible but not input-blocking (e.g., overlays)
    private boolean isNonBlockingOverlay(GuiScreen gui) {
        return gui instanceof net.minecraft.client.gui.GuiIngameMenu ||
                gui instanceof net.minecraft.client.gui.GuiGameOver ||
                gui.getClass().getName().contains("Boss");
    }

    // Handle detection of splash sounds near the hook
    private void handleWaterSplashDetection(PlaySoundEvent event) {
        if (!"random.splash".equals(event.name)) return;

        EntityPlayerSP player = mc.thePlayer;
        EntityFishHook hook = player != null ? player.fishEntity : null;
        if (hook == null) return;

        double dx = hook.posX - event.sound.getXPosF();
        double dy = hook.posY - event.sound.getYPosF();
        double dz = hook.posZ - event.sound.getZPosF();

        // Check if the sound was close enough to the hook
        if ((dx * dx + dy * dy + dz * dz) <= SPLASH_DETECTION_RADIUS) {
            lastSplashTime = mc.theWorld.getTotalWorldTime();
        }
    }

    // Handle detection of note block sounds (sometimes used in fish farms)
    private void handleNoteBlockDetection(PlaySoundEvent event) {
        String[] NOTE_SOUNDS = {
                "block.note.pling", "block.note.harp", "block.note.bell",
                "block.note.chime", "block.note.bit", "block.note.xylophone",
                "block.note.cow_bell", "block.note.iron_xylophone", "block.note.banjo"
        };

        for (String sound : NOTE_SOUNDS) {
            if (sound.equals(event.name)) {
                lastSplashTime = mc.theWorld.getTotalWorldTime();
                break;
            }
        }
    }

    // Determine if reel should be triggered now
    private boolean shouldPerformReel(long currentTime, boolean guiBlocking) {
        return nextReelTime > 0L && currentTime >= nextReelTime && !guiBlocking;
    }

    // Determine if cast should be triggered now
    private boolean shouldPerformCast(long currentTime, boolean guiBlocking) {
        return nextCastTime > 0L && currentTime >= nextCastTime && !guiBlocking;
    }
}
