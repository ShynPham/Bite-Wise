package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import model.CoachInsight;
import utility.CoachManager;
import utility.viewSwitcher;

import java.util.List;

public class SmartCoachController {

    @FXML private Label coachSummaryLabel;
    @FXML private Label recOneTitle;
    @FXML private Label recOneDetail;
    @FXML private Label recTwoTitle;
    @FXML private Label recTwoDetail;
    @FXML private Label recThreeTitle;
    @FXML private Label recThreeDetail;
    @FXML private Label recFourTitle;
    @FXML private Label recFourDetail;
    @FXML private Label recFiveTitle;
    @FXML private Label recFiveDetail;

    @FXML
    public void initialize() {
        coachSummaryLabel.setText("Today: " + CoachManager.getTodayCalories() + " cal eaten, "
                + CoachManager.getRemainingCalories() + " cal remaining.");
        List<CoachInsight> recommendations = CoachManager.getRecommendations();
        while (recommendations.size() < 5) {
            recommendations.add(new CoachInsight("Scan more", "More saved bites make the coach sharper.", "mint"));
        }
        setRecommendation(recommendations.get(0), recOneTitle, recOneDetail);
        setRecommendation(recommendations.get(1), recTwoTitle, recTwoDetail);
        setRecommendation(recommendations.get(2), recThreeTitle, recThreeDetail);
        setRecommendation(recommendations.get(3), recFourTitle, recFourDetail);
        setRecommendation(recommendations.get(4), recFiveTitle, recFiveDetail);
    }

    private void setRecommendation(CoachInsight insight, Label title, Label detail) {
        title.setText(insight.title());
        detail.setText(insight.detail());
    }

    @FXML private void handleDashboardClick() { viewSwitcher.switchScene("coach-dashboard.fxml"); }
    @FXML private void handleScanClick() { viewSwitcher.switchScene("scan-screen.fxml"); }
    @FXML private void handleTrendsClick() { viewSwitcher.switchScene("trends-screen.fxml"); }
    @FXML private void handleGoalsClick() { viewSwitcher.switchScene("goals-screen.fxml"); }
}
