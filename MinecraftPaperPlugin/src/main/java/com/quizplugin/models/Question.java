package com.quizplugin.models;

import java.util.List;

/**
 * Question - Represents a single quiz question
 * 
 * Purpose:
 * - Stores all data for one question loaded from quiz type YAML files
 * - Supports both "choice" (A-D) and "freeform" (text input) question types
 * - Contains difficulty, attempts, time limits, and optional resource links
 * 
 * Data source:
 * - Loaded from /quiz_types/*.yml files by QuizManager
 * 
 * YAML structure example:
 * - id: q01
 *   question: "What is 2+2?"
 *   type: choice
 *   options: ["1", "2", "3", "4"]
 *   correct: "D"
 *   difficulty: easy
 *   tag: "Math"
 *   resource: "https://example.com/math-help"
 *   time: 20
 *   attempts: 2
 */
public class Question {
    
    private String id;                  // Unique identifier (e.g., "q01")
    private String questionText;        // The actual question text
    private String type;                // "choice" or "freeform"
    private List<String> options;       // For choice questions: A, B, C, D options
    private String correctAnswer;       // Correct answer (A-D for choice, exact text for freeform)
    private String difficulty;          // "easy", "medium", or "hard"
    private String tag;                 // Optional category (e.g., "Grammar", "History")
    private String resource;            // Optional link to learning resource
    private int timeLimit;              // Time limit in seconds (0 = use default from config)
    private int maxAttempts;            // Maximum attempts allowed (0 = use default from config)
    
    // Full constructor
    public Question(String id, String questionText, String type, List<String> options,
                   String correctAnswer, String difficulty, String tag, String resource,
                   int timeLimit, int maxAttempts) {
        this.id = id;
        this.questionText = questionText;
        this.type = type;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.difficulty = difficulty;
        this.tag = tag;
        this.resource = resource;
        this.timeLimit = timeLimit;
        this.maxAttempts = maxAttempts;
    }
    
    // Getters
    public String getId() { return id; }
    public String getQuestionText() { return questionText; }
    public String getType() { return type; }
    public List<String> getOptions() { return options; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getDifficulty() { return difficulty; }
    public String getTag() { return tag; }
    public String getResource() { return resource; }
    public int getTimeLimit() { return timeLimit; }
    public int getMaxAttempts() { return maxAttempts; }
    
    /**
     * Checks if this is a choice-type question
     */
    public boolean isChoice() {
        return "choice".equalsIgnoreCase(type);
    }
    
    /**
     * Checks if this is a freeform-type question
     */
    public boolean isFreeform() {
        return "freeform".equalsIgnoreCase(type);
    }
    
    /**
     * Validates a player's answer
     * 
     * @param answer The player's answer
     * @return true if correct, false otherwise
     */
    public boolean isCorrect(String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            return false;
        }
        
        if (isChoice()) {
            // For choice questions, match A-D (case-insensitive)
            return correctAnswer.equalsIgnoreCase(answer.trim());
        } else {
            // For freeform questions, match exact text (case-insensitive)
            return correctAnswer.equalsIgnoreCase(answer.trim());
        }
    }
}
