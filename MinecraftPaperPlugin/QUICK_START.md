# QuizPlugin - Quick Start Guide

Get your quiz plugin up and running in 5 minutes!

## ⚡ Fast Setup

### 1. Build the Plugin (1 minute)

Choose your operating system:

**Windows:**
```cmd
build.bat
```

**Linux/Mac:**
```bash
chmod +x build.sh
./build.sh
```

**Result:** `target/QuizPlugin-1.0.0.jar` ✅

### 2. Install on Server (1 minute)

```bash
# Copy the JAR to your server
cp target/QuizPlugin-1.0.0.jar /path/to/your/server/plugins/

# Make sure LuckPerms is installed
# Download from: https://luckperms.net/download

# Start your server
```

### 3. Create LuckPerms Groups (2 minutes)

```bash
# Connect to your server console and run:

/lp creategroup A1-A2
/lp creategroup B1-B2
/lp creategroup C1-C2

# Assign yourself to a group for testing
/lp user YourUsername parent set C1-C2
```

### 4. Test It! (1 minute)

```bash
# Give yourself a quiz
/quiz give YourUsername type1

# Answer it by typing in chat:
!B

# Check your points
/quiz points

# View scoreboard
/quiz scoreboard
```

## 🎯 That's It!

Your quiz plugin is now running. Players will automatically receive quizzes every 5 minutes based on their LuckPerms group.

## 📝 Common Tasks

### Give a Quiz Manually
```
/quiz give <player> type1
```

### Broadcast a Message
```
/quiz broadcast &aGood job everyone!
```

### Reload Configuration
```
/quiz reload
```

### Check Top Players
```
/quiz scoreboard
```

### Let Players Opt Out
```
/quiz optout    (disable quizzes)
/quiz optin     (enable quizzes)
```

## 🎨 Customization

### Change Quiz Interval
Edit `plugins/QuizPlugin/config.yml`:
```yaml
quiz_interval_seconds: 300  # Change to desired seconds
```

### Add Your Own Questions
Edit any quiz type file in `plugins/QuizPlugin/quiz_types/`:
```yaml
questions:
  - id: myquestion
    question: "Your question here?"
    type: freeform
    correct: "answer"
    difficulty: easy
```

### Customize Rewards
Edit `plugins/QuizPlugin/rewards.yml`:
```yaml
easy:
  material: DIAMOND
  name: "&b&lAwesome Reward!"
  points: 10
```

### Change Messages
Edit `plugins/QuizPlugin/messages.yml`:
```yaml
correct_answer: "<green>Amazing! You got it right!</green>"
```

## 🔧 Troubleshooting

### "LuckPerms not found!"
➜ Install LuckPerms: https://luckperms.net/download

### Players not getting quizzes
➜ Check they're in a group: `/lp user <player> info`
➜ Make sure they haven't opted out: `/quiz optin`

### Build failed
➜ Install Maven: https://maven.apache.org/download.cgi
➜ Make sure you have Java 21

## 📚 Documentation

- **Full Guide**: `README.md`
- **Installation**: `INSTALLATION.md`
- **Project Overview**: `PROJECT_SUMMARY.md`

## 🎮 Answer Format

Players answer quizzes by typing in chat:

**For choice questions (A-D):**
```
!A
!B
!C
!D
```

**For freeform questions:**
```
!Paris
!went
!12
```

Case doesn't matter - `!paris` and `!Paris` are both correct!

## 🏆 Features

✅ Automatic quiz distribution  
✅ Group-based difficulty  
✅ Points and leaderboard  
✅ Custom rewards  
✅ Opt-out system  
✅ TAB plugin support  
✅ Fully configurable  
✅ Admin commands  

## ❓ Need Help?

1. Check server console for errors
2. Review code comments (all classes are documented)
3. Read full documentation in README.md
4. Check INSTALLATION.md for detailed setup

---

**Happy quizzing! 🎓**
