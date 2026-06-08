package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import model.CoachInsight;
import utility.CoachManager;
import utility.MealDraftManager;
import utility.viewSwitcher;

import java.util.List;

public class CoachDashboardController {

    @FXML private Label todayCaloriesLabel;
    @FXML private Label goalLabel;
    @FXML private Label remainingLabel;
    @FXML private Label latestScanLabel;
    @FXML private Label insightOneTitle;
    @FXML private Label insightOneDetail;
    @FXML private Label insightTwoTitle;
    @FXML private Label insightTwoDetail;
    @FXML private Label insightThreeTitle;
    @FXML private Label insightThreeDetail;
    @FXML private Label insightFourTitle;
    @FXML private Label insightFourDetail;
    @FXML private ProgressBar calorieProgressBar;

    @FXML
    public void initialize() {
        int today = CoachManager.getTodayCalories();
        int goal = CoachManager.getDailyGoal();
        int remaining = CoachManager.getRemainingCalories();

        todayCaloriesLabel.setText(today + " cal");
        goalLabel.setText(goal + " cal goal");
        remainingLabel.setText(remaining + " cal left");
        latestScanLabel.setText(CoachManager.getLatestScanSummary());
        calorieProgressBar.setProgress(CoachManager.getDailyProgress());
        calorieProgressBar.getStyleClass().removeAll("calorie-progress", "calorie-progress-over");
        calorieProgressBar.getStyleClass().add(today > goal ? "calorie-progress-over" : "calorie-progress");

        List<CoachInsight> insights = CoachManager.getDashboardInsights();
        setInsight(insights.get(0), insightOneTitle, insightOneDetail);
        setInsight(insights.get(1), insightTwoTitle, insightTwoDetail);
        setInsight(insights.get(2), insightThreeTitle, insightThreeDetail);
        setInsight(insights.get(3), insightFourTitle, insightFourDetail);
    }

    private void setInsight(CoachInsight insight, Label title, Label detail) {
        title.setText(insight.title());
        detail.setText(insight.detail());
    }

    @FXML private void handleScanClick() { viewSwitcher.switchScene("scan-screen.fxml"); }
    @FXML private void handleMealReviewClick() { viewSwitcher.switchScene("meal-review.fxml"); }
    @FXML private void handleSmartCoachClick() { viewSwitcher.switchScene("smart-coach.fxml"); }
    @FXML private void handleTrendsClick() { viewSwitcher.switchScene("trends-screen.fxml"); }
    @FXML private void handleGoalsClick() { viewSwitcher.switchScene("goals-screen.fxml"); }
    @FXML private void handleHistoryClick() { viewSwitcher.switchScene("bite-history.fxml"); }
    @FXML private void handleSettingsClick() { viewSwitcher.switchScene("settings-screen.fxml"); }
    @FXML private void handleClearDraftClick() {
        MealDraftManager.clear();
        latestScanLabel.setText("Meal draft cleared. Start a fresh scan when ready.");
    }
}
