package game.core;

import java.awt.Color;

public enum Difficulty {

    EASY(
        "EASY",
        "For those just waking up…",
        180,
        4,
        1,
        10,
        new Color(0x4CAF50),
        "easy"
    ),

    NORMAL(
        "NORMAL",
        "The loop remembers everything.",
        120,
        2,
        3,
        20,
        new Color(0xFFB300),
        "normal"
    ),

    HARD(
        "HARD",
        "Time is your enemy.",
        60,
        1,
        5,
        30,
        new Color(0xF44336),
        "hard"
    ),

    NIGHTMARE(
        "NIGHTMARE",
        "You were never meant to escape.",
        30,
        0,
        7,
        45,
        new Color(0x9C27B0),
        "nightmare"
    );

    public final String label;
    public final String tagline;
    public final int    timeLimitSeconds;
    public final int    startingVisibleClues;
    public final int    redHerringCount;
    public final int    penaltySeconds;
    public final Color  accentColor;
    public final String saveKey;

    Difficulty(String label, String tagline, int timeLimitSeconds,
               int startingVisibleClues, int redHerringCount,
               int penaltySeconds, Color accentColor, String saveKey) {
        this.label                = label;
        this.tagline              = tagline;
        this.timeLimitSeconds     = timeLimitSeconds;
        this.startingVisibleClues = startingVisibleClues;
        this.redHerringCount      = redHerringCount;
        this.penaltySeconds       = penaltySeconds;
        this.accentColor          = accentColor;
        this.saveKey              = saveKey;
    }

    public double getScoreMultiplier() {
        return switch (this) {
            case EASY      -> 1.0;
            case NORMAL    -> 1.5;
            case HARD      -> 2.5;
            case NIGHTMARE -> 4.0;
        };
    }
}
