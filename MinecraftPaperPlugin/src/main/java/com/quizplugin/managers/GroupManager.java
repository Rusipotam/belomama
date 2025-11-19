package com.quizplugin.managers;

import com.quizplugin.QuizPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * GroupManager - Integrates with LuckPerms to determine player groups
 * 
 * Purpose:
 * - Fetches a player's primary group from LuckPerms
 * - Maps groups to allowed quiz types using config.yml settings
 * - Determines which quizzes a player can receive
 * 
 * Group hierarchy (from spec):
 * - A1-A2: Beginner (receives type1, type2)
 * - B1-B2: Intermediate (receives type1, type2, type3)
 * - C1-C2: Advanced (receives all types)
 * 
 * Integration flow:
 * 1. Player joins or quiz is sent
 * 2. GroupManager queries LuckPerms for primary group
 * 3. Looks up allowed quiz types in config.yml
 * 4. Returns list of types this player can receive
 * 
 * Used by:
 * - QuizSessionManager (to filter available quiz types per player)
 */
public class GroupManager {
    
    private final QuizPlugin plugin;
    private final LuckPerms luckPerms;
    
    public GroupManager(QuizPlugin plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
    }
    
    /**
     * Gets a player's group for quiz purposes
     * Checks manual override first, then falls back to LuckPerms
     * 
     * @param player The player
     * @return Group name (e.g., "A1-A2", "B1-B2", "C1-C2")
     */
    public String getPlayerGroup(Player player) {
        // Check if there's a manual override first (using UUID for offline persistence)
        if (plugin.getPlayerGroupManager().hasOverride(player.getUniqueId())) {
            return plugin.getPlayerGroupManager().getOverrideGroup(player.getUniqueId());
        }
        
        // Otherwise, use LuckPerms group
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            plugin.getLogger().warning("Could not fetch LuckPerms user for " + player.getName());
            return "default";
        }
        
        String primaryGroup = user.getPrimaryGroup();
        return primaryGroup != null ? primaryGroup : "default";
    }
    
    /**
     * Gets allowed quiz types for a player based on their group
     * 
     * @param player The player
     * @return List of quiz types they can receive
     */
    public List<String> getAllowedQuizTypes(Player player) {
        String group = getPlayerGroup(player);
        return plugin.getConfigManager().getAllowedTypesForGroup(group);
    }
    
    /**
     * Checks if a player can receive a specific quiz type
     * 
     * @param player The player
     * @param quizType The quiz type to check
     * @return true if player's group allows this quiz type
     */
    public boolean canReceiveQuizType(Player player, String quizType) {
        List<String> allowedTypes = getAllowedQuizTypes(player);
        return allowedTypes.contains(quizType);
    }
}
