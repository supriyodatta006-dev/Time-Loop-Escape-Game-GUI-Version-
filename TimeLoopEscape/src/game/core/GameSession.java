package game.core;

import game.entities.Item;
import game.entities.Player;
import game.entities.Room;
import game.levels.LevelFactory;
import game.ui.GameWindow;
import game.ui.screens.GameScreen;
import game.utils.Profile;

import java.util.ArrayList;
import java.util.List;

public class GameSession {

    public static final int MAX_LOOPS = 3;

    private final Profile    profile;
    private final Difficulty difficulty;
    private final Player     player;
    private final List<Room> rooms;

    private int     loopCount      = 0;
    private int     score          = 0;
    private boolean escaped        = false;
    private long    startTimeMs;
    private long    totalElapsedMs = 0;

    private final List<String> roomClueIds = new ArrayList<>();
    private final List<String> roomItemIds = new ArrayList<>();

    private final List<String> sessionLog  = new ArrayList<>();

    public GameSession(Profile profile, Difficulty difficulty) {
        this.profile     = profile;
        this.difficulty  = difficulty;
        this.player      = new Player(profile.getName());
        this.rooms       = LevelFactory.generateRooms(difficulty);
        this.startTimeMs = System.currentTimeMillis();
        log("Session started  difficulty=" + difficulty.label);
    }

    public int getRemainingSeconds() {
        long elapsed = (System.currentTimeMillis() - startTimeMs) / 1000L;
        return (int) Math.max(0, difficulty.timeLimitSeconds - elapsed);
    }

    private void restartTimer() {
        startTimeMs = System.currentTimeMillis();
    }

    public void triggerLoopReset() {
        loopCount++;
        score = Math.max(0, score - difficulty.penaltySeconds * 10);
        restartTimer();
        roomClueIds.clear();
        roomItemIds.clear();
        player.fullReset();
        rooms.forEach(Room::reset);
        log("Loop reset  loop=" + loopCount);
    }

    public boolean isGameOver() { return loopCount >= MAX_LOOPS; }

    public void onRoomAdvance() {
        restartTimer();
        roomClueIds.clear();
        roomItemIds.clear();

        log("Room advanced  timer+clues reset");
    }

    public void collectItem(Item item) {
        String id = item.getId();
        if (roomItemIds.contains(id)) return;
        roomItemIds.add(id);

        switch (item.getType()) {
            case CLUE -> {
                roomClueIds.add(id);
                addScore(50);
                log("Clue collected: " + id + "  count=" + roomClueIds.size());
            }
            case KEY_ITEM, TOOL -> {
                addScore(20);
                log("Key item collected: " + id);
            }
            default -> log("Herring (ignored): " + id);
        }
    }

    public boolean hasEnoughClues() {
        int needed = switch (difficulty) {
            case EASY      -> 1;
            case NORMAL    -> 2;
            case HARD      -> 3;
            case NIGHTMARE -> 4;
        };
        return roomClueIds.size() >= needed;
    }

    public int getClueCount() { return roomClueIds.size(); }

    public void addScore(int pts) {
        score += (int)(pts * difficulty.getScoreMultiplier());
    }

    public void markEscaped() {
        escaped        = true;
        totalElapsedMs = System.currentTimeMillis() - startTimeMs;
        addScore((int) Math.max(0, 1000 - totalElapsedMs / 1000));
        addScore((MAX_LOOPS - loopCount) * 200);
        log("ESCAPED  score=" + score);
    }

    public GameScreen buildGameScreen(GameWindow w) { return new GameScreen(w, this); }

    private void log(String msg) {
        String s = "[Session] " + msg;
        sessionLog.add(s);
        System.out.println(s);
    }

    public Profile     getProfile()        { return profile; }
    public Difficulty  getDifficulty()     { return difficulty; }
    public Player      getPlayer()         { return player; }
    public List<Room>  getRooms()          { return rooms; }
    public int         getLoopCount()      { return loopCount; }
    public int         getScore()          { return score; }
    public boolean     isEscaped()         { return escaped; }
    public long        getTotalElapsedMs() { return totalElapsedMs; }
    public List<String>getCollectedClues() { return roomClueIds; }
}
