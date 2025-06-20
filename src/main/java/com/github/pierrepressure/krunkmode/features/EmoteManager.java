package com.github.pierrepressure.krunkmode.features;

import com.github.pierrepressure.krunkmode.VigilanceConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmoteManager {
    public static final EmoteManager INSTANCE = new EmoteManager();
    private static VigilanceConfig config;

    private static final Map<String, String> REPLACEMENTS = new HashMap<String, String>() {{
        put("<3", "❤");
        put("o/", "( ﾟ◡ﾟ)/");
        put(":star:", "✮");
        put(":yes:", "✔");
        put(":no:", "✖");
        put(":java:", "☕");
        put(":arrow:", "➜");
        put(":shrug:", "¯\\_(ツ)_/¯");
        put(":tableflip:", "(╯°□°）╯︵ ┻━┻");
        put(":totem:", "☉_☉");
        put(":typing:", "✎...");
        put(":maths:", "√(π+x)=L");
        put(":snail:", "@'-'");
        put("ez", "ｅｚ");
        put(":thinking:", "(0.o?)");
        put(":gimme:", "༼つ◕_◕༽つ");
        put(":wizard:", "('-')⊃━☆ﾟ.*･｡ﾟ");
        put(":pvp:", "⚔");
        put(":peace:", "✌");
        put(":puffer:", "<('O')>");
        put("h/", "ヽ(^◇^*)/");
        put(":sloth:", "(・⊝・)");
        put(":dog:", "(ᵔᴥᵔ)");
        put(":dj:", "ヽ(⌐■_■)ノ♬");
        put(":yey:", "ヽ (◕◡◕) ﾉ");
        put(":snow:", "☃");
        put(":dab:", "<o/");
        put(":cat:", "= ＾● ⋏ ●＾ =");
        put(":cute:", "(✿◠‿◠)");
        put(":skull:", "☠");
        put(":crip:", "♿");
        put(":sus:", "ඞ");
    }};

    private static final List<String> ALLOWED_COMMANDS = Arrays.asList("/pc", "/ac", "/gc", "/msg", "/w", "/r");

    private static boolean chatEmotesEnabled = true;
    private String lastMessage = "";
    private boolean isProcessing = false;
    private long lastProcessTime = 0;
    private boolean enterWasPressed = false;

    public void setChatEmotesEnabled(boolean enabled) {
        chatEmotesEnabled = enabled;
    }

    public boolean isChatEmotesEnabled() {
        return chatEmotesEnabled;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!chatEmotesEnabled || event.phase != TickEvent.Phase.START) return;

        if (Minecraft.getMinecraft().currentScreen instanceof GuiChat) {
            // Check if Enter is currently pressed
            boolean enterPressed = Keyboard.isKeyDown(Keyboard.KEY_RETURN);

            // If Enter was just pressed, don't process to avoid interference
            if (enterPressed && !enterWasPressed) {
                enterWasPressed = true;
                return;
            } else if (!enterPressed) {
                enterWasPressed = false;
            }

            // Don't process if Enter is held down
            if (!enterPressed) {
                processCurrentChatMessage();
            }
        } else {
            // Reset state when not in chat
            resetState();
        }
    }

    private void resetState() {
        lastMessage = "";
        isProcessing = false;
        enterWasPressed = false;
        updateSettings();
    }

    private void processCurrentChatMessage() {
        // Prevent recursive processing and rate limiting
        if (isProcessing || System.currentTimeMillis() - lastProcessTime < 50) {
            return;
        }

        try {
            isProcessing = true;
            GuiChat chatGui = (GuiChat) Minecraft.getMinecraft().currentScreen;

            // Get the input field using reflection
            Field inputField = GuiChat.class.getDeclaredField("inputField");
            inputField.setAccessible(true);
            GuiTextField textField = (GuiTextField) inputField.get(chatGui);

            String currentMessage = textField.getText();

            if (currentMessage == null || currentMessage.equals(lastMessage)) {
                return;
            }

            // Skip if it's a command that's not in our allowed list
            if (currentMessage.startsWith("/")) {
                boolean isAllowedCommand = false;
                for (String allowedCmd : ALLOWED_COMMANDS) {
                    if (currentMessage.startsWith(allowedCmd + " ") || currentMessage.equals(allowedCmd)) {
                        isAllowedCommand = true;
                        break;
                    }
                }
                if (!isAllowedCommand) {
                    lastMessage = currentMessage;
                    return;
                }
            }

            // Only process if user added content (not removing)
            if (currentMessage.length() >= lastMessage.length()) {
                String replacedMessage = replaceEmotes(currentMessage);

                if (!replacedMessage.equals(currentMessage)) {
                    // Store cursor position before modification
                    int cursorPos = textField.getCursorPosition();
                    int lengthDiff = replacedMessage.length() - currentMessage.length();

                    // Update text and cursor
                    textField.setText(replacedMessage);
                    textField.setCursorPosition(Math.min(cursorPos + lengthDiff, replacedMessage.length()));

                    lastMessage = replacedMessage;
                    lastProcessTime = System.currentTimeMillis();
                } else {
                    lastMessage = currentMessage;
                }
            } else {
                lastMessage = currentMessage;
            }

        } catch (Exception e) {
            System.err.println("Error processing chat message: " + e.getMessage());
            // Don't print stack trace in production to avoid spam
        } finally {
            isProcessing = false;
        }
    }

    private String replaceEmotes(String message) {
        // Split by spaces but preserve the structure
        String[] parts = message.split(" ");
        boolean hasReplacement = false;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (REPLACEMENTS.containsKey(part)) {
                parts[i] = REPLACEMENTS.get(part);
                hasReplacement = true;
            }
        }

        return hasReplacement ? String.join(" ", parts) : message;
    }

    public void init(VigilanceConfig config) {
        EmoteManager.config = config;
        chatEmotesEnabled = config.chatEmotes;
    }

    // If you have methods that update config values:
    public static void updateSettings() {

        if (config.chatEmotes != chatEmotesEnabled) {
            chatEmotesEnabled = config.chatEmotes;
        }


    }
}