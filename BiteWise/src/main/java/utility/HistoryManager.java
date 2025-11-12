package utility;

import model.HistoryEntry;
import org.json.JSONArray;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages saving and loading detection history to/from detection_history.json.
 */
public class HistoryManager {

    private static final String FILE_NAME = "detection_history.json";

    /**
     * Gets the file object for the history JSON file (in the project root).
     */
    private static File getHistoryFile() {
        String projectRoot = System.getProperty("user.dir");
        return new File(projectRoot, FILE_NAME);
    }

    /**
     * Loads all saved history entries from the JSON file.
     *
     * @return A list of HistoryEntry objects. Returns an empty list if file not found.
     */
    public static List<HistoryEntry> loadHistory() {
        List<HistoryEntry> history = new ArrayList<>();
        File file = getHistoryFile();
        if (!file.exists()) {
            return history; // Return empty list if no file
        }

        try {
            // Read the entire file into a string
            String jsonText = new String(Files.readAllBytes(file.toPath()));
            if (jsonText.isEmpty()) {
                return history;
            }

            // Parse the string into a JSON array
            JSONArray jsonArray = new JSONArray(jsonText);

            // Convert each JSONObject in the array to a HistoryEntry object
            for (int i = 0; i < jsonArray.length(); i++) {
                history.add(new HistoryEntry(jsonArray.getJSONObject(i)));
            }
        } catch (IOException | org.json.JSONException e) {
            System.err.println("Could not read or parse history file: " + e.getMessage());
            e.printStackTrace();
        }
        return history;
    }

    /**
     * Overwrites the history file with the provided (new) list of entries.
     *
     * @param history The complete list of entries to save.
     */
    public static void saveHistory(List<HistoryEntry> history) {
        JSONArray jsonArray = new JSONArray();

        // Convert every HistoryEntry object into a JSONObject
        for (HistoryEntry entry : history) {
            jsonArray.put(entry.toJSONObject());
        }

        // Use try-with-resources to auto-close the writer
        try (FileWriter fileWriter = new FileWriter(getHistoryFile())) {
            // Write the JSON array to the file with 4-space indentation
            fileWriter.write(jsonArray.toString(4));
        } catch (IOException e) {
            System.err.println("Could not save history file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
