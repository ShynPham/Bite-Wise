package model;

import org.json.JSONObject;

/**
 * A record to hold nutritional data from the JSON file.
 * @author Phu Pham
 */
public record NutritionInfo(String name, String calories, String totalFat,
                            String satFat, String cholesterol,
                            String sodium, String protein) {

    /**
     * A helper constructor to parse a JSONObject.
     */
    public NutritionInfo(JSONObject obj) {
        this(
                obj.optString("name", "N/A"),
                obj.optString("calories", "N/A"),
                obj.optString("total_fat", "N/A"),
                obj.optString("saturated_fat", "N/A"),
                obj.optString("cholesterol", "N/A"),
                obj.optString("sodium", "N/A"),
                obj.optString("protein", "N/A")
        );
    }

    /**
     * Converts this object into a JSONObject for saving to a file.
     */
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("calories", calories);
        obj.put("total_fat", totalFat);
        obj.put("saturated_fat", satFat);
        obj.put("cholesterol", cholesterol);
        obj.put("sodium", sodium);
        obj.put("protein", protein);
        return obj;
    }

    /**
     * Formats the nutrition data as a clean string for the TextArea.
     */
    @Override
    public String toString() {
        return "  - Calories: " + calories + "\n" +
                "  - Total Fat: " + totalFat + "\n" +
                "  - Sat. Fat: " + satFat + "\n" +
                "  - Cholesterol: " + cholesterol + "\n" +
                "  - Sodium: " + sodium + "\n" +
                "  - Protein: " + protein + "\n";
    }

    /**
     * Safely parses the calorie string into an integer.
     * @return The calorie count as an int, or 0 if invalid.
     */
    public int getNutritionAsInt() {
        try {
            if (calories == null) return 0;
            // Removes "cal", spaces, and non-numbers
            String cleanCalories = calories.replaceAll("[^\\d]", "");
            if (cleanCalories.isEmpty()) return 0;
            return Integer.parseInt(cleanCalories);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}