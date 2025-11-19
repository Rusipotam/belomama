package com.quizplugin;

import com.quizplugin.commands.QuizCommand;
import com.quizplugin.config.*;
import com.quizplugin.listeners.ChatAnswerListener;
import com.quizplugin.managers.*;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * QuizPlugin - Main plugin class
 * 
 * Purpose:
 * - Initializes and manages the quiz system for Paper 1.21.8
 * - Integrates with LuckPerms for group-based quiz distribution
 * - Optionally integrates with TAB plugin for leaderboard placeholders
 * 
 * Flow:
 * 1. onEnable() loads all configurations (config.yml, messages.yml, rewards.yml, etc.)
 * 2. Detects and connects to LuckPerms API (required)
 * 3. Detects TAB plugin (optional, for %quizpoints% placeholder)
 * 4. Registers event listeners for chat-based answer handling
 * 5. Registers /quiz command for all player/admin operations
 * 6. Starts quiz scheduler that periodically sends quizzes to eligible players
 * 
 * Config interactions:
 * - config.yml: quiz_interval_seconds determines scheduler timing
 * - All managers (QuizManager, PlayerDataManager, etc.) are initialized here
 */
public class QuizPlugin extends JavaPlugin {
    
    // Core managers - handle different aspects of the plugin
    private ConfigManager configManager;
    private MessagesManager messagesManager;
    private RewardsManager rewardsManager;
    private PlayerDataManager playerDataManager;
    private PlayerSettingsManager playerSettingsManager;
    private PlayerGroupManager playerGroupManager;
    private QuizManager quizManager;
    private QuizSessionManager quizSessionManager;
    private GroupManager groupManager;
    private RewardHandler rewardHandler;
    private TABIntegration tabIntegration;
    
    // External API integrations
    private LuckPerms luckPerms;
    private boolean tabEnabled = false;
    
    // Quiz scheduler task
    private BukkitTask quizTask;
    
    /**
     * Plugin startup logic
     * Called when the plugin is enabled by the server
     */
    @Override
    public void onEnable() {
        getLogger().info("QuizPlugin is starting...");
        
        // Step 1: Initialize LuckPerms API (required dependency)
        if (!setupLuckPerms()) {
            getLogger().severe("LuckPerms not found! This plugin requires LuckPerms to function.");
            getLogger().severe("Disabling QuizPlugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("LuckPerms integration successful!");
        
        // Step 2: Check for TAB plugin (optional)
        if (getServer().getPluginManager().getPlugin("TAB") != null) {
            tabEnabled = true;
            getLogger().info("TAB plugin detected - leaderboard integration enabled!");
        } else {
            getLogger().info("TAB plugin not found - using fallback scoreboard only");
        }
        
        // Step 3: Initialize all configuration managers
        // These handle loading/saving YAML files in the plugin data folder
        configManager = new ConfigManager(this);
        messagesManager = new MessagesManager(this);
        rewardsManager = new RewardsManager(this);
        playerDataManager = new PlayerDataManager(this);
        playerSettingsManager = new PlayerSettingsManager(this);
        playerGroupManager = new PlayerGroupManager(this);
        
        // Step 4: Initialize quiz manager (loads all quiz types from /quiz_types/ folder)
        quizManager = new QuizManager(this);
        
        // Step 5: Initialize session manager (tracks active quizzes per player)
        quizSessionManager = new QuizSessionManager(this);
        
        // Step 6: Initialize group manager (LuckPerms integration)
        groupManager = new GroupManager(this, luckPerms);
        
        // Step 7: Initialize reward handler (gives items to players)
        rewardHandler = new RewardHandler(this, rewardsManager);
        
        // Step 8: Initialize TAB integration if available
        if (tabEnabled) {
            tabIntegration = new TABIntegration(this, playerDataManager);
        }
        
        // Step 9: Register event listeners
        getServer().getPluginManager().registerEvents(
            new ChatAnswerListener(this, quizSessionManager, rewardHandler, playerDataManager, messagesManager), 
            this
        );
        
        // Step 10: Register commands
        getCommand("quiz").setExecutor(new QuizCommand(this));
        
        // Step 11: Start quiz scheduler
        startQuizScheduler();
        
        getLogger().info("QuizPlugin enabled successfully!");
    }
    
    /**
     * Plugin shutdown logic
     * Saves all data and cancels scheduled tasks
     */
    @Override
    public void onDisable() {
        // Cancel quiz scheduler
        if (quizTask != null) {
            quizTask.cancel();
        }
        
        // Save all player data before shutdown
        if (playerDataManager != null) {
            playerDataManager.saveData();
        }
        
        if (playerSettingsManager != null) {
            playerSettingsManager.saveSettings();
        }
        
        if (playerGroupManager != null) {
            playerGroupManager.saveGroups();
        }
        
        getLogger().info("QuizPlugin disabled. All data saved.");
    }
    
    /**
     * Connects to LuckPerms API
     * 
     * @return true if LuckPerms is available, false otherwise
     */
    private boolean setupLuckPerms() {
        RegisteredServiceProvider<LuckPerms> provider = 
            Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        
        if (provider != null) {
            luckPerms = provider.getProvider();
            return true;
        }
        return false;
    }
    
    /**
     * Starts the quiz scheduler
     * 
     * Reads quiz_interval_seconds from config.yml and schedules a repeating task
     * that sends quizzes to random eligible players
     * 
     * The scheduler:
     * 1. Picks a random quiz type from enabled types
     * 2. Finds online players who can receive that quiz type (based on LuckPerms group)
     * 3. Filters out players who have opted out
     * 4. Sends the quiz to one random eligible player
     */
    private void startQuizScheduler() {
        int intervalSeconds = configManager.getQuizInterval();
        long intervalTicks = intervalSeconds * 20L; // Convert seconds to ticks (20 ticks = 1 second)
        
        // Start after 1 second (20 ticks), then repeat every intervalTicks
        quizTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            // Quiz distribution logic handled by QuizSessionManager
            quizSessionManager.distributeRandomQuiz();
        }, 20L, intervalTicks);
        
        getLogger().info("Quiz scheduler started! Quizzes will be sent every " + intervalSeconds + " seconds.");
    }
    
    /**
     * Reloads all plugin configurations
     * Called by /quiz reload command
     */
    public void reloadConfigs() {
        configManager.reload();
        messagesManager.reload();
        rewardsManager.reload();
        playerDataManager.reload();
        playerSettingsManager.reload();
        playerGroupManager.reload();
        quizManager.reload();
        
        // Restart scheduler with new interval
        if (quizTask != null) {
            quizTask.cancel();
        }
        startQuizScheduler();
        
        getLogger().info("All configurations reloaded!");
    }
    
    // Getters for managers (used by other classes)
    public ConfigManager getConfigManager() { return configManager; }
    public MessagesManager getMessagesManager() { return messagesManager; }
    public RewardsManager getRewardsManager() { return rewardsManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public PlayerSettingsManager getPlayerSettingsManager() { return playerSettingsManager; }
    public PlayerGroupManager getPlayerGroupManager() { return playerGroupManager; }
    public QuizManager getQuizManager() { return quizManager; }
    public QuizSessionManager getQuizSessionManager() { return quizSessionManager; }
    public GroupManager getGroupManager() { return groupManager; }
    public RewardHandler getRewardHandler() { return rewardHandler; }
    public TABIntegration getTabIntegration() { return tabIntegration; }
    public LuckPerms getLuckPerms() { return luckPerms; }
    public boolean isTabEnabled() { return tabEnabled; }
}
