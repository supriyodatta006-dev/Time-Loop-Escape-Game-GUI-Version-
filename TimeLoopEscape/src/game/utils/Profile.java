package game.utils;

import game.core.Difficulty;
import java.util.HashMap;
import java.util.Map;

public class Profile {

    private String  name;
    private String  avatarId        = "avatar_01";
    private int     totalGamesPlayed;
    private int     totalEscapes;
    private int     bestScore;
    private long    totalPlaytimeMs;
    private boolean guest           = false;
    private String  createdAt;

    private final Map<String, Integer> bestScores    = new HashMap<>();
    private final Map<String, Boolean> completedDiff = new HashMap<>();

    public Profile(String name) {
        this.name      = name;
        this.createdAt = java.time.LocalDate.now().toString();
        for (Difficulty d : Difficulty.values()) {
            bestScores.put(d.saveKey, 0);
            completedDiff.put(d.saveKey, false);
        }
    }

    public void recordGame(int score, boolean escaped, Difficulty diff, long durationMs) {
        totalGamesPlayed++;
        totalPlaytimeMs += durationMs;
        if (escaped) {
            totalEscapes++;
            completedDiff.put(diff.saveKey, true);
        }
        if (score > bestScores.getOrDefault(diff.saveKey, 0))
            bestScores.put(diff.saveKey, score);
        if (score > bestScore)
            bestScore = score;
    }

    public int     getBestScoreFor(Difficulty d) { return bestScores.getOrDefault(d.saveKey, 0); }
    public boolean hasCompleted(Difficulty d)     { return completedDiff.getOrDefault(d.saveKey, false); }

    public String getFormattedPlaytime() {
        long secs  = totalPlaytimeMs / 1000;
        long mins  = secs / 60;
        long hours = mins / 60;
        return String.format("%dh %02dm", hours, mins % 60);
    }

    public double getEscapeRate() {
        if (totalGamesPlayed == 0) return 0;
        return (double) totalEscapes / totalGamesPlayed * 100.0;
    }

    public String  getName()                  { return name; }
    public void    setName(String n)          { name = n; }
    public String  getAvatarId()              { return avatarId; }
    public void    setAvatarId(String a)      { avatarId = a; }
    public int     getTotalGamesPlayed()      { return totalGamesPlayed; }
    public void    setTotalGamesPlayed(int v) { totalGamesPlayed = v; }
    public int     getTotalEscapes()          { return totalEscapes; }
    public void    setTotalEscapes(int v)     { totalEscapes = v; }
    public int     getBestScore()             { return bestScore; }
    public void    setBestScore(int v)        { bestScore = v; }
    public long    getTotalPlaytimeMs()       { return totalPlaytimeMs; }
    public void    setTotalPlaytimeMs(long v) { totalPlaytimeMs = v; }
    public boolean isGuest()                  { return guest; }
    public void    setGuest(boolean g)        { guest = g; }
    public String  getCreatedAt()             { return createdAt; }
    public void    setCreatedAt(String d)     { createdAt = d; }

    @Override
    public String toString() {
        return "Profile{name=" + name + ", guest=" + guest
               + ", games=" + totalGamesPlayed + ", best=" + bestScore + "}";
    }
}
