package com.quizplugin.managers;

import com.quizplugin.QuizPlugin;
import com.quizplugin.config.RewardsManager;
import com.quizplugin.models.Question;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RewardHandler - Gives rewards to players for correct answers
 * 
 * Purpose:
 * - Creates reward items with custom names, lore, and enchantments
 * - Adds lore showing quiz date and question ID
 * - Gives items to players and adds points
 * 
 * Reward attributes (from spec):
 * - Custom name (colored)
 * - Colored description
 * - Lore showing date + question ID
 * - Points awarded (tracked in players.yml)
 * 
 * Flow:
 * 1. Player answers correctly
 * 2. ChatAnswerListener calls giveReward()
 * 3. RewardHandler loads reward config for difficulty
 * 4. Creates ItemStack with custom meta
 * 5. Adds item to player inventory
 * 6. Awards points via PlayerDataManager
 * 
 * Used by:
 * - ChatAnswerListener (when player answers correctly)
 */
public class RewardHandler {
    
    private final QuizPlugin plugin;
    private final RewardsManager rewardsManager;
    
    public RewardHandler(QuizPlugin plugin, RewardsManager rewardsManager) {
        this.plugin = plugin;
        this.rewardsManager = rewardsManager;
    }
    
    /**
     * Gives a reward to a player for answering correctly
     * 
     * @param player The player who answered correctly
     * @param question The question they answered
     */
    public void giveReward(Player player, Question question) {
        String difficulty = question.getDifficulty();
        
        // Get reward configuration
        Map<String, Object> rewardData = rewardsManager.getReward(difficulty);
        
        Material material = (Material) rewardData.get("material");
        String name = (String) rewardData.get("name");
        List<String> lore = (List<String>) rewardData.get("lore");
        int points = (int) rewardData.get("points");
        
        // Create reward item
        ItemStack reward = createRewardItem(material, name, lore, question);
        
        // Give item to player
        player.getInventory().addItem(reward);
        
        // Award points
        plugin.getPlayerDataManager().addPoints(player, points);
        
        // Update TAB if enabled
        if (plugin.isTabEnabled() && plugin.getTabIntegration() != null) {
            plugin.getTabIntegration().updatePlayerPoints(player);
        }
        
        plugin.getLogger().info("Gave " + difficulty + " reward to " + player.getName() + " (+" + points + " points)");
    }
    
    /**
     * Creates a reward ItemStack with custom meta
     * 
     * @param material The item material
     * @param name The display name (supports & color codes)
     * @param loreLines Base lore lines from config
     * @param question The question (for ID and date)
     * @return Configured ItemStack
     */
    private ItemStack createRewardItem(Material material, String name, List<String> loreLines, Question question) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name
            Component displayName = Component.text(colorize(name))
                .decoration(TextDecoration.ITALIC, false);
            meta.displayName(displayName);
            
            // Build lore with date and question ID
            List<Component> lore = new ArrayList<>();
            
            // Add configured lore lines
            for (String line : loreLines) {
                lore.add(Component.text(colorize(line))
                    .decoration(TextDecoration.ITALIC, false));
            }
            
            // Add date and question ID
            String dateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            lore.add(Component.text(""));
            lore.add(Component.text(colorize("&7Date: &f" + dateString))
                .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text(colorize("&7Question ID: &f" + question.getId()))
                .decoration(TextDecoration.ITALIC, false));
            
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Converts & color codes to § codes
     * 
     * @param text Text with & codes
     * @return Text with § codes
     */
    private String colorize(String text) {
        return text.replace("&", "§");
    }
}
