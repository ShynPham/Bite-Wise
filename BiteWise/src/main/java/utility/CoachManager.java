package utility;

import model.CoachInsight;
import model.HistoryEntry;
import model.NutritionInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class CoachManager {

    public static final String PREF_NODE = "BiteWiseUsers";
    public static final String CALORIES_LIMIT = "dailyCalorieLimit";
    private static final int DEFAULT_GOAL = 2000;

    private CoachManager() {
    }

    public static int getDailyGoal() {
        return Preferences.userRoot().node(PREF_NODE).getInt(CALORIES_LIMIT, DEFAULT_GOAL);
    }

    public static int getTodayCalories() {
        return caloriesForDate(LocalDate.now());
    }

    public static int getRemainingCalories() {
        return Math.max(0, getDailyGoal() - getTodayCalories());
    }

    public static double getDailyProgress() {
        int goal = getDailyGoal();
        if (goal <= 0) {
            return 0.0;
        }
        return Math.min(1.0, (double) getTodayCalories() / goal);
    }

    public static String getLatestScanSummary() {
        List<HistoryEntry> history = HistoryManager.loadHistory();
        if (history.isEmpty()) {
            return "No saved bites yet. Start with a scan.";
        }

        HistoryEntry latest = history.get(history.size() - 1);
        int calories = latest.nutrition() == null ? 0 : latest.nutrition().getNutritionAsInt();
        return latest.foodName() + " saved at " + friendlyTime(latest.detectionTime()) + " (" + calories + " cal)";
    }

    public static List<CoachInsight> getDashboardInsights() {
        List<CoachInsight> insights = new ArrayList<>();
        int todayCalories = getTodayCalories();
        int remaining = getRemainingCalories();
        int protein = todayProtein();
        int sodium = todaySodium();

        if (protein < 45) {
            NutritionInfo pick = NutritionManager.getProteinPick(Math.max(remaining, 250));
            String detail = pick == null ? "Add a protein-forward bite to balance today's meals."
                    : "Try " + pick.name() + " for a protein lift within your remaining budget.";
            insights.add(new CoachInsight("Protein low", detail, "green"));
        }

        if (sodium > 1800) {
            insights.add(new CoachInsight("High sodium day", "Choose lower-sodium foods for the rest of today.", "coral"));
        }

        NutritionInfo light = NutritionManager.getLowestCalorieOption(Math.max(remaining, 120));
        if (light != null) {
            insights.add(new CoachInsight("Light snack idea", light.name() + " fits at about " + light.getNutritionAsInt() + " cal.", "mint"));
        }

        Map.Entry<String, Long> frequent = mostFrequentEntry();
        if (frequent != null && frequent.getValue() >= 2) {
            insights.add(new CoachInsight("Frequent food", frequent.getKey() + " appears " + frequent.getValue() + " times in your history.", "peach"));
        }

        if (todayCalories > getDailyGoal()) {
            insights.add(new CoachInsight("Over goal", "You are past today's goal. Keep the next bite light and hydrating.", "coral"));
        }

        while (insights.size() < 4) {
            insights.add(new CoachInsight("Steady start", "Scan a meal to unlock sharper local coaching.", "mint"));
        }
        return insights.subList(0, Math.min(4, insights.size()));
    }

    public static List<CoachInsight> getRecommendations() {
        List<CoachInsight> recommendations = new ArrayList<>(getDashboardInsights());
        int remaining = Math.max(getRemainingCalories(), 150);
        NutritionInfo snack = NutritionManager.getLowestCalorieOption(Math.min(remaining, 250));
        NutritionInfo protein = NutritionManager.getProteinPick(remaining);

        if (snack != null) {
            recommendations.add(new CoachInsight("Light snack", snack.name() + " is a calm pick at " + snack.getNutritionAsInt() + " cal.", "mint"));
        }
        if (protein != null) {
            recommendations.add(new CoachInsight("Protein pick", protein.name() + " brings " + protein.protein() + " protein.", "green"));
        }
        if (todaySodium() > 1800) {
            recommendations.add(new CoachInsight("Avoid more today", "Sodium is already high. Skip fries, cured meats, and salty sauces.", "coral"));
        }
        if (getTodayCalories() < getDailyGoal() * 0.55) {
            recommendations.add(new CoachInsight("Room left", "You still have " + getRemainingCalories() + " cal available today.", "peach"));
        }
        return recommendations;
    }

    public static Map<LocalDate, Integer> getSevenDayCalories() {
        Map<LocalDate, Integer> totals = new LinkedHashMap<>();
        LocalDate start = LocalDate.now().minusDays(6);
        for (int i = 0; i < 7; i++) {
            LocalDate day = start.plusDays(i);
            totals.put(day, caloriesForDate(day));
        }
        return totals;
    }

    public static int getAverageDailyCalories() {
        Map<LocalDate, Integer> totals = getSevenDayCalories();
        if (totals.isEmpty()) {
            return 0;
        }
        int sum = totals.values().stream().mapToInt(Integer::intValue).sum();
        return Math.round((float) sum / totals.size());
    }

    public static List<Map.Entry<String, Long>> getMostFrequentFoods(int limit) {
        return HistoryManager.loadHistory().stream()
                .collect(Collectors.groupingBy(HistoryEntry::foodName, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .toList();
    }

    public static String dayLabel(LocalDate day) {
        return day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US);
    }

    private static int caloriesForDate(LocalDate date) {
        int total = 0;
        for (HistoryEntry entry : HistoryManager.loadHistory()) {
            if (isSameDate(entry.detectionTime(), date) && entry.nutrition() != null) {
                double multiplier = entry.servingMultiplier() <= 0 ? 1.0 : entry.servingMultiplier();
                total += (int) Math.round(entry.nutrition().getNutritionAsInt() * multiplier);
            }
        }
        return total;
    }

    private static int todayProtein() {
        return HistoryManager.loadHistory().stream()
                .filter(entry -> isSameDate(entry.detectionTime(), LocalDate.now()) && entry.nutrition() != null)
                .mapToInt(entry -> NutritionManager.parseNumber(entry.nutrition().protein()))
                .sum();
    }

    private static int todaySodium() {
        return HistoryManager.loadHistory().stream()
                .filter(entry -> isSameDate(entry.detectionTime(), LocalDate.now()) && entry.nutrition() != null)
                .mapToInt(entry -> NutritionManager.parseNumber(entry.nutrition().sodium()))
                .sum();
    }

    private static Map.Entry<String, Long> mostFrequentEntry() {
        return getMostFrequentFoods(1).stream().findFirst().orElse(null);
    }

    private static boolean isSameDate(String detectionTime, LocalDate date) {
        try {
            return LocalDateTime.parse(detectionTime).toLocalDate().equals(date);
        } catch (Exception e) {
            return false;
        }
    }

    private static String friendlyTime(String detectionTime) {
        try {
            return LocalDateTime.parse(detectionTime).toLocalTime().withSecond(0).withNano(0).toString();
        } catch (Exception e) {
            return detectionTime;
        }
    }
}
