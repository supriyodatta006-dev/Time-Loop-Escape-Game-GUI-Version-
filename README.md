# 🕐 Time Loop Escape 
> *You wake up. Again. The clock resets. The rooms shift. But this time — you remember.*

This project is made by me as my semester final project

A feature-rich, cyberpunk-themed **puzzle escape room game** built entirely in **Java Swing / Java2D** as a course project for **CSE 124 – Object Oriented Programming** at USTC. The game challenges players to collect clues, interact with objects, and escape a series of rooms before the time loop resets — with progressively harder difficulty modes pushing the limits of your memory and speed.

---

## 🎮 Gameplay

You are trapped in a time loop. Each loop, rooms reset — but your **score penalty** stacks. Escape before time runs out by collecting enough clues and interacting with the environment. Fail too many loops and it's game over.

- **Collect clues** scattered across each room
- **Interact** with objects (puzzle panels, locked doors, bookshelves, clocks, mirrors, chests, computer terminals)
- **Escape the room** once you have enough clues — requirements scale with difficulty
- **10 loop maximum** — each reset costs you points and time
- Score bonuses for speed, loops remaining, and difficulty multiplier

---

## ✨ Features

### Core Gameplay
- Multi-room level progression with full reset/restore mechanics
- Item types: **Clues**, **Key Items**, **Tools**, and **Red Herrings**
- Interactable objects with item requirements and floating hint messages
- Phase-based state machine (`PLAYING`, `LOOP_RESETTING`, `ROOM_TRANSITION`, `WINNING`, `PAUSED`) for stable game flow
- Session logging for debugging and score tracking

### Difficulty Modes
| Mode | Time Limit | Clues Needed | Red Herrings | Penalty | Score Multiplier |
|------|-----------|--------------|--------------|---------|-----------------|
| Easy | 120s | 1 | 3 | 10s | 1.0× |
| Normal | 90s | 2 | 5 | 20s | 1.5× |
| Hard | 60s | 3 | 3 | 30s | 2.5× |
| Nightmare | 30s | 4 | 7 | 45s | 4.0× |

### User System
- **Login / Register** screen with SHA-256 password hashing
- **Guest mode** for quick play
- Up to **3 saved profiles** per installation with persistent stats
- Profile stats: games played, total escapes, best score, total playtime

### Animations & Visual Effects
- Particle system with burst effects on clue collection, loop resets, and escape
- Glitch effect on loop resets with RGB scanline distortion
- Ambient floating particles and pulsing glow on interactable objects
- Screen flash effects for feedback events
- Floating text labels (+CLUE!, LOOP RESET, ESCAPED!)

### Audio
- Per-screen background music tracks (main menu, gameplay, victory, game over)
- Sound effects for clicks, clue finds, loop resets, escape, door open, and more
- Volume controls: master, music, SFX — individually adjustable and mutable
- Settings persisted across sessions

### Screens
- Loading screen
- Login / Register
- Main Menu
- Difficulty Select
- Game Screen (main gameplay)
- Pause Menu
- Victory Screen
- Game Over Screen
- Profile Screen
- Settings Screen
- Premium Screen

---

## 🏗️ Architecture

The project follows an MVC-influenced, package-separated design:

```
src/
└── game/
    ├── Main.java                  # Entry point
    ├── animations/
    │   └── AnimationSystem.java   # Particles, flashes, glitch, float text
    ├── audio/
    │   └── AudioManager.java      # Music + SFX playback, volume control
    ├── core/
    │   ├── GameEngine.java        # Singleton engine, screen routing
    │   ├── GameSession.java       # Active game state, scoring, loop logic
    │   ├── GameState.java         # Enum: LOADING, LOGIN, PLAYING, VICTORY …
    │   └── Difficulty.java        # Enum with per-mode config and multipliers
    ├── entities/
    │   ├── Player.java            # Player state and inventory
    │   ├── Room.java              # Room data, items, interactables
    │   ├── Item.java              # Collectible items with glow/particle rendering
    │   └── Interactable.java      # Interactive objects with activation logic
    ├── levels/
    │   └── LevelFactory.java      # Procedural room and item generation
    ├── ui/
    │   ├── GameWindow.java        # Main JFrame host
    │   └── screens/               # One class per screen (GameScreen, MainMenuScreen …)
    └── utils/
        ├── AccountManager.java    # Login/register with SHA-256 hashing
        ├── ProfileManager.java    # Save/load up to 3 profiles (.timeloop/profiles.dat)
        ├── PremiumManager.java    # Premium feature gating
        ├── SettingsManager.java   # Persistent settings (.timeloop/settings.properties)
        └── Profile.java           # Profile data model
```

### Key Design Patterns
- **Singleton** — `GameEngine`, `AudioManager`, `ProfileManager`, `SettingsManager`, `AccountManager`
- **State Machine** — `GameEngine.GameState` enum drives all screen transitions
- **Factory** — `LevelFactory` generates rooms and items per difficulty
- **Observer-like callbacks** — `Interactable` uses `Consumer<Interactable>` for activation events
- **Entity-Component drawing** — `Item`, `Interactable`, `AnimationSystem` each own their `draw(Graphics2D)` logic

---

## 🚀 Getting Started

### Prerequisites
- Java 17 or later
- Any IDE (IntelliJ IDEA recommended) or command-line `javac`/`java`

### Build & Run

**From an IDE:**
1. Open the project root as a Java project
2. Add `src/` as the source root
3. Run `game.Main`

**From the command line:**
```bash
# Compile
javac -d out -sourcepath src src/game/Main.java

# Run
java -cp out game.Main
```

### Optional: Audio Setup
Place `.wav` audio files under `resources/sounds/` in the project root:
```
resources/
└── sounds/
    ├── music_menu.wav
    ├── music_gameplay.wav
    ├── music_victory.wav
    ├── music_gameover.wav
    ├── sfx_click.wav
    ├── sfx_clue.wav
    ├── sfx_loop_reset.wav
    ├── sfx_escape.wav
    ├── sfx_tick.wav
    ├── sfx_error.wav
    └── sfx_door.wav
```
The game runs fine without audio files — missing sounds are silently skipped.

### Save Data
Profile and settings data are stored in `~/.timeloop/` automatically:
```
~/.timeloop/
├── profiles.dat        # Saved player profiles
└── settings.properties # Volume, difficulty, display preferences
```

---

## 🛠️ Built With

| Technology | Usage |
|---|---|
| Java 17+ | Core language |
| Java Swing | Window, panels, layout |
| Java2D (Graphics2D) | Custom rendering, particles, animations |
| javax.sound.sampled | Audio playback |
| java.security (SHA-256) | Password hashing |
| java.util.Properties | Settings persistence |

---

## 📸 Screenshots

> *(Add screenshots of the main menu, gameplay screen, and victory screen here)*

---

## 📖 What I Learned

This project was built for **CSE 124 – Object Oriented Programming** and helped me apply:

- Singleton and Factory design patterns in a real application
- Enum-driven state machines for robust game flow
- Custom rendering pipelines with Java2D
- Multi-package project organisation in Java
- File I/O for save data and configuration persistence
- Password hashing and basic authentication flow
- Event-driven UI with Java Swing

---

## 📄 License

This project was developed as a university semester final lab project. Feel free to explore the code for learning purposes.

---

*Made with ☕ and way too many time loops.*
