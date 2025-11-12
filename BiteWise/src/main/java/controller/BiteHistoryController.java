package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import model.HistoryEntry;
import utility.HistoryManager;
import utility.viewSwitcher;

import java.util.ArrayList;
import java.util.List;

public class BiteHistoryController {

    @FXML private ListView<HistoryEntry> historyListView;
    @FXML private TextArea detailsTextArea;

    @FXML private VBox historyViewerContainer;

    private ObservableList<HistoryEntry> historyList; // A list that the ListView can "watch"

    @FXML
    public void initialize() {
        // 1. Load the history from the file
        List<HistoryEntry> loadedEntries = HistoryManager.loadHistory();

        // 2. Put them in an ObservableList
        historyList = FXCollections.observableArrayList(loadedEntries);

        // 3. Set the items in the ListView
        historyListView.setItems(historyList);

        // 4. Add a listener to show details when an item is clicked
        historyListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        showDetails(newSelection);
                    } else {
                        detailsTextArea.clear();
                    }
                }
        );
    }

    // Shows the full details of the selected item in the text area
    private void showDetails(HistoryEntry entry) {
        StringBuilder sb = new StringBuilder();
        sb.append(entry.foodName().toUpperCase()).append("\n");
        sb.append("----------------------------\n");
        sb.append(entry.nutrition().toString());
        detailsTextArea.setText(sb.toString());
    }

    @FXML
    private void handleBackClick() {
        viewSwitcher.switchScene("scan-screen.fxml");
    }

    @FXML
    private void onDeleteClick() {
        // Get the selected item
        HistoryEntry selected = historyListView.getSelectionModel().getSelectedItem();

        if (selected != null) {
            // 1. Remove it from the list in memory
            historyList.remove(selected);

            // 2. Save the new, shorter list to the file
            HistoryManager.saveHistory(new ArrayList<>(historyList));
        }
    }
}