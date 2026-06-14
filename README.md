Here's a professional README.md file for your GitHub repository:


# 🕐 Time Loop Escape

<p align="center">
  <img src="screenshots/main-menu.png" alt="Time Loop Escape Main Menu" width="600"/>
</p>

<p align="center">
  <b>A 2D Puzzle Adventure Game Built Entirely in Java</b><br>
  <i>Escape the loop or relive it forever.</i>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-JDK%2017%2B-orange?style=for-the-badge&logo=openjdk" alt="Java 17+"/>
  <img src="https://img.shields.io/badge/Swing-GUI%20Framework-blue?style=for-the-badge" alt="Java Swing"/>
  <img src="https://img.shields.io/badge/Lines%20of%20Code-~6500-green?style=for-the-badge" alt="LOC"/>
  <img src="https://img.shields.io/badge/Classes-26-purple?style=for-the-badge" alt="26 Classes"/>
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge" alt="MIT License"/>
</p>

---

## 📖 About

**Time Loop Escape** is a fully functional 2D puzzle adventure game developed in Java using Java Swing — with **zero external dependencies** or game engines. Navigate through 7 themed rooms, collect clues, solve puzzles, and escape before the time loop resets!

Built as a semester final project for **Object-Oriented Programming (OOP)**, this game demonstrates all four OOP pillars (Encapsulation, Inheritance, Polymorphism, Abstraction) along with professional design patterns in a playable, polished game.

---

## 🎮 Gameplay

<p align="center">
  <img src="screenshots/gameplay.png" alt="Gameplay Screenshot" width="600"/>
</p>

You are trapped in a mysterious facility caught in a **time loop**. Each loop gives you limited time to explore rooms, collect clues, and solve puzzles. If the timer runs out — the loop resets and your sanity drops. Run out of all 10 loops, and the loop consumes you forever.

### Core Mechanics
- ⏱️ **Time Loop** — Limited countdown timer; resets send you back to Room 1
- 🧠 **Sanity System** — Degrades on each reset, causing visual distortion and reduced visibility
- 🔑 **Puzzle Solving** — Find key items and interact with objects to unlock exits
- 🏃 **Sprint** — Move faster at the cost of draining sanity
- 🎯 **Score Multipliers** — Higher difficulty = bigger risk, bigger rewards

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🏰 **7 Themed Rooms** | Library, Clocktower, Lab, Crypt, Observatory, Submarine, Nexus |
| 🎚️ **4 Difficulty Levels** | Easy (180s) → Normal (120s) → Hard (60s) → Nightmare (30s) |
| 🔐 **User Authentication** | SHA-256 password hashing with random salt via SecureRandom |
| 👤 **3 Profile Slots** | Persistent stats tracking (games, escapes, best scores, playtime) |
| 🎵 **Full Audio System** | 4 music tracks + 7 sound effects with independent volume controls |
| ✨ **Particle Effects** | Physics-based particles, screen flashes, glitch effects |
| 💎 **Premium System** | Unlock Hard/Nightmare modes with licence key activation |
| 🖥️ **9 UI Screens** | Login, Menu, Difficulty, Gameplay, Victory, GameOver, Profile, Settings, Premium |
| 🎮 **60 FPS Game Loop** | Smooth animations with delta-time physics |
| 💾 **Persistent Save Data** | Settings, accounts, profiles saved to `~/.timeloop/` |

---

## 🖼️ Screenshots

<table>
  <tr>
    <td><img src="screenshots/login.png" alt="Login Screen" width="350"/></td>
    <td><img src="screenshots/main-menu.png" alt="Main Menu" width="350"/></td>
  </tr>
  <tr>
    <td align="center"><i>Login & Registration</i></td>
    <td align="center"><i>Main Menu</i></td>
  </tr>
  <tr>
    <td><img src="screenshots/difficulty.png" alt="Difficulty Selection" width="350"/></td>
    <td><img src="screenshots/gameplay.png" alt="Gameplay" width="350"/></td>
  </tr>
  <tr>
    <td align="center"><i>Difficulty Selection</i></td>
    <td align="center"><i>Gameplay - The Forgotten Library</i></td>
  </tr>
  <tr>
    <td><img src="screenshots/victory.png" alt="Victory Screen" width="350"/></td>
    <td><img src="screenshots/gameover.png" alt="Game Over Screen" width="350"/></td>
  </tr>
  <tr>
    <td align="center"><i>Victory - Escaped the Loop!</i></td>
    <td align="center"><i>Game Over - Consumed by the Loop</i></td>
  </tr>
  <tr>
    <td><img src="screenshots/settings.png" alt="Settings" width="350"/></td>
    <td><img src="screenshots/profile.png" alt="Profile" width="350"/></td>
  </tr>
  <tr>
    <td align="center"><i>Settings & Audio Controls</i></td>
    <td align="center"><i>Player Profiles & Stats</i></td>
  </tr>
</table>

---

## 🏗️ Architecture

### Tech Stack
- **Language:** Java (JDK 17+)
- **GUI Framework:** Java Swing (no external game engine)
- **Audio:** `javax.sound.sampled`
- **Security:** `java.security.MessageDigest` (SHA-256) + `java.security.SecureRandom`
- **Build:** Pure `javac` compilation (no Maven/Gradle required)

### Project Structure


TimeLoopEscape/
├── src/
│   └── game/
│       ├── Main.java                    # Entry point
│       ├── core/                        # Game engine & logic
│       │   ├── GameEngine.java          # Singleton state machine (11 states)
│       │   ├── GameSession.java         # Active playthrough manager
│       │   └── Difficulty.java          # 4-level difficulty enum
│       ├── entities/                    # Game world objects
│       │   ├── Player.java             # Movement, sanity, inventory
│       │   ├── Room.java               # Tile grid, items, themes
│       │   ├── Item.java               # Collectibles (4 types)
│       │   └── Interactable.java       # Puzzle objects (7 types)
│       ├── levels/                      # Level generation
│       │   └── LevelFactory.java       # Factory pattern - 7 rooms
│       ├── animations/                  # Visual effects
│       │   └── AnimationSystem.java    # Particles, flashes, glitch
│       ├── audio/                       # Sound management
│       │   └── AudioManager.java       # Singleton - music & SFX
│       ├── ui/                          # Base UI framework
│       │   ├── BaseScreen.java         # Abstract base (9 subclasses)
│       │   ├── GameWindow.java         # JFrame with CardLayout
│       │   └── screens/                # All game screens
│       │       ├── LoadingScreen.java
│       │       ├── LoginScreen.java
│       │       ├── MainMenuScreen.java
│       │       ├── DifficultyScreen.java
│       │       ├── GameScreen.java
│       │       ├── VictoryScreen.java
│       │       ├── GameOverScreen.java
│       │       ├── ProfileScreen.java
│       │       ├── SettingsScreen.java
│       │       └── PremiumScreen.java
│       └── utils/                       # Utilities & persistence
│           ├── AccountManager.java     # SHA-256 auth system
│           ├── ProfileManager.java     # Player profile CRUD
│           ├── SettingsManager.java    # Persistent settings
│           ├── PremiumManager.java     # Feature gating
│           └── Profile.java            # Profile data model
└── resources/
└── sounds/                          # Audio files (optional)


### Design Patterns

| Pattern | Implementation | Purpose |
|---------|---------------|---------|
| **Singleton** | GameEngine, AudioManager, AccountManager, ProfileManager, SettingsManager, PremiumManager | Single instance management for global state |
| **Factory** | LevelFactory | Generates 7 rooms with items, interactables, and themes |
| **State Machine** | GameEngine (11 GameStates) | Controls screen navigation and game flow |

### OOP Principles

| Principle | Example |
|-----------|---------|
| **Encapsulation** | `Player.reduceSanity()` enforces `sanity ≥ 0`; private fields with controlled accessors |
| **Inheritance** | Abstract `BaseScreen` → 9 concrete screen subclasses with shared rendering |
| **Polymorphism** | `drawScreen()`, `keyPressed()`, `onResize()` overridden contextually per screen |
| **Abstraction** | Abstract `init()` method; `Consumer<Interactable>` functional interfaces for per-object behavior |

---

## 🚀 Getting Started

### Prerequisites

- **Java JDK 17** or higher ([Download from Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/))

Verify your installation:
```bash
java -version    # Should show 17.x.x or higher
javac -version   # Should show 17.x.x or higher

Installation & Run

# Clone the repository
git clone https://github.com/yourusername/TimeLoopEscape.git
cd TimeLoopEscape

# Compile all source files
mkdir -p out
find src -name "*.java" | xargs javac -d out -sourcepath src

# Run the game
java -cp out game.Main

Windows (Command Prompt)

git clone https://github.com/yourusername/TimeLoopEscape.git
cd TimeLoopEscape
mkdir out
javac -d out -sourcepath src src/game/Main.java
java -cp out game.Main

Using an IDE

IDEStepsIntelliJ IDEAOpen folder → Mark src as Sources Root → Set SDK to JDK 17 → Run Main.javaEclipseImport as Java Project → Set JRE to JavaSE-17 → Run As → Java ApplicationVS CodeInstall Java Extension Pack → Open folder → Press F5 on Main.java

IDESteps

IDE

Steps

IntelliJ IDEAOpen folder → Mark src as Sources Root → Set SDK to JDK 17 → Run Main.java

IntelliJ IDEA

Open folder → Mark src as Sources Root → Set SDK to JDK 17 → Run Main.java

EclipseImport as Java Project → Set JRE to JavaSE-17 → Run As → Java Application

Eclipse

Import as Java Project → Set JRE to JavaSE-17 → Run As → Java Application

VS CodeInstall Java Extension Pack → Open folder → Press F5 on Main.java

VS Code

Install Java Extension Pack → Open folder → Press F5 on Main.java

🎮 Controls

KeyActionW A S DMove Up / Left / Down / RightShift + MoveSprint (drains sanity)EInteract with adjacent objectsESCPause / ResumeM (paused)Return to Main MenuMouse ClickNavigate menus & UI

KeyAction

Key

Action

W A S DMove Up / Left / Down / Right

W A S D

Move Up / Left / Down / Right

Shift + MoveSprint (drains sanity)

Shift + Move

Sprint (drains sanity)

EInteract with adjacent objects

E

Interact with adjacent objects

ESCPause / Resume

ESC

Pause / Resume

M (paused)Return to Main Menu

M (paused)

Return to Main Menu

Mouse ClickNavigate menus & UI

Mouse Click

Navigate menus & UI

🗺️ Game Rooms

#RoomTheme ColorKey Puzzle1The Forgotten Library🔵 CyanPuzzle Panel + Torn Page2The Clocktower Chamber🟡 AmberGrand Clock + Gear Cog3Temporal Laboratory🟣 PurpleOMEGA Terminal + Access Card4The Ancient Crypt🟠 OrangeBlood Altar + Skull Key5The Celestial Observatory🔵 Deep CyanTemporal Orrery + Crystal Lens6Submarine Control Room🟢 GreenSurface Override + Emergency Key7The Temporal Nexus🟣 MagentaNexus Core + Omega Shard

#RoomTheme ColorKey Puzzle

#

Room

Theme Color

Key Puzzle

1The Forgotten Library🔵 CyanPuzzle Panel + Torn Page

1

The Forgotten Library

🔵 Cyan

Puzzle Panel + Torn Page

2The Clocktower Chamber🟡 AmberGrand Clock + Gear Cog

2

The Clocktower Chamber

🟡 Amber

Grand Clock + Gear Cog

3Temporal Laboratory🟣 PurpleOMEGA Terminal + Access Card

3

Temporal Laboratory

🟣 Purple

OMEGA Terminal + Access Card

4The Ancient Crypt🟠 OrangeBlood Altar + Skull Key

4

The Ancient Crypt

🟠 Orange

Blood Altar + Skull Key

5The Celestial Observatory🔵 Deep CyanTemporal Orrery + Crystal Lens

5

The Celestial Observatory

🔵 Deep Cyan

Temporal Orrery + Crystal Lens

6Submarine Control Room🟢 GreenSurface Override + Emergency Key

6

Submarine Control Room

🟢 Green

Surface Override + Emergency Key

7The Temporal Nexus🟣 MagentaNexus Core + Omega Shard

7

The Temporal Nexus

🟣 Magenta

Nexus Core + Omega Shard

🔐 Security

The authentication system implements industry-standard practices:

- SHA-256 Hashing — Passwords are never stored in plain text

- Per-User Random Salt — 128-bit salt via SecureRandom prevents rainbow table attacks

- Input Validation — Username (3+ chars, alphanumeric) and password (4+ chars) enforcement

- Case-Insensitive Login — "Supriyo", "SUPRIYO", "supriyo" all match the same account

💾 Save Data

All persistent data is stored in ~/.timeloop/:

FilePurposeaccounts.datUser accounts (username, hash, salt)profiles.datPlayer stats and best scoressettings.propertiesAudio volumes, display preferencespremium.propertiesPremium activation status

FilePurpose

File

Purpose

accounts.datUser accounts (username, hash, salt)

accounts.dat

User accounts (username, hash, salt)

profiles.datPlayer stats and best scores

profiles.dat

Player stats and best scores

settings.propertiesAudio volumes, display preferences

settings.properties

Audio volumes, display preferences

premium.propertiesPremium activation status

premium.properties

Premium activation status

🧪 Testing

All 15 test cases passed across 4 testing categories:

- ✅ Unit Testing — Sanity clamping, item collection, score calculation

- ✅ Integration Testing — State machine transitions, login → gameplay flow

- ✅ System Testing — All 4 difficulties, victory/game-over paths

- ✅ User Acceptance Testing — 3 independent testers confirmed usability

📊 Project Stats

MetricValueTotal Classes26Total Packages9Lines of Code~6,500Design Patterns3 (Singleton, Factory, State Machine)Game Screens9Game Rooms7Music Tracks4Sound Effects7Difficulty Levels4Test Cases15 (all passing)

MetricValue

Metric

Value

Total Classes26

Total Classes

26

Total Packages9

Total Packages

9

Lines of Code~6,500

Lines of Code

~6,500

Design Patterns3 (Singleton, Factory, State Machine)

Design Patterns

3 (Singleton, Factory, State Machine)

Game Screens9

Game Screens

9

Game Rooms7

Game Rooms

7

Music Tracks4

Music Tracks

4

Sound Effects7

Sound Effects

7

Difficulty Levels4

Difficulty Levels

4

Test Cases15 (all passing)

Test Cases

15 (all passing)

🔮 Future Enhancements

- Procedural room generation for infinite replayability

- Online leaderboard via REST API

- Multiplayer co-op mode

- Save/Load mid-session

- Additional room themes (12+ rooms)

- Accessibility features (colorblind mode, screen reader support)

- Mobile port using LibGDX

🛠️ Troubleshooting

ProblemSolutionjavac: command not foundInstall JDK 17+ and add to PATHUnsupportedClassVersionErrorUpdate Java runtime to JDK 17+No audio playingAudio files in resources/sounds/ are optional; game runs silently without themBlank/black screen on launchResize the window to trigger repaintSettings not savingCheck write permissions to ~/.timeloop/ directory

ProblemSolution

Problem

Solution

javac: command not foundInstall JDK 17+ and add to PATH

javac: command not found

Install JDK 17+ and add to PATH

UnsupportedClassVersionErrorUpdate Java runtime to JDK 17+

UnsupportedClassVersionError

Update Java runtime to JDK 17+

No audio playingAudio files in resources/sounds/ are optional; game runs silently without them

No audio playing

Audio files in resources/sounds/ are optional; game runs silently without them

Blank/black screen on launchResize the window to trigger repaint

Blank/black screen on launch

Resize the window to trigger repaint

Settings not savingCheck write permissions to ~/.timeloop/ directory

Settings not saving

Check write permissions to ~/.timeloop/ directory

👤 Author

Supriyo

- University OOP Semester Final Project

- June 2025

📄 License

This project is licensed under the MIT License — see the LICENSE file for details.

<p align="center">
  <i>⏱️ Escape the loop... or relive it forever. ⏱️</i>
</p>


Here's your complete GitHub README file! It includes:

- **Eye-catching header** with badges (Java version, LOC, classes, license)
- **About & Gameplay** section explaining the core concept
- **Features table** highlighting all major features
- **Screenshots gallery** (you'll need to add your actual image files to a `screenshots/` folder)
- **Architecture section** with full project structure, design patterns, and OOP principles
- **Getting Started** with installation instructions for terminal and all major IDEs
- **Controls, Rooms, Security, Testing** sections
- **Future Enhancements** as a checklist
- **Troubleshooting** for common issues

To use it, just:
1. Save this as `README.md` in your project root
2. Create a `screenshots/` folder and add your game images with matching filenames
3. Replace `yourusername` in the clone URL with your actual GitHub username
