package com.github.pierrepressure.krunkmode.features;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public enum MinerManager {
    INSTANCE;

    private final Minecraft mc = Minecraft.getMinecraft();
    private final KeyBinding attackKey = mc.gameSettings.keyBindAttack;
    private final KeyBinding useKey = mc.gameSettings.keyBindUseItem;
    private final Random random = new Random();

    public static final KeyBinding minerTeleportKey = new KeyBinding("Miner Teleport", Keyboard.KEY_NONE, "KrunkMode");

    public boolean isEnabled = false;

    // Coordinate-based detection and automation
    private Set<BlockPos> targetCoordinates = new HashSet<>();
    private boolean isExecutingSequence = false;
    private int originalSlot = -1;
    private long lastSequenceTime = 0;
    private static final long SEQUENCE_COOLDOWN = 500; // 3 seconds in milliseconds

    public void toggle() {
        isEnabled = !isEnabled;

        if (mc.thePlayer != null) {
            String status = isEnabled ? "§a§lENABLED" : "§c§lDISABLED";
            mc.thePlayer.addChatMessage(new ChatComponentText("§l§6[KM] Miner " + status));
        }

        // Always release key when disabling and reset sequence
        if (!isEnabled) {
            KeyBinding.setKeyBindState(attackKey.getKeyCode(), false);
            resetSequence();
        }
    }

    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        if (!isEnabled || mc.thePlayer == null || mc.theWorld == null) return;

        // Release attack key when GUI is open
        if (mc.currentScreen != null && attackKey.isPressed()) {
            KeyBinding.setKeyBindState(attackKey.getKeyCode(), false);
        }

        // Skip normal behavior if sequence is running
        if (isExecutingSequence) return;


        // Check if enough time has passed since last sequence
        long currentTime = System.currentTimeMillis();

        // Early return if still in cooldown
        if (currentTime - lastSequenceTime < SEQUENCE_COOLDOWN) {
            return;
        }

        // Auto mode - no key set
        if (minerTeleportKey.getKeyCode() == Keyboard.KEY_NONE) {
            if (isLookingAtTargetCoordinate()) {
                startSequence();
            }
        }

        // Manual mode - key is set and pressed
        if (minerTeleportKey.isKeyDown()) {
            if (targetCoordinates.isEmpty() || isLookingAtTargetCoordinate()) {
                startSequence();
            }
        }

        // Normal mining behavior - hold break
        if (!isLookingAtTargetCoordinate() || minerTeleportKey.getKeyCode() == Keyboard.KEY_NONE) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
        }else{
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
        }
    }

    private boolean isLookingAtTargetCoordinate() {
        if (targetCoordinates.isEmpty()) return false;

        MovingObjectPosition rayTrace = mc.thePlayer.rayTrace(200.0D, 1.0F); // Extended range for detection

        if (rayTrace != null && rayTrace.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            BlockPos pos = rayTrace.getBlockPos();
            return targetCoordinates.contains(pos);
        }

        return false;
    }

    private void startSequence() {
        if (isExecutingSequence) return;

        isExecutingSequence = true;
        originalSlot = mc.thePlayer.inventory.currentItem;
        lastSequenceTime = System.currentTimeMillis(); // Update the last sequence time

        // Stop mining during sequence
        KeyBinding.setKeyBindState(attackKey.getKeyCode(), false);

        // Execute sequence with human-like delays in separate thread
        CompletableFuture.runAsync(() -> {
            try {
                // Step 1: Swap to 1st hotbar slot (slot 0)
                mc.addScheduledTask(() -> {
                    if (mc.thePlayer != null) {
                        mc.thePlayer.inventory.currentItem = 0;
                    }
                });
                Thread.sleep(getRandomDelay());

                // Step 2: Right click with the item
                mc.addScheduledTask(() -> {
                    KeyBinding.setKeyBindState(useKey.getKeyCode(), true);
                });
                Thread.sleep(50 + random.nextInt(50)); // Short delay for right click duration

                // Release right click
                mc.addScheduledTask(() -> {
                    KeyBinding.setKeyBindState(useKey.getKeyCode(), false);
                });
                Thread.sleep(getRandomDelay());

                // Step 3: Swap back to original slot
                mc.addScheduledTask(() -> {
                    if (mc.thePlayer != null) {
                        mc.thePlayer.inventory.currentItem = originalSlot;
                    }
                });
                Thread.sleep(getRandomDelay());

                // Step 4: Resume mining
                mc.addScheduledTask(() -> {
                    if (isEnabled) { // Only resume if still enabled
                        KeyBinding.setKeyBindState(attackKey.getKeyCode(), true);
                    }
                    resetSequence();
                });

            } catch (InterruptedException e) {
                // Handle interruption
                mc.addScheduledTask(() -> {
                    resetSequence();
                });
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // Handle any other exceptions
                mc.addScheduledTask(() -> {
                    resetSequence();
                });
            }
        });
    }

    private int getRandomDelay() {
        return 50 + random.nextInt(51); // Random delay between 50-100ms
    }

    private void resetSequence() {
        isExecutingSequence = false;
        originalSlot = -1;

        // Ensure keys are in correct state
        KeyBinding.setKeyBindState(useKey.getKeyCode(), false);
    }

    // Methods to manage target coordinates
    public void addTargetCoordinate(BlockPos pos) {
        targetCoordinates.add(pos);
    }

    public void addTargetCoordinate(int x, int y, int z) {
        targetCoordinates.add(new BlockPos(x, y, z));
    }

    public void removeTargetCoordinate(BlockPos pos) {
        targetCoordinates.remove(pos);
    }

    public void removeTargetCoordinate(int x, int y, int z) {
        targetCoordinates.remove(new BlockPos(x, y, z));
    }

    public void clearTargetCoordinates() {
        targetCoordinates.clear();
    }

    public Set<BlockPos> getTargetCoordinates() {
        return new HashSet<>(targetCoordinates);
    }

    public boolean isTargetCoordinate(BlockPos pos) {
        return targetCoordinates.contains(pos);
    }

    public boolean isTargetCoordinate(int x, int y, int z) {
        return targetCoordinates.contains(new BlockPos(x, y, z));
    }

    public boolean isExecutingSequence() {
        return isExecutingSequence;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}