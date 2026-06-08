package utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.prefs.Preferences;

public class LocalAuthManager {

    private static final String FILE_NAME = "bitewise_local_users.json";

    private LocalAuthManager() {
    }

    public static int signUp(String email, String password) throws Exception {
        JSONArray users = loadUsers();
        if (findUser(users, email) != null) {
            throw new Exception("User already exists");
        }

        int userId = Math.abs(email.toLowerCase().hashCode());
        JSONObject user = new JSONObject();
        user.put("id", userId);
        user.put("email", email.toLowerCase());
        user.put("passwordHash", hash(password));
        users.put(user);
        saveUsers(users);
        return userId;
    }

    public static int signIn(String email, String password) throws Exception {
        JSONObject user = findUser(loadUsers(), email);
        if (user == null || !user.optString("passwordHash").equals(hash(password))) {
            int legacyUserId = signInWithLegacyPreferences(email, password);
            if (legacyUserId > 0) {
                return legacyUserId;
            }
            throw new Exception("Invalid email or password");
        }
        return user.getInt("id");
    }

    private static int signInWithLegacyPreferences(String email, String password) {
        Preferences legacyPrefs = Preferences.userRoot().node("BiteWiseUser");
        String savedEmail = legacyPrefs.get("email", "");
        String savedPassword = legacyPrefs.get("password", "");
        if (email.equalsIgnoreCase(savedEmail) && password.equals(savedPassword)) {
            return Math.abs(email.toLowerCase().hashCode());
        }
        return -1;
    }

    private static JSONObject findUser(JSONArray users, String email) {
        String target = email.toLowerCase();
        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);
            if (target.equals(user.optString("email"))) {
                return user;
            }
        }
        return null;
    }

    private static JSONArray loadUsers() {
        File file = getUsersFile();
        if (!file.exists()) {
            return new JSONArray();
        }

        try {
            String jsonText = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            return jsonText.isBlank() ? new JSONArray() : new JSONArray(jsonText);
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private static void saveUsers(JSONArray users) throws Exception {
        try (FileWriter writer = new FileWriter(getUsersFile())) {
            writer.write(users.toString(4));
        }
    }

    private static File getUsersFile() {
        return new File(System.getProperty("user.dir"), FILE_NAME);
    }

    private static String hash(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hashed);
    }
}
