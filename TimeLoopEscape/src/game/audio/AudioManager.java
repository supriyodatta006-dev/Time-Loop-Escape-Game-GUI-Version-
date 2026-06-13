package game.audio;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

public class AudioManager {

    private static AudioManager instance;
    public static AudioManager getInstance() {
        if (instance == null) instance = new AudioManager();
        return instance;
    }

    public enum Track {
        MAIN_MENU ("music_menu.wav"),
        GAMEPLAY  ("music_gameplay.wav"),
        VICTORY   ("music_victory.wav"),
        GAME_OVER ("music_gameover.wav");

        public final String file;
        Track(String file) { this.file = file; }
    }

    public enum SFX {
        CLICK      ("sfx_click.wav"),
        CLUE_FOUND ("sfx_clue.wav"),
        LOOP_RESET ("sfx_loop_reset.wav"),
        ESCAPE     ("sfx_escape.wav"),
        TICK       ("sfx_tick.wav"),
        ERROR      ("sfx_error.wav"),
        DOOR_OPEN  ("sfx_door.wav");

        public final String file;
        SFX(String file) { this.file = file; }
    }

    private Clip  currentMusicClip;
    private Track currentTrack;

    private float   masterVolume = 0.8f;
    private float   musicVolume  = 0.6f;
    private float   sfxVolume    = 0.9f;
    private boolean musicMuted   = false;
    private boolean sfxMuted     = false;

    private final Map<SFX, Clip> sfxCache = new EnumMap<>(SFX.class);

    private AudioManager() {
        System.out.println("[Audio] Initialising. Working dir: "
                + System.getProperty("user.dir"));
        preloadSFX();
    }

    public void playMusic(Track track) {
        if (track == currentTrack && currentMusicClip != null
                && currentMusicClip.isRunning()) return;
        stopMusic();
        currentTrack = track;

        Clip clip = loadClip(track.file);
        if (clip == null) {
            System.out.println("[Audio] Music not found: " + track.file);
            return;
        }
        currentMusicClip = clip;
        setClipVolume(clip, masterVolume * musicVolume);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        if (!musicMuted) {
            clip.start();
        }
        System.out.println("[Audio] Playing music: " + track.file);
    }

    public void stopMusic() {
        if (currentMusicClip != null) {
            currentMusicClip.stop();
            currentMusicClip.close();
            currentMusicClip = null;
        }
        currentTrack = null;
    }

    public void pauseMusic() {
        if (currentMusicClip != null && currentMusicClip.isRunning())
            currentMusicClip.stop();
    }

    public void resumeMusic() {
        if (currentMusicClip != null && !currentMusicClip.isRunning())
            currentMusicClip.start();
    }

    public void playSFX(SFX sfx) {
        if (sfxMuted) return;
        Clip clip = sfxCache.get(sfx);
        if (clip == null) return;
        clip.stop();
        clip.setFramePosition(0);
        setClipVolume(clip, masterVolume * sfxVolume);
        clip.start();
    }

    private void setClipVolume(Clip clip, float level) {
        try {
            FloatControl fc = (FloatControl)
                    clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float)(20.0 * Math.log10(Math.max(level, 0.0001)));
            dB = Math.max(fc.getMinimum(), Math.min(dB, fc.getMaximum()));
            fc.setValue(dB);
        } catch (Exception ignored) {}
    }

    public void setMasterVolume(float v) {
        masterVolume = clamp(v);
        if (currentMusicClip != null)
            setClipVolume(currentMusicClip, masterVolume * musicVolume);
    }
    public void setMusicVolume(float v) {
        musicVolume = clamp(v);
        if (currentMusicClip != null)
            setClipVolume(currentMusicClip, masterVolume * musicVolume);
    }
    public void setSFXVolume(float v)       { sfxVolume  = clamp(v); }
    public void setMusicMuted(boolean m)    { musicMuted = m; if (m) pauseMusic(); else resumeMusic(); }
    public void setSFXMuted(boolean m)      { sfxMuted   = m; }
    public float   getMasterVolume()        { return masterVolume; }
    public float   getMusicVolume()         { return musicVolume;  }
    public float   getSFXVolume()           { return sfxVolume;    }
    public boolean isMusicMuted()           { return musicMuted;   }
    public boolean isSFXMuted()             { return sfxMuted;     }

    public void stopAll() {
        stopMusic();
        sfxCache.values().forEach(Clip::close);
        sfxCache.clear();
    }

    private void preloadSFX() {
        for (SFX sfx : SFX.values()) {
            Clip c = loadClip(sfx.file);
            if (c != null) {
                sfxCache.put(sfx, c);
                System.out.println("[Audio] Loaded SFX: " + sfx.file);
            }
        }
    }

    private Clip loadClip(String filename) {
        AudioInputStream ais = openStream(filename);
        if (ais == null) return null;
        try {

            AudioFormat base   = ais.getFormat();
            AudioFormat target = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    base.getSampleRate(),
                    16,
                    base.getChannels(),
                    base.getChannels() * 2,
                    base.getSampleRate(),
                    false);
            if (!base.matches(target)) {
                try { ais = AudioSystem.getAudioInputStream(target, ais); }
                catch (Exception ignored) {  }
            }
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            return clip;
        } catch (LineUnavailableException | IOException e) {
            System.out.println("[Audio] Failed to open clip: " + filename + " — " + e.getMessage());
            return null;
        }
    }

    private AudioInputStream openStream(String filename) {

        try {
            URL url = getClass().getClassLoader().getResource("sounds/" + filename);
            if (url != null) {
                System.out.println("[Audio] Found via classloader: " + filename);
                return AudioSystem.getAudioInputStream(url);
            }
        } catch (Exception ignored) {}

        String[] candidates = {
            "resources/sounds/" + filename,
            "sounds/"           + filename,
            "../resources/sounds/" + filename,
            "TimeLoopEscape/resources/sounds/" + filename,
        };
        for (String path : candidates) {
            File f = new File(path);
            if (f.exists()) {
                try {
                    System.out.println("[Audio] Found via file path: " + f.getAbsolutePath());
                    return AudioSystem.getAudioInputStream(f);
                } catch (Exception ignored) {}
            }
        }

        try {
            File classBase = new File(
                    getClass().getProtectionDomain()
                              .getCodeSource()
                              .getLocation()
                              .toURI());

            File dir = classBase.isFile() ? classBase.getParentFile() : classBase;
            for (int up = 0; up < 6; up++) {
                File candidate = new File(dir, "resources/sounds/" + filename);
                if (candidate.exists()) {
                    System.out.println("[Audio] Found via class-base: " + candidate.getAbsolutePath());
                    return AudioSystem.getAudioInputStream(candidate);
                }
                if (dir.getParentFile() == null) break;
                dir = dir.getParentFile();
            }
        } catch (Exception ignored) {}

        System.out.println("[Audio] Could not locate: " + filename);
        return null;
    }

    private float clamp(float v) { return Math.max(0f, Math.min(1f, v)); }
}
