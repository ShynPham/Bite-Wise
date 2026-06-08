package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.HistoryEntry;
import model.NutritionInfo;
import utility.HistoryManager;
import utility.NutritionManager;
import utility.viewSwitcher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * @author Phu Pham
 */
public class BiteHistoryController {

    // --- UI Elements ---
    @FXML private Label totalCaloriesLabel;
    @FXML private Label calorieLimitLabel;
    @FXML private Label recommendationLabel;
    @FXML private Label progressPercentLabel;
    @FXML private ProgressBar calorieProgressBar;
    @FXML private ListView<HistoryEntry> historyListView;
    @FXML private TextArea detailsTextArea;

    // --- Data ---
    private ObservableList<HistoryEntry> historyList;
    private Preferences prefs;
    private int currentCalorieLimit;
    private static final String CALORIE_LIMIT_KEY = "dailyCalorieLimit";
    private static final int DEFAULT_CALORIE_LIMIT = 2000;

    @FXML
    public void initialize() {
        // 1. Setup Preferences
        prefs = Preferences.userRoot().node("BiteWiseUsers");

        // 2. Load and display the limit
        loadCalorieLimit();

        // 3. Load History Data
        List<HistoryEntry> loadedEntries = HistoryManager.loadHistory();
        historyList = FXCollections.observableArrayList(loadedEntries);
        historyListView.setItems(historyList);

        // 4. Calculate and display today's totals
        calculateDailyCalories();

        // 5. Setup Listener for Details
        historyListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        showDetails(newSelection);
                    } else {
                        detailsTextArea.clear();
                    }
                }
        );
    }

    private void loadCalorieLimit() {
        currentCalorieLimit = prefs.getInt(CALORIE_LIMIT_KEY, DEFAULT_CALORIE_LIMIT);
        calorieLimitLabel.setText(currentCalorieLimit + " cal");
        // Re-calculate highlighting whenever limit changes
        calculateDailyCalories();
    }

    /**
     * Calculates total calories for TODAY and updates the UI label.
     */
    private void calculateDailyCalories() {
        int todayCalories = 0;
        LocalDate today = LocalDate.now();

        System.out.println("--- STARTING CALORIE CALCULATION ---");
        System.out.println("Today's Date: " + today);

        if (historyList != null) {
            for (HistoryEntry entry : historyList) {
                try {
                    // 1. Check the raw date string
                    String rawTime = entry.detectionTime();
                    if (rawTime == null || rawTime.equals("Unknown")) {
                        System.out.println("Skipping entry: Date is unknown");
                        continue;
                    }

                    // 2. Parse the date
                    LocalDateTime entryTime = LocalDateTime.parse(rawTime);

                    // 3. Check if it matches today
                    if (entryTime.toLocalDate().isEqual(today)) {

                        // 4. Get calories
                        // NOTE: Make sure your NutritionInfo class has this method!
                        int cals = entry.nutrition().getNutritionAsInt();

                        System.out.println("Found entry for today: " + entry.foodName() + " (" + cals + " cal)");
                        todayCalories += cals;
                    } else {
                        System.out.println("Skipping entry: Wrong date (" + entryTime.toLocalDate() + ")");
                    }

                } catch (Exception e) {
                    System.err.println("Error calculating entry '" + entry.foodName() + "': " + e.getMessage());
                     e.printStackTrace(); // Uncomment to see full error if needed
                }
            }
        }

        System.out.println("Total Calculated: " + todayCalories);
        System.out.println("------------------------------------");

        // Update label
        totalCaloriesLabel.setText(todayCalories + " cal");
        updateProgress(todayCalories);
        // update recommendation
        generateRecommendation(todayCalories);
        // Highlight if over limit
        if (todayCalories > currentCalorieLimit) {
            totalCaloriesLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            totalCaloriesLabel.setStyle("-fx-text-fill: #6B8E4E; -fx-font-weight: bold;");
        }
    }

    private void updateProgress(int todayCalories) {
        double progress = currentCalorieLimit <= 0 ? 0 : (double) todayCalories / currentCalorieLimit;
        calorieProgressBar.setProgress(Math.min(progress, 1.0));
        progressPercentLabel.setText(Math.round(progress * 100) + "%");

        if (progress >= 1.0) {
            calorieProgressBar.getStyleClass().remove("calorie-progress");
            if (!calorieProgressBar.getStyleClass().contains("calorie-progress-over")) {
                calorieProgressBar.getStyleClass().add("calorie-progress-over");
            }
        } else {
            calorieProgressBar.getStyleClass().remove("calorie-progress-over");
            if (!calorieProgressBar.getStyleClass().contains("calorie-progress")) {
                calorieProgressBar.getStyleClass().add("calorie-progress");
            }
        }
    }

    private void showDetails(HistoryEntry entry) {
        String sb = entry.foodName().toUpperCase() + "\n" +
                "----------------------------\n" +
                entry.nutrition().toString();
        detailsTextArea.setText(sb);
    }

    @FXML
    private void onChangeLimitClick() {
        viewSwitcher.switchScene("goals-screen.fxml");
    }

    private void openLegacyLimitDialog() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(currentCalorieLimit));
        dialog.setTitle("Change Calorie Limit");
        dialog.setHeaderText("Set your daily goal.");
        dialog.setContentText("Limit (cal):");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(limitStr -> {
            try {
                int newLimit = Integer.parseInt(limitStr);
                if (newLimit > 0) {
                    prefs.putInt(CALORIE_LIMIT_KEY, newLimit);
                    loadCalorieLimit(); // Refresh UI
                } else {
                    showAlert("Please enter a positive number.");
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid number format.");
            }
        });
    }
    private void generateRecommendation(int currentCalories) {
        int remaining = currentCalorieLimit - currentCalories;

        if (remaining <= 0) {
            recommendationLabel.setText("You've hit your limit! Stay hydrated.");
            recommendationLabel.setStyle("-fx-text-fill: #d9534f;"); // Red color
        } else {
            // Ask our new Manager for a food that fits 'remaining'
            NutritionInfo suggestion = NutritionManager.getRecommendation(remaining);

            if (suggestion != null) {
                recommendationLabel.setText("Try having: " + suggestion.name() +
                        " (" + suggestion.getNutritionAsInt() + " cal)");
                recommendationLabel.setStyle("-fx-text-fill: #6B8E4E;"); // Green color
            } else {
                recommendationLabel.setText("You have " + remaining + " cal left. Maybe a light snack?");
            }
        }
    }
    @FXML
    private void onDeleteClick() {
        HistoryEntry selected = historyListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Bite");
            confirm.setHeaderText(null);
            confirm.setContentText("Delete " + selected.foodName() + " from your history?");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                historyList.remove(selected);
                detailsTextArea.clear();
                HistoryManager.saveHistory(new ArrayList<>(historyList));
                calculateDailyCalories(); // Re-calculate totals after deletion
            }
        }
    }

    @FXML
    private void handleBackClick() {
        viewSwitcher.switchScene("settings-screen.fxml");
    }

    private void showAlert(String s) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(s);
        alert.showAndWait();
    }
}
