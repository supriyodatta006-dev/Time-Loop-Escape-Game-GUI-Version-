package game.utils;

import java.io.*;
import java.util.Properties;
import java.util.Set;

public class PremiumManager {

    private static PremiumManager instance;
    public static PremiumManager getInstance() {
        if (instance == null) instance = new PremiumManager();
        return instance;
    }

    private static final String SAVE_DIR  = System.getProperty("user.home") + File.separator + ".timeloop";
    private static final String PREM_FILE = SAVE_DIR + File.separator + "premium.properties";

    private static final Set<String> VALID_KEYS = Set.of(
            "TIMELOOPPREMIUM",
            "TIMELOOPDEMO2024",
            "TEMPORALESCAPEPRO",
            "TIMELOOP2024"
    );

    public static final int  FREE_ROOM_LIMIT   = 7;
    public static final int  FREE_PROFILE_LIMIT = 1;

    private boolean premium    = false;
    private String  licenceKey = null;

    private PremiumManager() { load(); }

    public boolean isPremium() { return premium; }

    public String getLicenceKey() { return licenceKey; }

    public boolean activateWithKey(String key) {
        if (key == null || key.isBlank()) return false;

        String normalised = key.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (VALID_KEYS.contains(normalised)) {
            premium    = true;
            licenceKey = key.trim().toUpperCase();
            save();
            System.out.println("[Premium] Activated with key: " + licenceKey);
            return true;
        }
        System.out.println("[Premium] Invalid key: \"" + key + "\"");
        return false;
    }

    public void simulatePurchase() {
        premium    = true;
        licenceKey = "PURCHASED-" + System.currentTimeMillis();
        save();
        System.out.println("[Premium] Simulated purchase activated.");
    }

    public void revoke() {
        premium    = false;
        licenceKey = null;
        save();
        System.out.println("[Premium] Revoked.");
    }

    public int getAllowedRoomCount() {
        return premium ? Integer.MAX_VALUE : FREE_ROOM_LIMIT;
    }

    public int getAllowedProfileSlots() {
        return premium ? 3 : FREE_PROFILE_LIMIT;
    }

    public boolean isDifficultyAllowed(game.core.Difficulty diff) {
        if (premium) return true;
        return diff == game.core.Difficulty.EASY || diff == game.core.Difficulty.NORMAL;
    }

    private void load() {
        File f = new File(PREM_FILE);
        if (!f.exists()) return;
        try (FileInputStream fis = new FileInputStream(f)) {
            Properties props = new Properties();
            props.load(fis);
            premium    = Boolean.parseBoolean(props.getProperty("premium",    "false"));
            licenceKey = props.getProperty("licenceKey", null);
            System.out.println("[Premium] Loaded: premium=" + premium);
        } catch (Exception e) {
            System.err.println("[Premium] Load error: " + e.getMessage());
        }
    }

    private void save() {
        try {
            new File(SAVE_DIR).mkdirs();
            Properties props = new Properties();
            props.setProperty("premium",    String.valueOf(premium));
            if (licenceKey != null)
                props.setProperty("licenceKey", licenceKey);
            try (FileOutputStream fos = new FileOutputStream(PREM_FILE)) {
                props.store(fos, "Time Loop Escape — Premium Licence");
            }
        } catch (IOException e) {
            System.err.println("[Premium] Save error: " + e.getMessage());
        }
    }
}
