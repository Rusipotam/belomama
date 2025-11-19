package com.quizplugin.managers;

import com.quizplugin.QuizPlugin;
import com.quizplugin.models.Question;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

/**
 * QuizManager - Loads and manages all quiz types
 * 
 * Purpose:
 * - Loads quiz questions from /quiz_types/*.yml files
 * - Provides random question selection
 * - Manages quiz type availability
 * 
 * File structure:
 * - /plugins/QuizPlugin/quiz_types/type1.yml
 * - /plugins/QuizPlugin/quiz_types/type2.yml
 * - etc.
 * 
 * Each file contains:
 * - List of questions with all properties (id, question, type, correct, etc.)
 * 
 * Used by:
 * - QuizSessionManager (to get random questions)
 */
public class QuizManager {
    
    private final QuizPlugin plugin;
    private final File quizTypesFolder;
    private final Map<String, List<Question>> quizzes; // Map: quiz type name -> list of questions
    
    public QuizManager(QuizPlugin plugin) {
        this.plugin = plugin;
        this.quizTypesFolder = new File(plugin.getDataFolder(), "quiz_types");
        this.quizzes = new HashMap<>();
        load();
    }
    
    /**
     * Loads all quiz type files from /quiz_types/ folder
     */
    public void load() {
        quizzes.clear();
        
        // Create folder if it doesn't exist
        if (!quizTypesFolder.exists()) {
            quizTypesFolder.mkdirs();
            // Create default quiz type files
            createDefaultQuizTypes();
        }
        
        // Load all .yml files in the folder
        File[] files = quizTypesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("No quiz type files found in /quiz_types/ folder!");
            return;
        }
        
        for (File file : files) {
            String quizType = file.getName().replace(".yml", "");
            List<Question> questions = loadQuestionsFromFile(file);
            quizzes.put(quizType, questions);
            plugin.getLogger().info("Loaded " + questions.size() + " questions for quiz type: " + quizType);
        }
    }
    
    /**
     * Loads questions from a single quiz type file
     * 
     * @param file The quiz type YAML file
     * @return List of Question objects
     */
    private List<Question> loadQuestionsFromFile(File file) {
        List<Question> questions = new ArrayList<>();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        if (!config.contains("questions")) {
            plugin.getLogger().warning("No 'questions' section found in " + file.getName());
            return questions;
        }
        
        List<Map<?, ?>> questionMaps = config.getMapList("questions");
        for (Map<?, ?> qMap : questionMaps) {
            try {
                // Extract required fields
                String id = (String) qMap.get("id");
                String questionText = (String) qMap.get("question");
                String type = (String) qMap.get("type");
                String correct = (String) qMap.get("correct");
                
                // Validate required fields
                if (id == null || questionText == null || type == null || correct == null) {
                    plugin.getLogger().warning("Skipping question with missing required fields in " + file.getName());
                    continue;
                }
                
                // Extract optional fields with safe defaults
                String difficulty = qMap.containsKey("difficulty") ? (String) qMap.get("difficulty") : "medium";
                String tag = qMap.containsKey("tag") ? (String) qMap.get("tag") : "";
                String resource = qMap.containsKey("resource") ? (String) qMap.get("resource") : "";
                
                // Safe integer parsing for numeric fields
                int time = 0;
                if (qMap.containsKey("time")) {
                    Object timeObj = qMap.get("time");
                    if (timeObj instanceof Number) {
                        time = ((Number) timeObj).intValue();
                    }
                }
                
                int attempts = 0;
                if (qMap.containsKey("attempts")) {
                    Object attemptsObj = qMap.get("attempts");
                    if (attemptsObj instanceof Number) {
                        attempts = ((Number) attemptsObj).intValue();
                    }
                }
                
                List<String> options = null;
                if ("choice".equalsIgnoreCase(type) && qMap.containsKey("options")) {
                    options = (List<String>) qMap.get("options");
                }
                
                Question question = new Question(id, questionText, type, options, 
                                                correct, difficulty, tag, resource, time, attempts);
                questions.add(question);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load question in " + file.getName() + ": " + e.getMessage());
            }
        }
        
        return questions;
    }
    
    /**
     * Creates default quiz type files if none exist
     */
    private void createDefaultQuizTypes() {
        plugin.saveResource("quiz_types/type1.yml", false);
        plugin.saveResource("quiz_types/type2.yml", false);
        plugin.saveResource("quiz_types/type3.yml", false);
        plugin.saveResource("quiz_types/type4.yml", false);
    }
    
    /**
     * Gets a random question from a specific quiz type
     * 
     * @param quizType The quiz type name
     * @return Random Question, or null if type not found
     */
    public Question getRandomQuestion(String quizType) {
        List<Question> questions = quizzes.get(quizType);
        if (questions == null || questions.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return questions.get(random.nextInt(questions.size()));
    }
    
    /**
     * Checks if a quiz type exists
     * 
     * @param quizType The quiz type name
     * @return true if exists, false otherwise
     */
    public boolean hasQuizType(String quizType) {
        return quizzes.containsKey(quizType);
    }
    
    /**
     * Gets all available quiz type names
     * 
     * @return Set of quiz type names
     */
    public Set<String> getAvailableQuizTypes() {
        return quizzes.keySet();
    }
    
    /**
     * Reloads all quiz types
     */
    public void reload() {
        load();
    }
}
