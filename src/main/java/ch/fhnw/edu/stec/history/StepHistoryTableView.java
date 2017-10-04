package ch.fhnw.edu.stec.history;

import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.notification.NotificationController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;

public final class StepHistoryTableView extends TableView<Step> {

    private static final String COLUMN_NAME_TAG = "Tag";
    private static final String COLUMN_NAME_TITLE = "Title";

    public StepHistoryTableView(ObservableList<Step> steps, StepHistoryController historyController, NotificationController notificationController) {
        super(new SortedList<>(steps, (s1, s2) -> steps.indexOf(s2) - steps.indexOf(s1))); // latest step at the top

        Callback<TableColumn<Step, String>, TableCell<Step, String>> cellFactory = (TableColumn<Step, String> column) -> new TableCell<Step, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    Step step = (Step) getTableRow().getItem();
                    Label label = new Label(item);
                    if (step != null && step.isHead()) {
                        // No bold with default font on macOS Sierra (https://bugs.openjdk.java.net/browse/JDK-8176835)
                        label.setFont(Font.font("Helvetica", FontWeight.BOLD, 13));
                    } else {
                        label.setFont(Font.font("Helvetica", FontWeight.NORMAL, 13));
                    }
                    setGraphic(label);
                }
            }
        };

        TableColumn<Step, String> tagColumn = new TableColumn<>(COLUMN_NAME_TAG);
        tagColumn.setCellValueFactory(step -> new SimpleStringProperty(step.getValue().getTag()));
        tagColumn.setCellFactory(cellFactory);

        TableColumn<Step, String> titleColumn = new TableColumn<>(COLUMN_NAME_TITLE);
        titleColumn.setCellValueFactory(step -> new SimpleStringProperty(step.getValue().getTitle()));
        titleColumn.setCellFactory(cellFactory);

        getColumns().add(tagColumn);
        getColumns().add(titleColumn);

        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        ContextMenu contextMenu = new StepHistoryContextMenu(
                () -> getSelectionModel().getSelectedItem(),
                historyController,
                notificationController);

        setContextMenu(contextMenu);
    }

}