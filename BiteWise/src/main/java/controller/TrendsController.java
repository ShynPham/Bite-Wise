package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import utility.CoachManager;
import utility.viewSwitcher;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class TrendsController {

    @FXML private Label averageCaloriesLabel;
    @FXML private Label frequentFoodsLabel;
    @FXML private VBox weeklyBarsBox;

    @FXML
    public void initialize() {
        averageCaloriesLabel.setText(CoachManager.getAverageDailyCalories() + " cal avg");
        List<Map.Entry<String, Long>> frequentFoods = CoachManager.getMostFrequentFoods(4);
        frequentFoodsLabel.setText(frequentFoods.isEmpty()
                ? "No foods saved yet."
                : frequentFoods.stream()
                .map(entry -> entry.getKey() + " x" + entry.getValue())
                .reduce((a, b) -> a + "  |  " + b)
                .orElse(""));
        buildWeeklyBars();
    }

    private void buildWeeklyBars() {
        weeklyBarsBox.getChildren().clear();
        Map<LocalDate, Integer> totals = CoachManager.getSevenDayCalories();
        int max = Math.max(1, totals.values().stream().mapToInt(Integer::intValue).max().orElse(1));

        for (Map.Entry<LocalDate, Integer> entry : totals.entrySet()) {
            Label dayLabel = new Label(CoachManager.dayLabel(entry.getKey()));
            dayLabel.getStyleClass().add("bar-day-label");
            dayLabel.setMinWidth(48);

            ProgressBar bar = new ProgressBar((double) entry.getValue() / max);
            bar.getStyleClass().add("calorie-progress");
            bar.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(bar, javafx.scene.layout.Priority.ALWAYS);

            Label totalLabel = new Label(entry.getValue() + " cal");
            totalLabel.getStyleClass().add("bar-value-label");
            totalLabel.setMinWidth(86);

            HBox row = new HBox(12, dayLabel, bar, totalLabel);
            row.getStyleClass().add("bar-row");
            weeklyBarsBox.getChildren().add(row);
        }
    }

    @FXML private void handleDashboardClick() { viewSwitcher.switchScene("coach-dashboard.fxml"); }
    @FXML private void handleSmartCoachClick() { viewSwitcher.switchScene("smart-coach.fxml"); }
    @FXML private void handleHistoryClick() { viewSwitcher.switchScene("bite-history.fxml"); }
    @FXML private void handleGoalsClick() { viewSwitcher.switchScene("goals-screen.fxml"); }
}
