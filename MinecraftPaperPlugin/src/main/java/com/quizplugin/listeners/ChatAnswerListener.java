package com.quizplugin.listeners;

import com.quizplugin.QuizPlugin;
import com.quizplugin.config.MessagesManager;
import com.quizplugin.config.PlayerDataManager;
import com.quizplugin.managers.QuizSessionManager;
import com.quizplugin.managers.RewardHandler;
import com.quizplugin.models.Question;
import com.quizplugin.models.QuizSession;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * ChatAnswerListener - Handles player answers via chat
 * 
 * Purpose:
 * - Listens to chat messages for quiz answers
 * - Validates answers starting with !
 * - Checks correctness and manages attempts
 * - Awards rewards or provides feedback
 * 
 * Answer format (from spec):
 * - Player types: !answer
 * - Messages without ! are ignored
 * - Extract text after ! for validation
 * 
 * Flow:
 * 1. Player sends chat message
 * 2. Check if it starts with !
 * 3. Check if player has active quiz session
 * 4. Validate answer against question
 * 5. If correct: reward + points
 * 6. If wrong: decrement attempts, provide feedback
 * 7. If out of attempts: show correct answer + resource link
 * 
 * Used by:
 * - Bukkit event system (automatically called on chat)
 */
public class ChatAnswerListener implements Listener {
    
    private final QuizPlugin plugin;
    private final QuizSessionManager sessionManager;
    private final RewardHandler rewardHandler;
    private final PlayerDataManager playerDataManager;
    private final MessagesManager messagesManager;
    
    public ChatAnswerListener(QuizPlugin plugin, QuizSessionManager sessionManager,
                             RewardHandler rewardHandler, PlayerDataManager playerDataManager,
                             MessagesManager messagesManager) {
        this.plugin = plugin;
        this.sessionManager = sessionManager;
        this.rewardHandler = rewardHandler;
        this.playerDataManager = playerDataManager;
        this.messagesManager = messagesManager;
    }
    
    /**
     * Handles chat events to detect quiz answers
     * 
     * @param event The async chat event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        
        // Convert message to plain text
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        
        // Ignore messages that don't start with !
        if (!message.startsWith("!")) {
            return;
        }
        
        // Check if player has an active quiz
        if (!sessionManager.hasActiveSession(player)) {
            return;
        }
        
        // Cancel the chat event so the answer isn't broadcast
        event.setCancelled(true);
        
        // Extract answer (everything after !)
        String answer = message.substring(1).trim();
        
        // Get quiz session
        QuizSession session = sessionManager.getSession(player);
        Question question = session.getQuestion();
        
        // Check if time expired
        if (session.isTimeExpired()) {
            player.sendMessage(Component.text("§c§lTime's up!"));
            player.sendMessage(Component.text("§7The correct answer was: §f" + question.getCorrectAnswer()));
            
            if (question.getResource() != null && !question.getResource().isEmpty()) {
                String linkMsg = messagesManager.getMessage("link_message", "%link%", question.getResource());
                player.sendMessage(Component.text(linkMsg));
            }
            
            sessionManager.removeSession(player);
            playerDataManager.recordWrongAnswer(player);
            return;
        }
        
        // Validate answer
        if (question.isCorrect(answer)) {
            // Correct answer!
            handleCorrectAnswer(player, session);
        } else {
            // Wrong answer
            handleWrongAnswer(player, session, answer);
        }
    }
    
    /**
     * Handles a correct answer
     * 
     * @param player The player
     * @param session Their quiz session
     */
    private void handleCorrectAnswer(Player player, QuizSession session) {
        Question question = session.getQuestion();
        
        // Send success message
        Component successMsg = messagesManager.getComponent("correct_answer");
        player.sendMessage(successMsg);
        player.sendMessage(Component.text(""));
        
        // Give reward
        rewardHandler.giveReward(player, question);
        
        // Record statistics
        playerDataManager.recordCorrectAnswer(player);
        
        // Remove session
        sessionManager.removeSession(player);
        
        plugin.getLogger().info(player.getName() + " answered correctly: " + question.getId());
    }
    
    /**
     * Handles a wrong answer
     * 
     * @param player The player
     * @param session Their quiz session
     * @param answer Their submitted answer
     */
    private void handleWrongAnswer(Player player, QuizSession session, String answer) {
        Question question = session.getQuestion();
        
        // Decrement attempts
        session.decrementAttempts();
        
        if (session.getAttemptsRemaining() > 0) {
            // Still has attempts left
            Component wrongMsg = messagesManager.getComponent("wrong_answer");
            player.sendMessage(wrongMsg);
            player.sendMessage(Component.text("§7Attempts remaining: §f" + session.getAttemptsRemaining()));
            player.sendMessage(Component.text(""));
        } else {
            // Out of attempts
            String outOfAttemptsMsg = messagesManager.getMessage("out_of_attempts", 
                "%answer%", question.getCorrectAnswer());
            player.sendMessage(Component.text(outOfAttemptsMsg));
            
            // Provide resource link if available
            if (question.getResource() != null && !question.getResource().isEmpty()) {
                String linkMsg = messagesManager.getMessage("link_message", 
                    "%link%", question.getResource());
                player.sendMessage(Component.text(linkMsg));
            }
            
            player.sendMessage(Component.text(""));
            
            // Record failure and remove session
            playerDataManager.recordWrongAnswer(player);
            sessionManager.removeSession(player);
            
            plugin.getLogger().info(player.getName() + " failed quiz: " + question.getId());
        }
    }
}
