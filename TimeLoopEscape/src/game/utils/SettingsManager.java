package game.utils;

import java.io.*;
import java.util.Properties;

public class SettingsManager {

    private static final String SAVE_DIR  = System.getProperty("user.home") + File.separator + ".timeloop";
    private static final String PROP_FILE = SAVE_DIR + File.separator + "settings.properties";

    private static SettingsManager instance;
    public static SettingsManager getInstance() {
        if (instance == null) instance = new SettingsManager();
        return instance;
    }

    private final Properties props = new Properties();

    private float  masterVolume  = 0.8f;
    private float  musicVolume   = 0.6f;
    private float  sfxVolume     = 0.9f;
    private boolean musicEnabled = true;
    private boolean sfxEnabled   = true;
    private boolean fullscreen   = false;
    private boolean showFPS      = false;
    private String  lastDifficulty = "NORMAL";

    private SettingsManager() {
        load();
    }

    public void load() {
        File f = new File(PROP_FILE);
        if (!f.exists()) { save(); return; }
        try (FileInputStream fis = new FileInputStream(f)) {
            props.load(fis);
            masterVolume   = Float.parseFloat(props.getProperty("masterVolume",   "0.8"));
            musicVolume    = Float.parseFloat(props.getProperty("musicVolume",    "0.6"));
            sfxVolume      = Float.parseFloat(props.getProperty("sfxVolume",      "0.9"));
            musicEnabled   = Boolean.parseBoolean(props.getProperty("musicEnabled", "true"));
            sfxEnabled     = Boolean.parseBoolean(props.getProperty("sfxEnabled",   "true"));
            fullscreen     = Boolean.parseBoolean(props.getProperty("fullscreen",   "false"));
            showFPS        = Boolean.parseBoolean(props.getProperty("showFPS",      "false"));
            lastDifficulty = props.getProperty("lastDifficulty", "NORMAL");
        } catch (Exception e) {
            System.err.println("[Settings] Load error: " + e.getMessage());
        }
    }

    public void save() {
        try {
            new File(SAVE_DIR).mkdirs();
            props.setProperty("masterVolume",   String.valueOf(masterVolume));
            props.setProperty("musicVolume",    String.valueOf(musicVolume));
            props.setProperty("sfxVolume",      String.valueOf(sfxVolume));
            props.setProperty("musicEnabled",   String.valueOf(musicEnabled));
            props.setProperty("sfxEnabled",     String.valueOf(sfxEnabled));
            props.setProperty("fullscreen",     String.valueOf(fullscreen));
            props.setProperty("showFPS",        String.valueOf(showFPS));
            props.setProperty("lastDifficulty", lastDifficulty);
            try (FileOutputStream fos = new FileOutputStream(PROP_FILE)) {
                props.store(fos, "Time Loop Escape — Settings");
            }
        } catch (IOException e) {
            System.err.println("[Settings] Save error: " + e.getMessage());
        }
    }

    public float  getMasterVolume()             { return masterVolume; }
    public void   setMasterVolume(float v)      { masterVolume = v; save(); }

    public float  getMusicVolume()              { return musicVolume; }
    public void   setMusicVolume(float v)       { musicVolume = v; save(); }

    public float  getSFXVolume()                { return sfxVolume; }
    public void   setSFXVolume(float v)         { sfxVolume = v; save(); }

    public boolean isMusicEnabled()             { return musicEnabled; }
    public void   setMusicEnabled(boolean b)    { musicEnabled = b; save(); }

    public boolean isSFXEnabled()               { return sfxEnabled; }
    public void   setSFXEnabled(boolean b)      { sfxEnabled = b; save(); }

    public boolean isFullscreen()               { return fullscreen; }
    public void   setFullscreen(boolean b)      { fullscreen = b; save(); }

    public boolean isShowFPS()                  { return showFPS; }
    public void   setShowFPS(boolean b)         { showFPS = b; save(); }

    public String  getLastDifficulty()          { return lastDifficulty; }
    public void   setLastDifficulty(String d)   { lastDifficulty = d; save(); }
}
