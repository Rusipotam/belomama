package com.quizplugin.commands;

import com.quizplugin.QuizPlugin;
import com.quizplugin.config.PlayerDataManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * QuizCommand - Handles all /quiz subcommands
 * 
 * Purpose:
 * - Provides player commands: optout, optin, points, scoreboard
 * - Provides admin commands: broadcast, reload, setpoints, setgroup, give
 * - Routes subcommands to appropriate handlers
 * 
 * Command structure:
 * - /quiz optout - Disable quizzes
 * - /quiz optin - Enable quizzes
 * - /quiz points - View your points
 * - /quiz scoreboard - View leaderboard
 * - /quiz broadcast <message> - Admin broadcast
 * - /quiz reload - Admin reload configs
 * - /quiz setpoints <player> <points> - Admin set points
 * - /quiz setgroup <player> <group> - Admin set quiz group override
 * - /quiz give <player> <type> - Admin give quiz
 * 
 * Permissions:
 * - quizplugin.optout, quizplugin.optin, quizplugin.points, quizplugin.scoreboard (players)
 * - quizplugin.broadcast, quizplugin.reload, quizplugin.setpoints, quizplugin.setgroup, quizplugin.give (admins)
 */
public class QuizCommand implements CommandExecutor {
    
    private final QuizPlugin plugin;
    
    public QuizCommand(QuizPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("§6§lQuizPlugin Commands:"));
            sender.sendMessage(Component.text("§e/quiz optout §7- Disable quizzes"));
            sender.sendMessage(Component.text("§e/quiz optin §7- Enable quizzes"));
            sender.sendMessage(Component.text("§e/quiz points §7- View your points"));
            sender.sendMessage(Component.text("§e/quiz scoreboard §7- View leaderboard"));
            
            if (sender.hasPermission("quizplugin.admin")) {
                sender.sendMessage(Component.text(""));
                sender.sendMessage(Component.text("§c§lAdmin Commands:"));
                sender.sendMessage(Component.text("§e/quiz broadcast <message> §7- Broadcast message"));
                sender.sendMessage(Component.text("§e/quiz reload §7- Reload configs"));
                sender.sendMessage(Component.text("§e/quiz setpoints <player> <points> §7- Set points"));
                sender.sendMessage(Component.text("§e/quiz setgroup <player> <group> §7- Set quiz group"));
                sender.sendMessage(Component.text("§e/quiz give <player> <type> §7- Give quiz"));
            }
            return true;
        }
        
        String subcommand = args[0].toLowerCase();
        
        switch (subcommand) {
            case "optout":
                return handleOptOut(sender);
            case "optin":
                return handleOptIn(sender);
            case "points":
                return handlePoints(sender);
            case "scoreboard":
                return handleScoreboard(sender);
            case "broadcast":
                return handleBroadcast(sender, args);
            case "reload":
                return handleReload(sender);
            case "setpoints":
                return handleSetPoints(sender, args);
            case "setgroup":
                return handleSetGroup(sender, args);
            case "give":
                return handleGive(sender, args);
            default:
                sender.sendMessage(Component.text("§cUnknown subcommand. Use /quiz for help."));
                return true;
        }
    }
    
    /**
     * Handles /quiz optout
     */
    private boolean handleOptOut(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("§cOnly players can use this command."));
            return true;
        }
        
        Player player = (Player) sender;
        plugin.getPlayerSettingsManager().setOptOut(player, true);
        
        Component msg = plugin.getMessagesManager().getComponent("optout_enabled");
        player.sendMessage(msg);
        return true;
    }
    
    /**
     * Handles /quiz optin
     */
    private boolean handleOptIn(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("§cOnly players can use this command."));
            return true;
        }
        
        Player player = (Player) sender;
        plugin.getPlayerSettingsManager().setOptOut(player, false);
        
        Component msg = plugin.getMessagesManager().getComponent("optout_disabled");
        player.sendMessage(msg);
        return true;
    }
    
    /**
     * Handles /quiz points
     */
    private boolean handlePoints(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("§cOnly players can use this command."));
            return true;
        }
        
        Player player = (Player) sender;
        int points = plugin.getPlayerDataManager().getPoints(player);
        
        player.sendMessage(Component.text("§6§lYour Quiz Points: §f" + points));
        return true;
    }
    
    /**
     * Handles /quiz scoreboard
     * Fallback scoreboard if TAB is not available
     */
    private boolean handleScoreboard(CommandSender sender) {
        PlayerDataManager dataManager = plugin.getPlayerDataManager();
        List<Map<String, Object>> topPlayers = dataManager.getTopPlayers(10);
        
        sender.sendMessage(Component.text("§6§l=== Quiz Leaderboard ==="));
        sender.sendMessage(Component.text(""));
        
        if (topPlayers.isEmpty()) {
            sender.sendMessage(Component.text("§7No data yet!"));
        } else {
            int rank = 1;
            for (Map<String, Object> playerData : topPlayers) {
                String name = (String) playerData.get("name");
                int points = (int) playerData.get("points");
                
                String medal = "§7";
                if (rank == 1) medal = "§6§l";
                else if (rank == 2) medal = "§e§l";
                else if (rank == 3) medal = "§c§l";
                
                sender.sendMessage(Component.text(medal + rank + ". §f" + name + " §7- §f" + points + " points"));
                rank++;
            }
        }
        
        sender.sendMessage(Component.text(""));
        return true;
    }
    
    /**
     * Handles /quiz broadcast <message>
     * Admin command to broadcast messages to all players
     */
    private boolean handleBroadcast(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quizplugin.broadcast")) {
            sender.sendMessage(Component.text("§cYou don't have permission to use this command."));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(Component.text("§cUsage: /quiz broadcast <message>"));
            return true;
        }
        
        // Join all args after "broadcast" into message
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }
        
        String broadcastMsg = message.toString().trim();
        String colorized = broadcastMsg.replace("&", "§");
        
        // Broadcast to all players
        Bukkit.broadcast(Component.text("§6§l[Quiz] §r" + colorized));
        
        sender.sendMessage(Component.text("§aMessage broadcasted!"));
        return true;
    }
    
    /**
     * Handles /quiz reload
     */
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("quizplugin.reload")) {
            sender.sendMessage(Component.text("§cYou don't have permission to use this command."));
            return true;
        }
        
        plugin.reloadConfigs();
        sender.sendMessage(Component.text("§aQuizPlugin reloaded successfully!"));
        return true;
    }
    
    /**
     * Handles /quiz setpoints <player> <points>
     */
    private boolean handleSetPoints(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quizplugin.setpoints")) {
            sender.sendMessage(Component.text("§cYou don't have permission to use this command."));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(Component.text("§cUsage: /quiz setpoints <player> <points>"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("§cPlayer not found."));
            return true;
        }
        
        int points;
        try {
            points = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("§cInvalid number."));
            return true;
        }
        
        plugin.getPlayerDataManager().setPoints(target.getUniqueId(), points);
        sender.sendMessage(Component.text("§aSet " + target.getName() + "'s points to " + points));
        return true;
    }
    
    /**
     * Handles /quiz setgroup <player> <group>
     * Admin command to manually assign a quiz group to a player
     * Works for both online and offline players
     */
    private boolean handleSetGroup(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quizplugin.setgroup")) {
            sender.sendMessage(Component.text("§cYou don't have permission to use this command."));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(Component.text("§cUsage: /quiz setgroup <player> <group|clear>"));
            sender.sendMessage(Component.text("§7Example: /quiz setgroup PlayerName A1-A2"));
            sender.sendMessage(Component.text("§7Use 'clear' to remove override: /quiz setgroup PlayerName clear"));
            sender.sendMessage(Component.text("§7Works for both online and offline players."));
            return true;
        }
        
        // Use OfflinePlayer to support both online and offline players
        @SuppressWarnings("deprecation")
        org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            sender.sendMessage(Component.text("§cPlayer not found or has never joined the server."));
            return true;
        }
        
        String group = args[2];
        String playerName = target.getName() != null ? target.getName() : args[1];
        
        // Handle clearing the override
        if (group.equalsIgnoreCase("clear")) {
            plugin.getPlayerGroupManager().removeOverride(target.getUniqueId());
            sender.sendMessage(Component.text("§aCleared group override for " + playerName));
            sender.sendMessage(Component.text("§7They will now use their LuckPerms group."));
            return true;
        }
        
        // Set the group override (persists even when offline)
        plugin.getPlayerGroupManager().setGroup(target, group);
        sender.sendMessage(Component.text("§aSet " + playerName + "'s quiz group to: " + group));
        sender.sendMessage(Component.text("§7This overrides their LuckPerms group for quiz distribution."));
        sender.sendMessage(Component.text("§7Override persists even when offline."));
        return true;
    }
    
    /**
     * Handles /quiz give <player> <type>
     */
    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quizplugin.give")) {
            sender.sendMessage(Component.text("§cYou don't have permission to use this command."));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(Component.text("§cUsage: /quiz give <player> <type>"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("§cPlayer not found."));
            return true;
        }
        
        String quizType = args[2];
        
        // Check if quiz type exists
        if (!plugin.getQuizManager().hasQuizType(quizType)) {
            sender.sendMessage(Component.text("§cQuiz type not found: " + quizType));
            return true;
        }
        
        // Send quiz
        plugin.getQuizSessionManager().sendQuiz(target, quizType);
        sender.sendMessage(Component.text("§aSent quiz '" + quizType + "' to " + target.getName()));
        return true;
    }
}
