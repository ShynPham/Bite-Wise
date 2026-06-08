package utility;

import model.NutritionInfo;
import org.json.JSONArray;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
           // e.printStackTrace();
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
}

