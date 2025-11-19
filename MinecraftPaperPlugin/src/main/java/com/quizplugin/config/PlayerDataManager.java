package com.quizplugin.config;

import com.quizplugin.QuizPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * PlayerDataManager - Handles players.yml for points and statistics
 * 
 * Purpose:
 * - Stores and manages player quiz points
 * - Tracks quiz statistics (total answered, correct, wrong)
 * - Provides leaderboard data
 * - Integrates with TAB plugin for %quizpoints% placeholder
 * 
 * Data stored per player (UUID-based):
 * - points: Total quiz points earned
 * - total_answered: Number of quizzes attempted
 * - correct_answers: Number correct
 * - wrong_answers: Number wrong
 * 
 * Used by:
 * - RewardHandler (to add points)
 * - TABIntegration (to display points)
 * - QuizCommand (for /quiz points and /quiz scoreboard)
 */
public class PlayerDataManager {
    
    private final QuizPlugin plugin;
    private final File dataFile;
    private FileConfiguration data;
    
    public PlayerDataManager(QuizPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "players.yml");
        load();
    }
    
    /**
     * Loads players.yml from disk
     */
    public void load() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create players.yml!");
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    /**
     * Saves players.yml to disk
     */
    public void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save players.yml!");
            e.printStackTrace();
        }
    }
    
    /**
     * Reloads data from disk
     */
    public void reload() {
        load();
    }
    
    /**
     * Gets a player's quiz points
     * 
     * @param player The player
     * @return Their total points
     */
    public int getPoints(Player player) {
        return data.getInt("players." + player.getUniqueId() + ".points", 0);
    }
    
    /**
     * Adds points to a player
     * 
     * @param player The player
     * @param points Points to add
     */
    public void addPoints(Player player, int points) {
        String path = "players." + player.getUniqueId();
        int currentPoints = data.getInt(path + ".points", 0);
        data.set(path + ".points", currentPoints + points);
        data.set(path + ".name", player.getName()); // Store name for leaderboard display
        saveData();
    }
    
    /**
     * Sets a player's points (admin command)
     * 
     * @param playerUUID The player's UUID
     * @param points Points to set
     */
    public void setPoints(UUID playerUUID, int points) {
        data.set("players." + playerUUID + ".points", points);
        saveData();
    }
    
    /**
     * Records a correct answer
     * 
     * @param player The player
     */
    public void recordCorrectAnswer(Player player) {
        String path = "players." + player.getUniqueId();
        int correct = data.getInt(path + ".correct_answers", 0);
        int total = data.getInt(path + ".total_answered", 0);
        data.set(path + ".correct_answers", correct + 1);
        data.set(path + ".total_answered", total + 1);
        saveData();
    }
    
    /**
     * Records a wrong answer
     * 
     * @param player The player
     */
    public void recordWrongAnswer(Player player) {
        String path = "players." + player.getUniqueId();
        int wrong = data.getInt(path + ".wrong_answers", 0);
        int total = data.getInt(path + ".total_answered", 0);
        data.set(path + ".wrong_answers", wrong + 1);
        data.set(path + ".total_answered", total + 1);
        saveData();
    }
    
    /**
     * Gets top players sorted by points
     * 
     * @param limit Number of top players to return
     * @return List of maps with player name and points
     */
    public List<Map<String, Object>> getTopPlayers(int limit) {
        List<Map<String, Object>> topPlayers = new ArrayList<>();
        
        if (!data.contains("players")) {
            return topPlayers;
        }
        
        for (String uuidString : data.getConfigurationSection("players").getKeys(false)) {
            String path = "players." + uuidString;
            String name = data.getString(path + ".name", "Unknown");
            int points = data.getInt(path + ".points", 0);
            
            Map<String, Object> playerData = new HashMap<>();
            playerData.put("name", name);
            playerData.put("points", points);
            playerData.put("uuid", uuidString);
            topPlayers.add(playerData);
        }
        
        // Sort by points (descending)
        topPlayers.sort((a, b) -> {
            int pointsA = (int) a.get("points");
            int pointsB = (int) b.get("points");
            return Integer.compare(pointsB, pointsA);
        });
        
        // Return top N
        return topPlayers.stream().limit(limit).toList();
    }
}
