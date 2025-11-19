package com.quizplugin.models;

import org.bukkit.entity.Player;

/**
 * QuizSession - Represents an active quiz session for a player
 * 
 * Purpose:
 * - Tracks the current quiz state for a player
 * - Manages attempt counter and time limits
 * - Used by QuizSessionManager to track who has active quizzes
 * 
 * Lifecycle:
 * 1. Created when a quiz is sent to a player
 * 2. Updated when player submits answers
 * 3. Removed when quiz is completed (correct answer or out of attempts)
 * 
 * Data flow:
 * - QuizSessionManager creates sessions
 * - ChatAnswerListener reads/updates sessions when player answers
 * - RewardHandler accesses session to give rewards
 */
public class QuizSession {
    
    private final Player player;         // The player taking the quiz
    private final Question question;     // The question being asked
    private final String quizType;       // Type of quiz (type1, type2, etc.)
    private int attemptsRemaining;       // How many attempts are left
    private long startTime;              // When the quiz was sent (milliseconds)
    private long timeLimit;              // Time limit in milliseconds (0 = no limit)
    
    /**
     * Creates a new quiz session
     * 
     * @param player The player receiving the quiz
     * @param question The question to ask
     * @param quizType The type of quiz (for tracking/stats)
     * @param maxAttempts Maximum number of attempts allowed
     * @param timeLimitSeconds Time limit in seconds (0 = no limit)
     */
    public QuizSession(Player player, Question question, String quizType, 
                      int maxAttempts, int timeLimitSeconds) {
        this.player = player;
        this.question = question;
        this.quizType = quizType;
        this.attemptsRemaining = maxAttempts;
        this.startTime = System.currentTimeMillis();
        this.timeLimit = timeLimitSeconds > 0 ? timeLimitSeconds * 1000L : 0;
    }
    
    // Getters
    public Player getPlayer() { return player; }
    public Question getQuestion() { return question; }
    public String getQuizType() { return quizType; }
    public int getAttemptsRemaining() { return attemptsRemaining; }
    public long getStartTime() { return startTime; }
    public long getTimeLimit() { return timeLimit; }
    
    /**
     * Decrements the attempts counter
     */
    public void decrementAttempts() {
        attemptsRemaining--;
    }
    
    /**
     * Checks if the time limit has expired
     * 
     * @return true if time is up, false otherwise
     */
    public boolean isTimeExpired() {
        if (timeLimit == 0) {
            return false; // No time limit
        }
        long elapsed = System.currentTimeMillis() - startTime;
        return elapsed > timeLimit;
    }
    
    /**
     * Gets remaining time in seconds
     * 
     * @return seconds remaining, or -1 if no time limit
     */
    public int getRemainingSeconds() {
        if (timeLimit == 0) {
            return -1;
        }
        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = timeLimit - elapsed;
        return Math.max(0, (int) (remaining / 1000));
    }
}
