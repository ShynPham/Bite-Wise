package utility;

import model.HistoryEntry;
import model.MealDraftItem;
import model.NutritionInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MealDraftManager {

    private static final List<MealDraftItem> draftItems = new ArrayList<>();
    private static String mealId = "";

    private MealDraftManager() {
    }

    public static void setDraft(List<MealDraftItem> items) {
        draftItems.clear();
        draftItems.addAll(items);
        mealId = "meal-" + UUID.randomUUID();
    }

    public static List<MealDraftItem> getDraft() {
        return new ArrayList<>(draftItems);
    }

    public static boolean isEmpty() {
        return draftItems.isEmpty();
    }

    public static void clear() {
        draftItems.clear();
        mealId = "";
    }

    public static String getMealId() {
        return mealId;
    }

    public static void remove(int index) {
        if (index >= 0 && index < draftItems.size()) {
            draftItems.remove(index);
        }
    }

    public static void rename(int index, String foodName, NutritionInfo nutrition) {
        if (index >= 0 && index < draftItems.size() && nutrition != null) {
            draftItems.set(index, draftItems.get(index).withFood(foodName, nutrition));
        }
    }

    public static void setServingMultiplier(int index, double multiplier) {
        if (index >= 0 && index < draftItems.size()) {
            draftItems.set(index, draftItems.get(index).withServingMultiplier(multiplier));
        }
    }

    public static int getTotalCalories() {
        int total = 0;
        for (MealDraftItem item : draftItems) {
            total += item.caloriesWithServing();
        }
        return total;
    }

    public static int saveDraftToHistory() {
        if (draftItems.isEmpty()) {
            return 0;
        }

        List<HistoryEntry> history = HistoryManager.loadHistory();
        String detectionTime = LocalDateTime.now().toString();
        String sharedMealId = mealId == null || mealId.isBlank() ? "meal-" + UUID.randomUUID() : mealId;

        for (MealDraftItem item : draftItems) {
            NutritionInfo nutrition = item.nutrition();
            if (nutrition == null) {
                nutrition = new NutritionInfo("N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A");
            }
            history.add(new HistoryEntry(
                    item.foodName(),
                    detectionTime,
                    nutrition,
                    sharedMealId,
                    item.servingMultiplier(),
                    item.confidence(),
                    "meal-review"
            ));
        }

        int saved = draftItems.size();
        HistoryManager.saveHistory(history);
        clear();
        return saved;
    }
}
