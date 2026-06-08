package utility;

import model.NutritionInfo;
import org.json.JSONArray;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * A utility class that move the logic of history scan
 * and make recommendation base on scan history
 *
 * @author Phu Pham
 */
public class NutritionManager {

    private static final String FILE_PATH = "/edu/utsa/cs3443/group7/bitewise/food_nutrition.json";
    private static final List<NutritionInfo> allFood = new ArrayList<>();

    // Load data once
    static {
        loadData();
    }

    private static void loadData() {
        try (InputStream is = NutritionManager.class.getResourceAsStream(FILE_PATH)) {
            if (is == null) return;
            String jsonText = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(jsonText);

            for (int i = 0; i < jsonArray.length(); i++) {
                allFood.add(new NutritionInfo(jsonArray.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Finds a random food item that has FEWER calories than the max specified.
     */
    public static NutritionInfo getRecommendation(int maxCalories) {
        List<NutritionInfo> candidates = new ArrayList<>();

        for (NutritionInfo food : allFood) {
            int cals = food.getNutritionAsInt(); // Using your helper method
            // We want food that fits the budget, but isn't too small (unless budget is tiny)
            if (cals > 0 && cals <= maxCalories) {
                candidates.add(food);
            }
        }

        if (candidates.isEmpty()) return null;

        // Pick a random one from the candidates
        Random rand = new Random();
        return candidates.get(rand.nextInt(candidates.size()));
    }

    public static List<NutritionInfo> getAllFood() {
        return Collections.unmodifiableList(allFood);
    }

    public static NutritionInfo findByName(String foodName) {
        if (foodName == null || foodName.isBlank()) {
            return null;
        }

        String target = foodName.toLowerCase(Locale.ROOT).trim();
        for (NutritionInfo food : allFood) {
            if (food.name().toLowerCase(Locale.ROOT).equals(target)) {
                return food;
            }
        }
        return null;
    }

    public static NutritionInfo findClosestByName(String foodName) {
        NutritionInfo exact = findByName(foodName);
        if (exact != null || foodName == null) {
            return exact;
        }

        String target = foodName.toLowerCase(Locale.ROOT).trim();
        for (NutritionInfo food : allFood) {
            String name = food.name().toLowerCase(Locale.ROOT);
            if (target.contains(name) || name.contains(target)) {
                return food;
            }
        }
        return null;
    }

    public static NutritionInfo getProteinPick(int maxCalories) {
        return allFood.stream()
                .filter(food -> food.getNutritionAsInt() > 0 && food.getNutritionAsInt() <= maxCalories)
                .max(Comparator.comparingInt(food -> parseNumber(food.protein())))
                .orElse(null);
    }

    public static NutritionInfo getLowestCalorieOption(int maxCalories) {
        return allFood.stream()
                .filter(food -> food.getNutritionAsInt() > 0 && food.getNutritionAsInt() <= maxCalories)
                .min(Comparator.comparingInt(NutritionInfo::getNutritionAsInt))
                .orElse(null);
    }

    public static int parseNumber(String value) {
        if (value == null) {
            return 0;
        }
        String clean = value.replaceAll("[^\\d]", "");
        if (clean.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(clean);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

