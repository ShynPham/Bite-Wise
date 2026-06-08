/**
 *  Analytics screen controller, displays daily calories,
 *  and a barchart of weekly calorie intake...
 */

package controller;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import model.HistoryEntry;
import utility.HistoryManager;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class AnalyticsController {

    @FXML
    private HBox dailyCalBox;

    @FXML
    private Label lblTodayCalories;

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    public void initialize() {
        if (lblTodayCalories == null) {
            lblTodayCalories = new Label("0 cal");
            lblTodayCalories.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2e8b57;");
            dailyCalBox.setSpacing(10);
            dailyCalBox.getChildren().add(lblTodayCalories);
        }

        updateAnalytics();
    }

        /**
         * loads history from json and sorts.
         */
        private void updateAnalytics() {


            }


    @FXML
    private void handleRefresh() {
        updateAnalytics();
    }
    }


