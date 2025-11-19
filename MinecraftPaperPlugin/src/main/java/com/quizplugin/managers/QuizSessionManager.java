package com.quizplugin.managers;

import com.quizplugin.QuizPlugin;
import com.quizplugin.models.Question;
import com.quizplugin.models.QuizSession;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * QuizSessionManager - Manages active quiz sessions for players
 * 
 * Purpose:
 * - Tracks which players currently have active quizzes
 * - Distributes quizzes to eligible players
 * - Provides session lookup for answer validation
 * - Cleans up expired or completed sessions
 * 
 * Session lifecycle:
 * 1. distributeRandomQuiz() picks a random quiz type and eligible player
 * 2. Creates QuizSession and stores it in activeSessions map
 * 3. Sends quiz question to player
 * 4. ChatAnswerListener checks activeSessions when player answers
 * 5. Session removed when correct answer or out of attempts
 * 
 * Used by:
 * - QuizPlugin (scheduled task calls distributeRandomQuiz())
 * - ChatAnswerListener (looks up sessions when player answers)
 * - QuizCommand (for /quiz give command)
 */
public class QuizSessionManager {
    
    private final QuizPlugin plugin;
    private final Map<UUID, QuizSession> activeSessions; // UUID -> active quiz session
    
    public QuizSessionManager(QuizPlugin plugin) {
        this.plugin = plugin;
        this.activeSessions = new HashMap<>();
    }
    
    /**
     * Distributes a random quiz to a random eligible player
     * Called by the quiz scheduler every X seconds
     * 
     * Logic:
     * 1. Pick random quiz type from enabled types
     * 2. Find all online players who can receive that type
     * 3. Filter out players with active quizzes or opted out
     * 4. Pick random player from eligible list
     * 5. Send quiz to that player
     */
    public void distributeRandomQuiz() {
        boolean debug = plugin.getConfigManager().isDebugMode();
        
        if (debug) {
            plugin.getLogger().info("[DEBUG] Quiz distribution started...");
        }
        
        // Get enabled quiz types from config
        List<String> enabledTypes = plugin.getConfigManager().getEnabledQuizTypes();
        if (enabledTypes.isEmpty()) {
            plugin.getLogger().warning("No enabled quiz types in config.yml!");
            return;
        }
        
        // Pick random quiz type
        Random random = new Random();
        String quizType = enabledTypes.get(random.nextInt(enabledTypes.size()));
        
        if (debug) {
            plugin.getLogger().info("[DEBUG] Selected quiz type: " + quizType);
            plugin.getLogger().info("[DEBUG] Online players: " + Bukkit.getOnlinePlayers().size());
        }
        
        // Find eligible players
        List<Player> eligiblePlayers = new ArrayList<>();
        int totalPlayers = 0;
        int playersWithActiveQuiz = 0;
        int playersOptedOut = 0;
        int playersWrongGroup = 0;
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            totalPlayers++;
            
            // Skip if player has active quiz
            if (hasActiveSession(player)) {
                playersWithActiveQuiz++;
                if (debug) {
                    plugin.getLogger().info("[DEBUG] " + player.getName() + " - has active quiz");
                }
                continue;
            }
            
            // Skip if player opted out
            if (plugin.getPlayerSettingsManager().hasOptedOut(player)) {
                playersOptedOut++;
                if (debug) {
                    plugin.getLogger().info("[DEBUG] " + player.getName() + " - opted out");
                }
                continue;
            }
            
            // Skip if player's group doesn't allow this quiz type
            String playerGroup = plugin.getGroupManager().getPlayerGroup(player);
            if (!plugin.getGroupManager().canReceiveQuizType(player, quizType)) {
                playersWrongGroup++;
                if (debug) {
                    plugin.getLogger().info("[DEBUG] " + player.getName() + " (group: " + playerGroup + ") - cannot receive " + quizType);
                }
                continue;
            }
            
            if (debug) {
                plugin.getLogger().info("[DEBUG] " + player.getName() + " (group: " + playerGroup + ") - eligible!");
            }
            eligiblePlayers.add(player);
        }
        
        // Send quiz to random eligible player
        if (!eligiblePlayers.isEmpty()) {
            Player selectedPlayer = eligiblePlayers.get(random.nextInt(eligiblePlayers.size()));
            sendQuiz(selectedPlayer, quizType);
            if (debug) {
                plugin.getLogger().info("[DEBUG] Quiz sent to: " + selectedPlayer.getName());
            }
        } else {
            plugin.getLogger().warning("No eligible players for quiz distribution!");
            if (debug) {
                plugin.getLogger().info("[DEBUG] Distribution summary:");
                plugin.getLogger().info("[DEBUG] - Total online: " + totalPlayers);
                plugin.getLogger().info("[DEBUG] - Has active quiz: " + playersWithActiveQuiz);
                plugin.getLogger().info("[DEBUG] - Opted out: " + playersOptedOut);
                plugin.getLogger().info("[DEBUG] - Wrong group for " + quizType + ": " + playersWrongGroup);
            }
        }
    }
    
    /**
     * Sends a quiz to a specific player
     * 
     * @param player The player to receive the quiz
     * @param quizType The quiz type to send
     */
    public void sendQuiz(Player player, String quizType) {
        // Get random question from quiz type
        Question question = plugin.getQuizManager().getRandomQuestion(quizType);
        if (question == null) {
            plugin.getLogger().warning("No questions found for quiz type: " + quizType);
            return;
        }
        
        // Determine time limit and max attempts
        int timeLimit = question.getTimeLimit() > 0 ? 
            question.getTimeLimit() : 
            plugin.getConfigManager().getTimeLimit(question.getDifficulty());
            
        int maxAttempts = question.getMaxAttempts() > 0 ?
            question.getMaxAttempts() :
            plugin.getConfigManager().getMaxAttempts(question.getDifficulty());
        
        // Create quiz session
        QuizSession session = new QuizSession(player, question, quizType, maxAttempts, timeLimit);
        activeSessions.put(player.getUniqueId(), session);
        
        // Send quiz to player
        sendQuizMessage(player, question, timeLimit, maxAttempts);
        
        plugin.getLogger().info("Sent quiz '" + question.getId() + "' to " + player.getName());
    }
    
    /**
     * Sends the quiz question message to the player
     * 
     * @param player The player
     * @param question The question
     * @param timeLimit Time limit in seconds
     * @param maxAttempts Maximum attempts
     */
    private void sendQuizMessage(Player player, Question question, int timeLimit, int maxAttempts) {
        // Send header
        Component header = plugin.getMessagesManager().getComponent("start_quiz");
        player.sendMessage(header);
        
        // Send question
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("§6§l" + question.getQuestionText()));
        player.sendMessage(Component.text(""));
        
        // If choice question, show options
        if (question.isChoice() && question.getOptions() != null) {
            List<String> options = question.getOptions();
            String[] labels = {"A", "B", "C", "D"};
            for (int i = 0; i < options.size() && i < 4; i++) {
                player.sendMessage(Component.text("§e" + labels[i] + ") §f" + options.get(i)));
            }
            player.sendMessage(Component.text(""));
        }
        
        // Send instructions
        player.sendMessage(Component.text("§7Difficulty: §f" + question.getDifficulty()));
        player.sendMessage(Component.text("§7Time limit: §f" + timeLimit + " seconds"));
        player.sendMessage(Component.text("§7Max attempts: §f" + maxAttempts));
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("§aAnswer with: §f!youranswer"));
        player.sendMessage(Component.text(""));
    }
    
    /**
     * Checks if a player has an active quiz session
     * 
     * @param player The player
     * @return true if they have an active quiz
     */
    public boolean hasActiveSession(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }
    
    /**
     * Gets a player's active quiz session
     * 
     * @param player The player
     * @return Their QuizSession, or null if none
     */
    public QuizSession getSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }
    
    /**
     * Removes a player's active quiz session
     * 
     * @param player The player
     */
    public void removeSession(Player player) {
        activeSessions.remove(player.getUniqueId());
    }
    
    /**
     * Gets count of active sessions
     * 
     * @return Number of players with active quizzes
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
}
