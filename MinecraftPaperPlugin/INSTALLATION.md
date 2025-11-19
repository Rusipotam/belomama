# Installation Guide - QuizPlugin

This guide will walk you through installing and configuring QuizPlugin on your Minecraft Paper 1.21.8 server.

## Prerequisites

Before installing QuizPlugin, ensure you have:

1. **Minecraft Paper Server 1.21.3+** (compatible with 1.21.8)
2. **Java 21** installed on your server
3. **LuckPerms 5.4+** installed and configured
4. **(Optional)** TAB plugin for leaderboard integration

## Step 1: Build the Plugin

### On Linux/Mac:

```bash
chmod +x build.sh
./build.sh
```

### On Windows:

```cmd
build.bat
```

### Manual Build:

```bash
mvn clean package
```

The compiled JAR will be in `target/QuizPlugin-1.0.0.jar`

## Step 2: Install the Plugin

1. **Stop your server** if it's running
2. **Copy** `target/QuizPlugin-1.0.0.jar` to your server's `plugins/` folder
3. **Start your server**

The plugin will create its data folder at `plugins/QuizPlugin/` with all default configurations.

## Step 3: Configure LuckPerms Groups

QuizPlugin uses LuckPerms groups to determine which quizzes players receive. You need to set up the following groups:

### Create Groups:

```bash
# Create beginner group
/lp creategroup A1-A2

# Create intermediate group  
/lp creategroup B1-B2

# Create advanced group
/lp creategroup C1-C2
```

### Assign Groups to Players:

```bash
# Assign a player to beginner group
/lp user <player> parent set A1-A2

# Assign a player to intermediate group
/lp user <player> parent set B1-B2

# Assign a player to advanced group
/lp user <player> parent set C1-C2
```

### Set as Primary Group:

```bash
/lp user <player> parent settrack <group>
```

## Step 4: Configure Quiz Settings

Edit `plugins/QuizPlugin/config.yml`:

```yaml
# How often to send quizzes (in seconds)
quiz_interval_seconds: 300  # 5 minutes

# Which quiz types are active
enabled_quiz_types:
  - type1  # Grammar basics
  - type2  # Vocabulary
  - type3  # Minecraft knowledge
  - type4  # Advanced topics

# Configure which groups get which quiz types
group_settings:
  A1-A2:
    allowed_types: [type1, type2]
  B1-B2:
    allowed_types: [type1, type2, type3]
  C1-C2:
    allowed_types: [type1, type2, type3, type4]
```

## Step 5: Customize Quizzes (Optional)

### Edit Existing Quizzes:

Navigate to `plugins/QuizPlugin/quiz_types/` and edit:
- `type1.yml` - Grammar basics
- `type2.yml` - Vocabulary
- `type3.yml` - Minecraft knowledge
- `type4.yml` - Advanced topics

### Add New Questions:

```yaml
questions:
  - id: mynewquestion01
    question: "What is the capital of France?"
    type: freeform
    correct: "Paris"
    difficulty: easy
    tag: "Geography"
    resource: ""
```

### Create New Quiz Type:

1. Create a new file: `plugins/QuizPlugin/quiz_types/type5.yml`
2. Add questions following the format above
3. Add `type5` to `enabled_quiz_types` in `config.yml`
4. Add `type5` to appropriate groups in `group_settings`

## Step 6: Customize Rewards (Optional)

Edit `plugins/QuizPlugin/rewards.yml`:

```yaml
easy:
  material: IRON_INGOT
  name: "&a&lQuiz Reward &7(Easy)"
  lore:
    - "&7You answered correctly!"
  points: 1
```

The plugin includes 4 blank reward templates (`custom_reward_1` through `custom_reward_4`) ready for customization.

## Step 7: Customize Messages (Optional)

Edit `plugins/QuizPlugin/messages.yml` to change all text messages. Supports:
- `&` color codes: `&a` = green, `&c` = red, etc.
- MiniMessage format: `<gold>`, `<bold>`, etc.

## Step 8: Reload and Test

```bash
# Reload the plugin
/quiz reload

# Test by giving yourself a quiz
/quiz give <yourname> type1

# Answer with: !youranswer
```

## Step 9: TAB Integration (Optional)

If you have TAB plugin installed:

1. QuizPlugin will automatically detect it
2. Use `%quizpoints%` placeholder in TAB configs
3. Player points will display in tablist, nametags, etc.

Example TAB configuration:
```yaml
tablist-name: "%quizpoints% points | %player%"
```

## Troubleshooting

### "LuckPerms not found!" Error

**Solution**: Install LuckPerms from [https://luckperms.net/download](https://luckperms.net/download)

### Players Not Receiving Quizzes

**Possible causes**:
1. Player has opted out (`/quiz optin` to re-enable)
2. Player's LuckPerms group not in `config.yml`
3. Player's group doesn't allow any enabled quiz types
4. Quiz type files missing or invalid

**Check**:
```bash
/quiz give <player> type1
```

### "No questions found" Error

**Solution**: 
1. Check that quiz type files exist in `plugins/QuizPlugin/quiz_types/`
2. Verify YAML syntax is correct
3. Check server console for loading errors

### Build Errors

**Common issues**:
- Maven not installed: Install from [maven.apache.org](https://maven.apache.org/download.cgi)
- Wrong Java version: QuizPlugin requires Java 21
- Network issues: Maven needs internet to download dependencies

## File Locations

After installation, you'll have:

```
plugins/QuizPlugin/
├── config.yml              # Main settings
├── messages.yml            # All text messages
├── rewards.yml             # Reward definitions
├── players.yml             # Player points (auto-generated)
├── player_settings.yml     # Opt-out settings (auto-generated)
└── quiz_types/
    ├── type1.yml
    ├── type2.yml
    ├── type3.yml
    └── type4.yml
```

## Permissions Setup

Grant permissions to players/groups:

```bash
# Allow players to opt out
/lp group default permission set quizplugin.optout true

# Allow players to view points and scoreboard
/lp group default permission set quizplugin.points true
/lp group default permission set quizplugin.scoreboard true

# Give admins full access
/lp group admin permission set quizplugin.admin true
```

## Next Steps

1. ✅ Create and configure LuckPerms groups
2. ✅ Customize quiz questions for your needs
3. ✅ Adjust rewards in `rewards.yml`
4. ✅ Customize messages for your server's style
5. ✅ Test with `/quiz give` command
6. ✅ Monitor server console for any errors
7. ✅ Adjust `quiz_interval_seconds` as needed

## Support

- Check server console logs for errors
- Review code comments in Java files for understanding
- All code is extensively documented for easy customization
- Refer to README.md for detailed feature information
