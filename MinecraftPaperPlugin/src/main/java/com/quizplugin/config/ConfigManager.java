package com.quizplugin.config;

import com.quizplugin.QuizPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ConfigManager - Handles config.yml loading and access
 * 
 * Purpose:
 * - Loads and manages the main configuration file (config.yml)
 * - Provides easy access to quiz intervals, enabled types, group settings, etc.
 * - Handles default configuration creation if file doesn't exist
 * 
 * Config structure managed:
 * - quiz_interval_seconds: How often quizzes are sent
 * - enabled_quiz_types: List of active quiz types
 * - group_settings: Maps LuckPerms groups to allowed quiz types
 * - time_limits: Default time limits per difficulty
 * - attempts: Default max attempts per difficulty
 * 
 * Used by:
 * - QuizPlugin (main initialization)
 * - QuizSessionManager (for quiz distribution)
 * - GroupManager (for determining allowed quiz types)
 */
public class ConfigManager {
    
    private final QuizPlugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(QuizPlugin plugin) {
        this.plugin = plugin;
        load();
    }
    
    /**
     * Loads config.yml from disk
     * Creates default config if it doesn't exist
     */
    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    /**
     * Reloads config from disk
     */
    public void reload() {
        load();
    }
    
    /**
     * Gets quiz interval in seconds
     * Determines how often the scheduler sends quizzes
     * 
     * @return interval in seconds (default: 300)
     */
    public int getQuizInterval() {
        return config.getInt("quiz_interval_seconds", 300);
    }
    
    /**
     * Gets debug mode setting
     * When enabled, shows detailed logs about quiz distribution
     * 
     * @return true if debug mode is enabled
     */
    public boolean isDebugMode() {
        return config.getBoolean("debug_mode", false);
    }
    
    /**
     * Gets list of enabled quiz types
     * 
     * @return List of quiz type names (e.g., ["type1", "type2"])
     */
    public List<String> getEnabledQuizTypes() {
        return config.getStringList("enabled_quiz_types");
    }
    
    /**
     * Gets allowed quiz types for a specific group
     * 
     * @param groupName The LuckPerms group name (e.g., "A1-A2")
     * @return List of allowed quiz types for this group
     */
    public List<String> getAllowedTypesForGroup(String groupName) {
        ConfigurationSection groupSettings = config.getConfigurationSection("group_settings");
        if (groupSettings != null && groupSettings.contains(groupName)) {
            return groupSettings.getStringList(groupName + ".allowed_types");
        }
        // Return all enabled types for unconfigured groups (default fallback)
        return getEnabledQuizTypes();
    }
    
    /**
     * Gets all group settings as a map
     * 
     * @return Map of group names to their allowed quiz types
     */
    public Map<String, List<String>> getAllGroupSettings() {
        Map<String, List<String>> groupMap = new HashMap<>();
        ConfigurationSection groupSettings = config.getConfigurationSection("group_settings");
        
        if (groupSettings != null) {
            for (String groupName : groupSettings.getKeys(false)) {
                List<String> allowedTypes = groupSettings.getStringList(groupName + ".allowed_types");
                groupMap.put(groupName, allowedTypes);
            }
        }
        
        return groupMap;
    }
    
    /**
     * Gets default time limit for a difficulty level
     * 
     * @param difficulty "easy", "medium", or "hard"
     * @return time limit in seconds
     */
    public int getTimeLimit(String difficulty) {
        return config.getInt("time_limits." + difficulty, 30);
    }
    
    /**
     * Gets default max attempts for a difficulty level
     * 
     * @param difficulty "easy", "medium", or "hard"
     * @return maximum attempts allowed
     */
    public int getMaxAttempts(String difficulty) {
        return config.getInt("attempts." + difficulty, 2);
    }
    
    /**
     * Gets the raw configuration object
     * For advanced access when needed
     */
    public FileConfiguration getConfig() {
        return config;
    }
}
