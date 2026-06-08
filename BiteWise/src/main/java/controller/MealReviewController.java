package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import model.MealDraftItem;
import model.NutritionInfo;
import utility.MealDraftManager;
import utility.NutritionManager;
import utility.viewSwitcher;

import java.util.List;

public class MealReviewController {

    @FXML private Label draftStatusLabel;
    @FXML private Label totalCaloriesLabel;
    @FXML private Label selectedFoodLabel;
    @FXML private ListView<MealDraftItem> mealListView;
    @FXML private ComboBox<String> foodComboBox;
    @FXML private ComboBox<String> servingComboBox;
    @FXML private TextArea nutritionTextArea;

    @FXML
    public void initialize() {
        foodComboBox.setItems(FXCollections.observableArrayList(
                NutritionManager.getAllFood().stream().map(NutritionInfo::name).sorted().toList()
        ));
        servingComboBox.setItems(FXCollections.observableArrayList("0.5x", "1x", "1.5x", "2x"));
        servingComboBox.setValue("1x");
        mealListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> showSelectedItem());
        refresh();
    }

    private void refresh() {
        List<MealDraftItem> items = MealDraftManager.getDraft();
        mealListView.setItems(FXCollections.observableArrayList(items));
        draftStatusLabel.setText(items.isEmpty()
                ? "No unsaved meal yet. Run a scan to review detections here."
                : items.size() + " item meal draft ready for review.");
        totalCaloriesLabel.setText(MealDraftManager.getTotalCalories() + " cal");
        if (!items.isEmpty() && mealListView.getSelectionModel().getSelectedIndex() < 0) {
            mealListView.getSelectionModel().select(0);
        }
        showSelectedItem();
    }

    private void showSelectedItem() {
        MealDraftItem item = mealListView.getSelectionModel().getSelectedItem();
        if (item == null) {
            selectedFoodLabel.setText("Select a food to edit");
            nutritionTextArea.setText("Detected foods, nutrition, confidence, and serving edits will appear here.");
            return;
        }
        selectedFoodLabel.setText(item.foodName() + " at " + Math.round(item.confidence() * 100) + "% confidence");
        foodComboBox.setValue(item.foodName());
        servingComboBox.setValue(formatMultiplier(item.servingMultiplier()));
        nutritionTextArea.setText("Serving: " + item.servingMultiplier() + "x\n"
                + "Meal calories from item: " + item.caloriesWithServing() + "\n\n"
                + (item.nutrition() == null ? "No nutrition data available." : item.nutrition().toString()));
    }

    @FXML
    private void handleApplyFoodClick() {
        int index = mealListView.getSelectionModel().getSelectedIndex();
        String selectedFood = foodComboBox.getValue();
        NutritionInfo nutrition = NutritionManager.findByName(selectedFood);
        if (index >= 0 && nutrition != null) {
            MealDraftManager.rename(index, selectedFood, nutrition);
            refresh();
            mealListView.getSelectionModel().select(index);
        }
    }

    @FXML
    private void handleApplyServingClick() {
        int index = mealListView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            MealDraftManager.setServingMultiplier(index, parseMultiplier(servingComboBox.getValue()));
            refresh();
            mealListView.getSelectionModel().select(index);
        }
    }

    @FXML
    private void handleRemoveClick() {
        int index = mealListView.getSelectionModel().getSelectedIndex();
        MealDraftManager.remove(index);
        refresh();
    }

    @FXML
    private void handleSaveMealClick() {
        int saved = MealDraftManager.saveDraftToHistory();
        if (saved <= 0) {
            showAlert(Alert.AlertType.WARNING, "Nothing to Save", "Scan a meal first, then review it here.");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Meal Saved", saved + " item(s) added to Bite History.");
        viewSwitcher.switchScene("coach-dashboard.fxml");
    }

    @FXML private void handleScanClick() { viewSwitcher.switchScene("scan-screen.fxml"); }
    @FXML private void handleDashboardClick() { viewSwitcher.switchScene("coach-dashboard.fxml"); }
    @FXML private void handleHistoryClick() { viewSwitcher.switchScene("bite-history.fxml"); }

    private double parseMultiplier(String value) {
        if (value == null) {
            return 1.0;
        }
        return switch (value) {
            case "0.5x" -> 0.5;
            case "1.5x" -> 1.5;
            case "2x" -> 2.0;
            default -> 1.0;
        };
    }

    private String formatMultiplier(double multiplier) {
        if (multiplier == 0.5) return "0.5x";
        if (multiplier == 1.5) return "1.5x";
        if (multiplier == 2.0) return "2x";
        return "1x";
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
