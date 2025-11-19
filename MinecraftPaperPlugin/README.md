# QuizPlugin - Minecraft Paper 1.21.8

A comprehensive quiz plugin for Paper 1.21.8 that integrates with LuckPerms to send educational quizzes to players based on their permission groups.

## Features

✅ **LuckPerms Integration** - Automatic group detection (A1-A2, B1-B2, C1-C2)  
✅ **Flexible Quiz System** - Supports choice (A-D) and freeform text answers  
✅ **Difficulty-Based Rewards** - Easy, Medium, and Hard rewards with custom items and lore  
✅ **Points & Leaderboard** - Track player progress with optional TAB plugin integration  
✅ **Fully Configurable** - All messages, rewards, and quiz types are customizable  
✅ **Player Opt-Out** - Players can disable quizzes if they prefer  
✅ **Admin Commands** - Broadcast, reload, manage points, and manually send quizzes  
✅ **Comprehensive Logging** - All code is fully commented for easy customization

## Requirements

- **Minecraft Server**: Paper 1.21.3+ (tested on 1.21.8)
- **Java**: 21
- **Required Dependency**: LuckPerms 5.4+
- **Optional Dependency**: TAB plugin (for leaderboard placeholders)

## Building the Plugin

### Using Maven

```bash
# Clone or download this project
cd QuizPlugin

# Build with Maven
mvn clean package

# The compiled JAR will be in target/QuizPlugin-1.0.0.jar
```

### Manual Build

If you don't have Maven installed, you can download it from [maven.apache.org](https://maven.apache.org/download.cgi).

## Installation

1. **Build the plugin** (see above)
2. **Copy JAR** to your server's `plugins/` folder
3. **Install LuckPerms** if not already installed
4. **(Optional)** Install TAB plugin for leaderboard integration
5. **Start/restart** your server
6. **Configure** the plugin (see Configuration section)

## Configuration

### Main Config (`config.yml`)

```yaml
quiz_interval_seconds: 300  # Send quiz every 5 minutes
enabled_quiz_types:         # Active quiz types
  - type1
  - type2
  - type3
  - type4

group_settings:             # Group-based access control
  A1-A2:                    # Beginner group
    allowed_types: [type1, type2]
  B1-B2:                    # Intermediate group
    allowed_types: [type1, type2, type3]
  C1-C2:                    # Advanced group
    allowed_types: [type1, type2, type3, type4]

time_limits:                # Default time limits per difficulty
  easy: 20
  medium: 30
  hard: 45

attempts:                   # Default max attempts per difficulty
  easy: 2
  medium: 2
  hard: 3
```

### Quiz Types (`quiz_types/*.yml`)

Create custom quiz types by adding YAML files in `plugins/QuizPlugin/quiz_types/`:

```yaml
questions:
  - id: q01
    question: "Your question here?"
    type: choice              # "choice" or "freeform"
    options:                  # Only for choice type
      - "Option A"
      - "Option B"
      - "Option C"
      - "Option D"
    correct: "A"              # A-D for choice, exact text for freeform
    difficulty: easy          # easy, medium, or hard
    tag: "Category"           # Optional category tag
    resource: "https://..."   # Optional learning resource
    time: 20                  # Optional time override
    attempts: 2               # Optional attempts override
```

### Rewards (`rewards.yml`)

Customize rewards for each difficulty level. The plugin includes 4 blank reward templates (`custom_reward_1` through `custom_reward_4`) for your use.

### Messages (`messages.yml`)

All user-facing text is configurable. Supports MiniMessage formatting and `&` color codes.

## Commands

### Player Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/quiz optout` | Disable quizzes | `quizplugin.optout` |
| `/quiz optin` | Enable quizzes | `quizplugin.optin` |
| `/quiz points` | View your points | `quizplugin.points` |
| `/quiz scoreboard` | View leaderboard | `quizplugin.scoreboard` |

### Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/quiz broadcast <message>` | Broadcast message to all players | `quizplugin.broadcast` |
| `/quiz reload` | Reload all configurations | `quizplugin.reload` |
| `/quiz setpoints <player> <points>` | Set player points | `quizplugin.setpoints` |
| `/quiz give <player> <type>` | Send quiz to player | `quizplugin.give` |

## How It Works

### Quiz Distribution

1. Every X seconds (configured in `config.yml`), the scheduler picks a random enabled quiz type
2. Finds online players whose LuckPerms group allows that quiz type
3. Filters out players who have active quizzes or opted out
4. Sends the quiz to one random eligible player

### Answering Quizzes

Players answer by typing in chat:
```
!youranswer
```

- Messages without `!` are ignored (normal chat continues)
- Choice questions: Answer with A, B, C, or D
- Freeform questions: Type the exact answer (case-insensitive)

### Rewards

- **Correct answer**: Player receives reward item with custom lore (date + question ID) and earns points
- **Wrong answer**: Attempts decrease, player can try again
- **Out of attempts**: Shows correct answer and optional resource link

## TAB Integration

If TAB plugin is installed, the plugin provides a `%quizpoints%` placeholder that displays player points in:
- Tablist
- Nametags
- Scoreboard
- Any TAB feature that supports placeholders

If TAB is not installed, use `/quiz scoreboard` as a fallback.

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `quizplugin.use` | Base permission for commands | `true` |
| `quizplugin.optout` | Use /quiz optout | `true` |
| `quizplugin.optin` | Use /quiz optin | `true` |
| `quizplugin.points` | Use /quiz points | `true` |
| `quizplugin.scoreboard` | Use /quiz scoreboard | `true` |
| `quizplugin.admin` | All admin commands | `op` |
| `quizplugin.broadcast` | Use /quiz broadcast | `op` |
| `quizplugin.reload` | Use /quiz reload | `op` |
| `quizplugin.setpoints` | Use /quiz setpoints | `op` |
| `quizplugin.give` | Use /quiz give | `op` |

## File Structure

```
plugins/QuizPlugin/
├── config.yml              # Main configuration
├── messages.yml            # All text messages
├── rewards.yml             # Reward definitions (includes blank templates)
├── players.yml             # Player points and statistics
├── player_settings.yml     # Opt-out preferences
└── quiz_types/
    ├── type1.yml           # Grammar basics
    ├── type2.yml           # Vocabulary
    ├── type3.yml           # Minecraft knowledge
    └── type4.yml           # Advanced topics
```

## Extending the Plugin

### Adding New Quiz Types

1. Create a new YAML file in `quiz_types/` (e.g., `type5.yml`)
2. Add questions following the format in existing files
3. Add the type name to `enabled_quiz_types` in `config.yml`
4. Add the type to appropriate groups in `group_settings`

### Customizing Rewards

Edit `rewards.yml` to change reward items, names, lore, and points. Use the 4 blank reward templates (`custom_reward_1` through `custom_reward_4`) for special occasions or custom difficulty levels.

### Modifying Messages

All text is in `messages.yml`. Supports:
- `&` color codes (`&a`, `&c`, etc.)
- MiniMessage format (`<gold>`, `<bold>`, etc.)
- Placeholders (`%answer%`, `%link%`, etc.)

## Code Structure

All code is extensively commented to explain:
- What each class does
- Why it exists
- Where it sends data
- How it interacts with config files

Key classes:
- `QuizPlugin.java` - Main plugin initialization
- `QuizManager.java` - Loads quiz types from YAML
- `QuizSessionManager.java` - Manages active quizzes
- `GroupManager.java` - LuckPerms integration
- `RewardHandler.java` - Gives rewards and points
- `ChatAnswerListener.java` - Handles player answers
- `QuizCommand.java` - All /quiz commands

## Support

For issues or questions about the plugin code, review the extensive comments in each Java file. The code is designed to be self-documenting and easy to modify.

## License

This plugin is provided as-is for use on Minecraft Paper servers.
