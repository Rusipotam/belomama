package com.quizplugin.config;

import com.quizplugin.QuizPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * PlayerGroupManager - Handles manual group overrides for players
 * 
 * Purpose:
 * - Allows admins to manually assign quiz groups to players
 * - Overrides LuckPerms groups for quiz distribution
 * - Persists group assignments across server restarts
 * 
 * Data structure:
 * - Stores player UUID with assigned group name
 * - Works with both online and offline players
 * - If no override exists, falls back to LuckPerms group
 * 
 * Used by:
 * - GroupManager (checks for overrides before using LuckPerms)
 * - QuizCommand (for /quiz setgroup command)
 */
public class PlayerGroupManager {
    
    private final QuizPlugin plugin;
    private final File groupFile;
    private FileConfiguration groups;
    
    public PlayerGroupManager(QuizPlugin plugin) {
        this.plugin = plugin;
        this.groupFile = new File(plugin.getDataFolder(), "player_groups.yml");
        load();
    }
    
    /**
     * Loads player_groups.yml from disk
     */
    public void load() {
        if (!groupFile.exists()) {
            try {
                groupFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create player_groups.yml!");
                e.printStackTrace();
            }
        }
        groups = YamlConfiguration.loadConfiguration(groupFile);
    }
    
    /**
     * Saves player_groups.yml to disk
     */
    public void saveGroups() {
        try {
            groups.save(groupFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player_groups.yml!");
            e.printStackTrace();
        }
    }
    
    /**
     * Reloads groups from disk
     */
    public void reload() {
        load();
    }
    
    /**
     * Checks if a player has a manual group override (by UUID)
     * Works for both online and offline players
     * 
     * @param uuid The player's UUID
     * @return true if they have an override set
     */
    public boolean hasOverride(UUID uuid) {
        return groups.contains("players." + uuid + ".group");
    }
    
    /**
     * Gets a player's manual group override (by UUID)
     * Works for both online and offline players
     * 
     * @param uuid The player's UUID
     * @return Their override group, or null if none
     */
    public String getOverrideGroup(UUID uuid) {
        return groups.getString("players." + uuid + ".group");
    }
    
    /**
     * Sets a player's group override (works for offline players)
     * 
     * @param player The offline player (can be online or offline)
     * @param group The group to assign (e.g., "A1-A2", "B1-B2", "C1-C2")
     */
    public void setGroup(OfflinePlayer player, String group) {
        groups.set("players." + player.getUniqueId() + ".group", group);
        groups.set("players." + player.getUniqueId() + ".name", player.getName());
        saveGroups();
    }
    
    /**
     * Removes a player's group override (by UUID)
     * Returns them to using their LuckPerms group
     * 
     * @param uuid The player's UUID
     */
    public void removeOverride(UUID uuid) {
        groups.set("players." + uuid, null);
        saveGroups();
    }
}
