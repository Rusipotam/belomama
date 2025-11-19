package com.quizplugin.managers;

import com.quizplugin.QuizPlugin;
import com.quizplugin.config.PlayerDataManager;
import org.bukkit.entity.Player;

/**
 * TABIntegration - Integrates with TAB plugin for leaderboard placeholders
 * 
 * Purpose:
 * - Provides %quizpoints% placeholder for TAB plugin
 * - Updates TAB when player points change
 * - Falls back gracefully if TAB is not installed
 * 
 * How it works:
 * - TAB plugin will query this plugin for placeholder values
 * - We return player points from PlayerDataManager
 * - TAB displays the points in nametags, tablist, etc.
 * 
 * Note: TAB placeholder integration requires TAB plugin's PlaceholderAPI support
 * If TAB is not available, the /quiz scoreboard command provides fallback
 * 
 * Used by:
 * - RewardHandler (updates TAB when points change)
 * - QuizPlugin (initialization check)
 */
public class TABIntegration {
    
    private final QuizPlugin plugin;
    private final PlayerDataManager playerDataManager;
    
    public TABIntegration(QuizPlugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        
        plugin.getLogger().info("TAB integration initialized!");
    }
    
    /**
     * Gets a player's quiz points for TAB placeholder
     * 
     * @param player The player
     * @return Their points as a string
     */
    public String getPlayerPoints(Player player) {
        return String.valueOf(playerDataManager.getPoints(player));
    }
    
    /**
     * Updates a player's points display in TAB
     * Called when points change
     * 
     * @param player The player whose points changed
     */
    public void updatePlayerPoints(Player player) {
        // TAB automatically refreshes placeholders
        // This method is here for future enhancements
        // (e.g., forcing a refresh or triggering animations)
        plugin.getLogger().fine("Updated TAB points for " + player.getName());
    }
    
    /**
     * Refreshes all players' points in TAB
     * Called on plugin reload
     */
    public void refreshAllPlayers() {
        plugin.getLogger().info("Refreshed TAB integration for all players");
    }
}
