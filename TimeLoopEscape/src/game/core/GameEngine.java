package game.core;

import game.audio.AudioManager;
import game.ui.GameWindow;
import game.ui.screens.MainMenuScreen;
import game.ui.screens.LoginScreen;
import game.ui.screens.LoadingScreen;
import game.utils.AccountManager;
import game.utils.PremiumManager;
import game.utils.ProfileManager;
import game.utils.SettingsManager;

public class GameEngine {

    private static GameEngine instance;
    public static GameEngine getInstance() {
        if (instance == null) instance = new GameEngine();
        return instance;
    }

    private GameWindow      window;
    private AudioManager    audioManager;
    private ProfileManager  profileManager;
    private SettingsManager settingsManager;
    private AccountManager  accountManager;
    private PremiumManager  premiumManager;

    private GameState currentState = GameState.LOGIN;
    private boolean   running      = false;

    private GameEngine() {}

    public void start() {
        System.out.println("[GameEngine] Starting Time Loop Escape...");

        settingsManager = SettingsManager.getInstance();
        profileManager  = ProfileManager.getInstance();
        accountManager  = AccountManager.getInstance();
        audioManager    = AudioManager.getInstance();
        premiumManager  = PremiumManager.getInstance();

        audioManager.setMasterVolume(settingsManager.getMasterVolume());
        audioManager.setMusicVolume(settingsManager.getMusicVolume());
        audioManager.setSFXVolume(settingsManager.getSFXVolume());
        audioManager.setMusicMuted(!settingsManager.isMusicEnabled());
        audioManager.setSFXMuted(!settingsManager.isSFXEnabled());

        window = new GameWindow();
        window.setVisible(true);

        running = true;
        showLoading();

        audioManager.playMusic(AudioManager.Track.MAIN_MENU);
    }

    public void stop() {
        running = false;
        audioManager.stopAll();
        window.dispose();
        System.exit(0);
    }

    public void showLoading() {
        currentState = GameState.LOADING;
        window.showScreen(new LoadingScreen(window));
    }

    public void showLogin() {
        currentState = GameState.LOGIN;
        window.showScreen(new LoginScreen(window));
    }

    public void showMainMenu() {
        currentState = GameState.MAIN_MENU;
        audioManager.playMusic(AudioManager.Track.MAIN_MENU);
        window.showScreen(new MainMenuScreen(window));
    }

    public void startGame(Difficulty difficulty) {
        currentState = GameState.PLAYING;
        audioManager.playMusic(AudioManager.Track.GAMEPLAY);
        GameSession session = new GameSession(profileManager.getActiveProfile(), difficulty);
        window.showScreen(session.buildGameScreen(window));
    }

    public void showProfile() {
        currentState = GameState.PROFILE;
        window.showScreen(new game.ui.screens.ProfileScreen(window));
    }

    public void showSettings() {
        currentState = GameState.SETTINGS;
        window.showScreen(new game.ui.screens.SettingsScreen(window));
    }

    public void showDifficultySelect() {
        currentState = GameState.DIFFICULTY_SELECT;
        window.showScreen(new game.ui.screens.DifficultyScreen(window));
    }

    public void showPremium() {
        currentState = GameState.PREMIUM;
        window.showScreen(new game.ui.screens.PremiumScreen(window));
    }

    public void showTutorial() {
        currentState = GameState.TUTORIAL;
        window.showScreen(new game.ui.screens.TutorialScreen(window));
    }

    public void showVictory(GameSession session) {
        currentState = GameState.VICTORY;
        audioManager.playMusic(AudioManager.Track.VICTORY);
        window.showScreen(new game.ui.screens.VictoryScreen(window, session));
    }

    public void showGameOver(GameSession session) {
        currentState = GameState.GAME_OVER;
        audioManager.playMusic(AudioManager.Track.GAME_OVER);
        window.showScreen(new game.ui.screens.GameOverScreen(window, session));
    }

    public void logout() {
        accountManager.logout();
        profileManager.clearGuestProfile();
        showLogin();
    }

    public GameWindow      getWindow()           { return window; }
    public AudioManager    getAudioManager()     { return audioManager; }
    public ProfileManager  getProfileManager()   { return profileManager; }
    public SettingsManager getSettingsManager()  { return settingsManager; }
    public AccountManager  getAccountManager()   { return accountManager; }
    public PremiumManager  getPremiumManager()   { return premiumManager; }
    public GameState       getCurrentState()     { return currentState; }
    public boolean         isRunning()           { return running; }

    public enum GameState {
        LOADING, LOGIN, MAIN_MENU, PLAYING, PAUSED,
        PROFILE, SETTINGS, DIFFICULTY_SELECT,
        VICTORY, GAME_OVER, PREMIUM, TUTORIAL
    }
}
