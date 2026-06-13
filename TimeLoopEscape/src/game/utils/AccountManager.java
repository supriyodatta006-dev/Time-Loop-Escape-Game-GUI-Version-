package game.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;

public class AccountManager {

    private static final String SAVE_DIR  =
            System.getProperty("user.home") + File.separator + ".timeloop";
    private static final String SAVE_FILE = SAVE_DIR + File.separator + "accounts.dat";
    private static final int    MAX_ACCOUNTS = 10;

    private static AccountManager instance;
    public static AccountManager getInstance() {
        if (instance == null) instance = new AccountManager();
        return instance;
    }

    public static class Account {
        public final String username;
        public final String passwordHash;
        public final String salt;
        public final String profileName;
        public final String createdAt;

        public Account(String username, String passwordHash,
                       String salt, String profileName, String createdAt) {
            this.username     = username;
            this.passwordHash = passwordHash;
            this.salt         = salt;
            this.profileName  = profileName;
            this.createdAt    = createdAt;
        }
    }

    public enum LoginResult  { SUCCESS, WRONG_PASSWORD, NOT_FOUND }
    public enum RegisterResult {
        SUCCESS, USERNAME_TAKEN, USERNAME_TOO_SHORT,
        PASSWORD_TOO_SHORT, USERNAME_INVALID, MAX_ACCOUNTS_REACHED
    }

    private final Map<String, Account> accounts    = new LinkedHashMap<>();
    private       Account              loggedIn    = null;
    private final SecureRandom         rng         = new SecureRandom();

    private AccountManager() {
        loadAccounts();
    }

    public LoginResult login(String username, String password) {
        String key = username.trim().toLowerCase();
        Account acc = accounts.get(key);
        if (acc == null) return LoginResult.NOT_FOUND;

        String hash = hash(password, acc.salt);
        if (!hash.equals(acc.passwordHash)) return LoginResult.WRONG_PASSWORD;

        loggedIn = acc;
        System.out.println("[AccountManager] Logged in: " + username);
        return LoginResult.SUCCESS;
    }

    public RegisterResult register(String username, String password) {
        String trimmed = username.trim();
        String key     = trimmed.toLowerCase();

        if (trimmed.length() < 3)               return RegisterResult.USERNAME_TOO_SHORT;
        if (password.length() < 4)              return RegisterResult.PASSWORD_TOO_SHORT;
        if (!trimmed.matches("[a-zA-Z0-9_]+"))  return RegisterResult.USERNAME_INVALID;
        if (accounts.containsKey(key))          return RegisterResult.USERNAME_TAKEN;
        if (accounts.size() >= MAX_ACCOUNTS)    return RegisterResult.MAX_ACCOUNTS_REACHED;

        String salt = generateSalt();
        String hash = hash(password, salt);
        Account acc = new Account(trimmed, hash, salt, trimmed,
                java.time.LocalDate.now().toString());
        accounts.put(key, acc);
        loggedIn = acc;
        saveAccounts();
        System.out.println("[AccountManager] Registered: " + trimmed);
        return RegisterResult.SUCCESS;
    }

    public void logout() {
        loggedIn = null;
        ProfileManager.getInstance().clearGuestProfile();
        System.out.println("[AccountManager] Logged out.");
    }

    public boolean isLoggedIn()     { return loggedIn != null; }
    public Account getLoggedIn()    { return loggedIn; }
    public boolean accountExists(String username) {
        return accounts.containsKey(username.trim().toLowerCase());
    }

    private String generateSalt() {
        byte[] salt = new byte[16];
        rng.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {

            return salt + password;
        }
    }

    public void saveAccounts() {
        try {
            new File(SAVE_DIR).mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(SAVE_FILE))) {
                for (Account acc : accounts.values()) {
                    pw.println("username="    + acc.username);
                    pw.println("hash="        + acc.passwordHash);
                    pw.println("salt="        + acc.salt);
                    pw.println("profile="     + acc.profileName);
                    pw.println("created="     + acc.createdAt);
                    pw.println("---");
                }
            }
        } catch (IOException e) {
            System.err.println("[AccountManager] Save failed: " + e.getMessage());
        }
    }

    private void loadAccounts() {
        File f = new File(SAVE_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            String user = null, hash = null, salt = null,
                   prof = null, created = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("---")) {
                    if (user != null && hash != null && salt != null) {
                        Account acc = new Account(user, hash, salt,
                                prof != null ? prof : user,
                                created != null ? created : "");
                        accounts.put(user.toLowerCase(), acc);
                    }
                    user = hash = salt = prof = created = null;
                    continue;
                }
                String[] parts = line.split("=", 2);
                if (parts.length < 2) continue;
                switch (parts[0]) {
                    case "username" -> user    = parts[1];
                    case "hash"     -> hash    = parts[1];
                    case "salt"     -> salt    = parts[1];
                    case "profile"  -> prof    = parts[1];
                    case "created"  -> created = parts[1];
                }
            }
            System.out.println("[AccountManager] Loaded " + accounts.size() + " account(s).");
        } catch (IOException e) {
            System.err.println("[AccountManager] Load failed: " + e.getMessage());
        }
    }

    public int getAccountCount() { return accounts.size(); }
}
