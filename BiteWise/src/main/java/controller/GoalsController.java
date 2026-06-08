package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import utility.CoachManager;
import utility.viewSwitcher;

import java.util.prefs.Preferences;

public class GoalsController {

    @FXML private Label currentGoalLabel;
    @FXML private TextField manualGoalField;
    @FXML private TextField weightField;
    @FXML private TextField heightField;
    @FXML private TextField ageField;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private ComboBox<String> activityComboBox;

    @FXML
    public void initialize() {
        genderComboBox.getItems().addAll("Male", "Female");
        genderComboBox.setValue("Male");
        activityComboBox.getItems().addAll("Rest day", "Light movement", "Active day");
        activityComboBox.setValue("Light movement");
        refreshGoalLabel();
    }

    @FXML
    private void handleSaveManualClick() {
        try {
            int goal = Integer.parseInt(manualGoalField.getText().trim());
            if (goal < 800 || goal > 6000) {
                showAlert(Alert.AlertType.ERROR, "Goal Range", "Choose a goal between 800 and 6000 calories.");
                return;
            }
            saveGoal(goal);
            showAlert(Alert.AlertType.INFORMATION, "Goal Saved", "Daily calorie goal updated to " + goal + " cal.");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Goal", "Enter a whole number calorie limit.");
        }
    }

    @FXML
    private void handleCalculateClick() {
        try {
            double weight = Double.parseDouble(weightField.getText().trim());
            double height = Double.parseDouble(heightField.getText().trim());
            int age = Integer.parseInt(ageField.getText().trim());
            boolean male = "Male".equals(genderComboBox.getValue());

            double bmr = male
                    ? 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)
                    : 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
            double activityMultiplier = switch (activityComboBox.getValue()) {
                case "Rest day" -> 1.2;
                case "Active day" -> 1.55;
                default -> 1.375;
            };
            int goal = (int) Math.round(bmr * activityMultiplier);
            saveGoal(goal);
            manualGoalField.setText(String.valueOf(goal));
            showAlert(Alert.AlertType.INFORMATION, "Goal Calculated", "Daily calorie goal updated to " + goal + " cal.");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Details", "Enter valid weight, height, and age values.");
        }
    }

    private void saveGoal(int goal) {
        Preferences.userRoot().node(CoachManager.PREF_NODE).putInt(CoachManager.CALORIES_LIMIT, goal);
        refreshGoalLabel();
    }

    private void refreshGoalLabel() {
        currentGoalLabel.setText(CoachManager.getDailyGoal() + " cal daily goal");
    }

    @FXML private void handleDashboardClick() { viewSwitcher.switchScene("coach-dashboard.fxml"); }
    @FXML private void handleHistoryClick() { viewSwitcher.switchScene("bite-history.fxml"); }
    @FXML private void handleSmartCoachClick() { viewSwitcher.switchScene("smart-coach.fxml"); }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
