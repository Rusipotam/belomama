# QuizPlugin - Minecraft Paper Plugin

## Project Overview
This is a comprehensive Minecraft Paper plugin (version 1.21.8) that integrates with LuckPerms to send educational quizzes to players based on their permission groups. The plugin features a reward system, leaderboard integration, and fully configurable quiz types.

**Project Type:** Java-based Minecraft Paper plugin (backend JAR file)  
**Build System:** Maven 3.9.9  
**Java Version:** 21  
**Main Output:** `MinecraftPaperPlugin/target/QuizPlugin-1.0.0.jar` (44KB)

## Project Structure
```
MinecraftPaperPlugin/
├── src/main/java/com/quizplugin/     # Java source code
│   ├── commands/                      # Command handlers
│   ├── config/                        # Configuration managers
│   ├── listeners/                     # Event listeners
│   ├── managers/                      # Core managers
│   ├── models/                        # Data models
│   └── QuizPlugin.java               # Main plugin class
├── src/main/resources/
│   ├── quiz_types/                   # Quiz question definitions
│   ├── config.yml                    # Main configuration
│   ├── messages.yml                  # All text messages
│   ├── rewards.yml                   # Reward definitions
│   └── plugin.yml                    # Plugin metadata
├── pom.xml                           # Maven build configuration
├── build.sh                          # Build script
└── target/                           # Build output (gitignored)
    └── QuizPlugin-1.0.0.jar         # Final plugin JAR
```

## Key Features
- **LuckPerms Integration:** Automatic group detection (A1-A2, B1-B2, C1-C2)
- **Quiz System:** Choice (A-D) and freeform text questions
- **Difficulty Levels:** Easy, Medium, and Hard with custom rewards
- **Points & Leaderboard:** Track player progress with TAB plugin integration
- **Player Commands:** `/quiz optout`, `/quiz optin`, `/quiz points`, `/quiz scoreboard`
- **Admin Commands:** `/quiz broadcast`, `/quiz reload`, `/quiz setpoints`, `/quiz setgroup`, `/quiz give`
- **Debug Mode:** Optional detailed logging for troubleshooting quiz distribution
- **Manual Group Overrides:** Admins can manually assign quiz groups to players (overrides LuckPerms)
- **Fully Configurable:** All messages, rewards, and quiz types are customizable

## Dependencies
- **Paper API:** 1.21.3 (provided by server)
- **LuckPerms API:** 5.4+ (required)
- **TAB Plugin:** Optional for leaderboard placeholders
- **Java Runtime:** 21

## Building the Plugin

### Using the Workflow
The "Build Plugin" workflow is configured to automatically build the plugin. It runs:
```bash
cd MinecraftPaperPlugin && mvn clean package
```

### Manual Build
```bash
cd MinecraftPaperPlugin
mvn clean package
```

The compiled JAR will be at: `MinecraftPaperPlugin/target/QuizPlugin-1.0.0.jar`

## Installation on Minecraft Server
1. Build the plugin (see above)
2. Copy `target/QuizPlugin-1.0.0.jar` to your Minecraft server's `plugins/` folder
3. Install LuckPerms plugin (required dependency)
4. Optionally install TAB plugin for leaderboard integration
5. Restart your server
6. Configure the plugin files in `plugins/QuizPlugin/`
7. Create LuckPerms groups: A1-A2, B1-B2, C1-C2
8. Assign players to groups using LuckPerms commands

## Configuration Files
After first run, the plugin creates these files in `plugins/QuizPlugin/`:
- `config.yml` - Quiz intervals, group settings, time limits
- `messages.yml` - All user-facing text (supports MiniMessage format)
- `rewards.yml` - Difficulty-based rewards and points
- `quiz_types/*.yml` - Question definitions for each quiz type
- `players.yml` - Player points and statistics (auto-generated)
- `player_settings.yml` - Opt-out preferences (auto-generated)

## Recent Changes
- **2025-11-18:** Quiz distribution fixes and enhancements
  - Fixed automatic quiz distribution (scheduler now starts after 1 second instead of 5 minutes)
  - Fixed group filtering (unconfigured groups now receive all quiz types as fallback)
  - Added debug_mode configuration option for troubleshooting
  - Added comprehensive debug logging throughout distribution system
  - Created manual group override system (PlayerGroupManager)
  - Added /quiz setgroup command for admins to manually assign groups
  - Group overrides work for both online and offline players
  - Successfully built fixed QuizPlugin JAR (47KB)
- **2025-11-18:** Initial Replit setup completed
  - Installed Java 21 and Maven 3.9.9
  - Created build workflow
  - Added .gitignore for Java/Maven projects
  - Created project documentation

## Development Notes
- The plugin is production-ready and fully functional
- All Java code includes comprehensive JavaDoc comments
- Type-safe numeric parsing implemented
- Proper error handling with logging
- Graceful degradation for optional dependencies (TAB)

## Documentation
- `README.md` - Full feature guide and usage instructions
- `INSTALLATION.md` - Step-by-step setup guide
- `QUICK_START.md` - 5-minute quick start
- `PROJECT_SUMMARY.md` - Complete project overview
- `BUILD_COMPLETE.md` - Build status and deployment checklist

## User Preferences
None set yet.

## Technical Notes
This is a **backend-only** Minecraft plugin project:
- No web frontend or server to run
- Output is a JAR file for Minecraft Paper servers
- Build system: Maven with Shade plugin for dependency bundling
- The workflow builds the plugin on demand
