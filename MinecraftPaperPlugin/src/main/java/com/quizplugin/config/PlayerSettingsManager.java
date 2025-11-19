package com.quizplugin.config;

import com.quizplugin.QuizPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

/**
 * PlayerSettingsManager - Handles player_settings.yml for opt-out preferences
 * 
 * Purpose:
 * - Stores which players have opted out of receiving quizzes
 * - Provides fast lookup to check if a player should receive quizzes
 * - Persists opt-out status across server restarts
 * 
 * Data structure:
 * - Stores player UUID with opt-out status (true/false)
 * - Default is false (player receives quizzes)
 * 
 * Used by:
 * - QuizSessionManager (checks before sending quizzes)
 * - QuizCommand (for /quiz optout and /quiz optin)
 */
public class PlayerSettingsManager {
    
    private final QuizPlugin plugin;
    private final File settingsFile;
    private FileConfiguration settings;
    
    public PlayerSettingsManager(QuizPlugin plugin) {
        this.plugin = plugin;
        this.settingsFile = new File(plugin.getDataFolder(), "player_settings.yml");
        load();
    }
    
    /**
     * Loads player_settings.yml from disk
     */
    public void load() {
        if (!settingsFile.exists()) {
            try {
                settingsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create player_settings.yml!");
                e.printStackTrace();
            }
        }
        settings = YamlConfiguration.loadConfiguration(settingsFile);
    }
    
    /**
     * Saves player_settings.yml to disk
     */
    public void saveSettings() {
        try {
            settings.save(settingsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player_settings.yml!");
            e.printStackTrace();
        }
    }
    
    /**
     * Reloads settings from disk
     */
    public void reload() {
        load();
    }
    
    /**
     * Checks if a player has opted out of quizzes
     * 
     * @param player The player to check
     * @return true if opted out, false if opted in (default)
     */
    public boolean hasOptedOut(Player player) {
        return settings.getBoolean("players." + player.getUniqueId() + ".opted_out", false);
    }
    
    /**
     * Sets a player's opt-out status
     * 
     * @param player The player
     * @param optedOut true to opt out, false to opt in
     */
    public void setOptOut(Player player, boolean optedOut) {
        settings.set("players." + player.getUniqueId() + ".opted_out", optedOut);
        settings.set("players." + player.getUniqueId() + ".name", player.getName());
        saveSettings();
    }
}
