package model;

import org.json.JSONObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents one saved detection in the user's history.
 *
 * @author Phu Pham
 */
public record HistoryEntry(String foodName, String detectionTime, NutritionInfo nutrition,
                           String mealId, double servingMultiplier, double confidence, String source) {

    public HistoryEntry(String foodName, String detectionTime, NutritionInfo nutrition) {
        this(foodName, detectionTime, nutrition, "", 1.0, 0.0, "legacy");
    }

    /**
     * Constructor to parse from a JSONObject when loading from a file.
     */
    public HistoryEntry(JSONObject obj) {
        this(
                obj.optString("foodName", "Unknown"),
                obj.optString("detectionTime", "Unknown Date"),
                new NutritionInfo(obj.optJSONObject("nutrition")),
                obj.optString("mealId", ""),
                obj.optDouble("servingMultiplier", 1.0),
                obj.optDouble("confidence", 0.0),
                obj.optString("source", "legacy")
        );
    }

    /**
     * Converts this object to a JSONObject for saving to a file.
     */
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("foodName", foodName);
        obj.put("detectionTime", detectionTime);
        obj.put("nutrition", nutrition.toJSONObject()); // Serialize NutritionInfo
        if (mealId != null && !mealId.isBlank()) {
            obj.put("mealId", mealId);
        }
        if (servingMultiplier != 1.0) {
            obj.put("servingMultiplier", servingMultiplier);
        }
        if (confidence > 0.0) {
            obj.put("confidence", confidence);
        }
        if (source != null && !source.isBlank() && !"legacy".equals(source)) {
            obj.put("source", source);
        }
        return obj;
    }

    /**
     * This is how the entry will be displayed in the history ListView.
     */
    @Override
    public String toString() {
        try {
            // Try to parse the saved time string
            LocalDateTime dt = LocalDateTime.parse(detectionTime);
            // Reformat it to be user-friendly
            String formattedTime = dt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"));
            return foodName + "\n" + formattedTime;
        } catch (Exception e) {
            // Fallback if parsing fails
            return foodName + "\n" + detectionTime;
        }
    }
}
