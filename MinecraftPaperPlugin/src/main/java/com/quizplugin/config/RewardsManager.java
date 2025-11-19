package com.quizplugin.config;

import com.quizplugin.QuizPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RewardsManager - Handles rewards.yml configuration
 * 
 * Purpose:
 * - Loads and manages reward definitions for each difficulty level
 * - Provides reward data (material, name, lore) to RewardHandler
 * - Supports custom reward configurations including blank templates
 * 
 * Reward structure:
 * - easy: Basic reward (e.g., iron ingot, +1 point)
 * - medium: Advanced reward (e.g., gold ingot, +2 points)
 * - hard: Rare reward (e.g., diamond, +3 points)
 * - custom_1, custom_2, etc: Blank templates for future use
 * 
 * Config fields per reward:
 * - material: Item type (DIAMOND, GOLD_INGOT, etc.)
 * - name: Display name with color codes
 * - lore: List of lore lines
 * - points: Points awarded
 * 
 * Used by:
 * - RewardHandler (to give items to players)
 * - PlayerDataManager (to add points)
 */
public class RewardsManager {
    
    private final QuizPlugin plugin;
    private final File rewardsFile;
    private FileConfiguration rewards;
    
    public RewardsManager(QuizPlugin plugin) {
        this.plugin = plugin;
        this.rewardsFile = new File(plugin.getDataFolder(), "rewards.yml");
        load();
    }
    
    /**
     * Loads rewards.yml from disk
     */
    public void load() {
        if (!rewardsFile.exists()) {
            plugin.saveResource("rewards.yml", false);
        }
        rewards = YamlConfiguration.loadConfiguration(rewardsFile);
    }
    
    /**
     * Reloads rewards from disk
     */
    public void reload() {
        load();
    }
    
    /**
     * Gets reward configuration for a difficulty level
     * 
     * @param difficulty "easy", "medium", or "hard"
     * @return Map containing reward data (material, name, lore, points)
     */
    public Map<String, Object> getReward(String difficulty) {
        Map<String, Object> rewardData = new HashMap<>();
        ConfigurationSection section = rewards.getConfigurationSection(difficulty);
        
        if (section == null) {
            // Return default reward if not found
            rewardData.put("material", Material.PAPER);
            rewardData.put("name", "&aQuiz Reward");
            rewardData.put("lore", List.of("&7Difficulty: " + difficulty));
            rewardData.put("points", 1);
            return rewardData;
        }
        
        String materialName = section.getString("material", "PAPER");
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            material = Material.PAPER;
        }
        
        rewardData.put("material", material);
        rewardData.put("name", section.getString("name", "&aReward"));
        rewardData.put("lore", section.getStringList("lore"));
        rewardData.put("points", section.getInt("points", 1));
        
        return rewardData;
    }
    
    /**
     * Gets points value for a difficulty level
     * 
     * @param difficulty "easy", "medium", or "hard"
     * @return points to award (default: 1)
     */
    public int getPoints(String difficulty) {
        return rewards.getInt(difficulty + ".points", 1);
    }
    
    /**
     * Gets the raw configuration object
     */
    public FileConfiguration getConfig() {
        return rewards;
    }
}
