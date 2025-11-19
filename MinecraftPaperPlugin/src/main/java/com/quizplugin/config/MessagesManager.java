package com.quizplugin.config;

import com.quizplugin.QuizPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * MessagesManager - Handles messages.yml for all plugin text
 * 
 * Purpose:
 * - Loads and manages all user-facing messages
 * - Supports color codes and MiniMessage formatting
 * - Allows placeholder replacement (e.g., %answer%, %link%)
 * - Ensures no hardcoded text in the plugin
 * 
 * Message types:
 * - Quiz start/end messages
 * - Correct/wrong answer feedback
 * - Opt-out/opt-in confirmations
 * - Admin command responses
 * - Error messages
 * 
 * Used by:
 * - ChatAnswerListener (answer feedback)
 * - QuizCommand (command responses)
 * - QuizSessionManager (quiz sending)
 */
public class MessagesManager {
    
    private final QuizPlugin plugin;
    private final File messagesFile;
    private FileConfiguration messages;
    private final MiniMessage miniMessage;
    
    public MessagesManager(QuizPlugin plugin) {
        this.plugin = plugin;
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        this.miniMessage = MiniMessage.miniMessage();
        load();
    }
    
    /**
     * Loads messages.yml from disk
     * Creates default file if it doesn't exist
     */
    public void load() {
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    /**
     * Reloads messages from disk
     */
    public void reload() {
        load();
    }
    
    /**
     * Gets a message from messages.yml
     * 
     * @param key The message key (e.g., "start_quiz")
     * @return The message text with color codes
     */
    public String getMessage(String key) {
        return messages.getString(key, "Message not found: " + key);
    }
    
    /**
     * Gets a message with placeholder replacement
     * 
     * @param key The message key
     * @param placeholders Map of placeholders to their values
     * @return Formatted message with placeholders replaced
     */
    public String getMessage(String key, String... placeholders) {
        String message = getMessage(key);
        
        // Replace placeholders in pairs (key, value, key, value, ...)
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            String placeholder = placeholders[i];
            String value = placeholders[i + 1];
            message = message.replace(placeholder, value);
        }
        
        return message;
    }
    
    /**
     * Gets a message as an Adventure Component
     * Supports MiniMessage formatting
     * 
     * @param key The message key
     * @return Adventure Component for modern text display
     */
    public Component getComponent(String key) {
        String message = getMessage(key);
        return miniMessage.deserialize(message);
    }
    
    /**
     * Gets a message as Component with placeholder replacement
     * 
     * @param key The message key
     * @param placeholders Placeholder pairs (key, value, key, value, ...)
     * @return Adventure Component with replaced placeholders
     */
    public Component getComponent(String key, String... placeholders) {
        String message = getMessage(key, placeholders);
        return miniMessage.deserialize(message);
    }
}
