package game.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ProfileManager {

    private static final String SAVE_DIR  =
            System.getProperty("user.home") + File.separator + ".timeloop";
    private static final String SAVE_FILE = SAVE_DIR + File.separator + "profiles.dat";

    private static ProfileManager instance;
    public static ProfileManager getInstance() {
        if (instance == null) instance = new ProfileManager();
        return instance;
    }

    private final List<Profile> profiles = new ArrayList<>();
    private Profile activeProfile;
    private Profile guestProfile;

    private ProfileManager() {
        loadProfiles();
        if (profiles.isEmpty()) {
            activeProfile = null;
        } else {
            activeProfile = profiles.get(0);
        }
    }

    public void setGuestProfile(Profile p) {
        guestProfile  = p;
        activeProfile = p;
    }

    public void clearGuestProfile() {
        guestProfile = null;
        activeProfile = profiles.isEmpty() ? null : profiles.get(0);
    }

    public Profile createProfile(String name) {
        if (profiles.size() >= 3)
            throw new IllegalStateException("Maximum 3 profiles allowed.");
        Profile p = new Profile(name);
        profiles.add(p);
        saveProfiles();
        return p;
    }

    public void deleteProfile(Profile p) {
        profiles.remove(p);
        if (activeProfile == p)
            activeProfile = profiles.isEmpty() ? null : profiles.get(0);
        saveProfiles();
    }

    public void setActiveProfile(Profile p) {
        if (profiles.contains(p) || p == guestProfile)
            activeProfile = p;
    }

    public Profile getActiveProfile() {
        if (activeProfile == null && !profiles.isEmpty())
            activeProfile = profiles.get(0);
        return activeProfile;
    }

    public List<Profile> getProfiles() { return profiles; }

    public void saveProfiles() {
        try {
            new File(SAVE_DIR).mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(SAVE_FILE))) {
                for (Profile p : profiles) {
                    if (p.isGuest()) continue;
                    pw.println("name="         + p.getName());
                    pw.println("avatar="       + p.getAvatarId());
                    pw.println("games="        + p.getTotalGamesPlayed());
                    pw.println("escapes="      + p.getTotalEscapes());
                    pw.println("bestScore="    + p.getBestScore());
                    pw.println("playtime="     + p.getTotalPlaytimeMs());
                    pw.println("created="      + p.getCreatedAt());
                    pw.println("---");
                }
            }
            System.out.println("[ProfileManager] Saved " + profiles.size() + " profile(s).");
        } catch (IOException e) {
            System.err.println("[ProfileManager] Save failed: " + e.getMessage());
        }
    }

    private void loadProfiles() {
        File f = new File(SAVE_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            Profile cur = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("---")) {
                    if (cur != null) profiles.add(cur);
                    cur = null;
                    continue;
                }
                String[] parts = line.split("=", 2);
                if (parts.length < 2) continue;
                String k = parts[0], v = parts[1];
                switch (k) {
                    case "name"      -> cur = new Profile(v);
                    case "avatar"    -> { if (cur != null) cur.setAvatarId(v); }
                    case "games"     -> { if (cur != null) cur.setTotalGamesPlayed(parseInt(v)); }
                    case "escapes"   -> { if (cur != null) cur.setTotalEscapes(parseInt(v)); }
                    case "bestScore" -> { if (cur != null) cur.setBestScore(parseInt(v)); }
                    case "playtime"  -> { if (cur != null) cur.setTotalPlaytimeMs(parseLong(v)); }
                    case "created"   -> { if (cur != null) cur.setCreatedAt(v); }
                }
            }
            System.out.println("[ProfileManager] Loaded " + profiles.size() + " profile(s).");
        } catch (IOException e) {
            System.err.println("[ProfileManager] Load failed: " + e.getMessage());
        }
    }

    private int  parseInt(String s)  { try { return Integer.parseInt(s);  } catch (Exception e) { return 0; } }
    private long parseLong(String s) { try { return Long.parseLong(s);     } catch (Exception e) { return 0; } }
}
