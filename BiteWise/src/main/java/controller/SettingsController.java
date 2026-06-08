package controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import utility.viewSwitcher;

import java.util.Objects;
import java.util.Optional;
import java.util.prefs.Preferences;


public  class SettingsController {
    //user clicks logout button, navigates to the sign-inscreen screen

    @FXML
    private void handleLogOut(){viewSwitcher.switchScene("sign-in.fxml");}
    @FXML
    private void handleBiteHistoryClick(){viewSwitcher.switchScene("bite-history.fxml");}


    @FXML
    private void handleScanScreenClick(){viewSwitcher.switchScene("scan-screen.fxml");}

    @FXML
    private void handleAboutBiteWiseClick() { viewSwitcher.switchScene("about-screen.fxml");}

    @FXML
    private void handleDashboardClick(){viewSwitcher.switchScene("coach-dashboard.fxml");}

    @FXML
    private void handleSmartCoachClick(){viewSwitcher.switchScene("smart-coach.fxml");}

    @FXML
    private void handleTrendsClick(){viewSwitcher.switchScene("trends-screen.fxml");}

    @FXML
    private void handleGoalsClick(){viewSwitcher.switchScene("goals-screen.fxml");}

    @FXML
    private void handleMealReviewClick(){viewSwitcher.switchScene("meal-review.fxml");}



    @FXML
    private void handleRecentBite(){viewSwitcher.switchScene("bite-history.fxml");}

    private static final String PREF_NODE = "BiteWiseUsers";
    private static final String CALORIES_LIMIT = "dailyCalorieLimit";

    @FXML
    private void handleCalculationCLick(){
        viewSwitcher.switchScene("goals-screen.fxml");
    }

    private void openLegacyBmrDialog(){
        // 1. Create a custom dialog box
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("BiteWise Calculation");
        dialog.setHeaderText("Enter your personal details to calculated BMR");

        // Apply existing CSS to the dialog so it looks like the app
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/edu/utsa/cs3443/group7/bitewise/style.css")).toExternalForm());
        dialogPane.getStyleClass().add("bite-history-element"); // Use existing style for background

        // 2. Set the button types (Calculate vs Cancel)
        ButtonType loginButtonType = new ButtonType("Calculate", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // 3. Create the UI elements programmatically
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField weightField = new TextField();
        weightField.setPromptText("Weight (kg)");
        weightField.getStyleClass().add("textbox"); // Apply your CSS

        TextField heightField = new TextField();
        heightField.setPromptText("Height (cm)");
        heightField.getStyleClass().add("textbox");

        TextField ageField = new TextField();
        ageField.setPromptText("Age");
        ageField.getStyleClass().add("textbox");

        // Radio Buttons for Gender
        ToggleGroup group = new ToggleGroup();
        RadioButton maleRadio = new RadioButton("Male");
        maleRadio.setToggleGroup(group);
        maleRadio.setSelected(true);
        maleRadio.setStyle("-fx-font-family: 'KG Red Hands';"); // Quick inline font fix

        RadioButton femaleRadio = new RadioButton("Female");
        femaleRadio.setToggleGroup(group);
        femaleRadio.setStyle("-fx-font-family: 'KG Red Hands';");

        HBox genderBox = new HBox(10, maleRadio, femaleRadio);

        // Add everything to the grid
        grid.add(new Label("Weight:"), 0, 0);
        grid.add(weightField, 1, 0);
        grid.add(new Label("Height:"), 0, 1);
        grid.add(heightField, 1, 1);
        grid.add(new Label("Age:"), 0, 2);
        grid.add(ageField, 1, 2);
        grid.add(new Label("Gender:"), 0, 3);
        grid.add(genderBox, 1, 3);

        // Style the labels in the grid
        grid.getChildren().stream()
                .filter(node -> node instanceof Label)
                .forEach(node -> node.setStyle("-fx-font-family: 'KG Red Hands'; -fx-font-size: 14px;"));

        dialog.getDialogPane().setContent(grid);

        // 4. Wait for the user to close the dialog
        Optional<ButtonType> result = dialog.showAndWait();

        // 5. If they clicked "Calculate", run the logic
        if (result.isPresent() && result.get() == loginButtonType) {
            calculateAndSave(weightField.getText(), heightField.getText(), ageField.getText(), maleRadio.isSelected());
        }
    }

    private void calculateAndSave(String weightStr, String heightStr, String ageStr, boolean isMale) {
        try {
            double weight = Double.parseDouble(weightStr);
            double height = Double.parseDouble(heightStr);
            int age = Integer.parseInt(ageStr);

            double bmr;
            // Harris-Benedict Equation
            if (isMale) {
                bmr = 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age);
            } else {
                bmr = 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
            }

            // Multiply by 1.2 for sedentary lifestyle (default)
            int dailyLimit = (int) (bmr * 1.2);

            // Save to preferences
            Preferences prefs = Preferences.userRoot().node(PREF_NODE);
            prefs.putInt(CALORIES_LIMIT, dailyLimit);

            showAlert(Alert.AlertType.INFORMATION, "Goal Updated", "Your new daily limit is: " + dailyLimit + " cal");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers for all fields.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

}
