package ch.fhnw.edu.stec.history;

import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.notification.NotificationController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public final class StepHistoryTableView extends TableView<Step> {

    private static final String COLUMN_NAME_TAG = "Tag";
    private static final String COLUMN_NAME_TITLE = "Title";

    public StepHistoryTableView(ObservableList<Step> steps, StepHistoryController historyController, NotificationController notificationController) {
        super(steps);

        TableColumn<Step, String> tagColumn = new TableColumn<>(COLUMN_NAME_TAG);
        tagColumn.setCellValueFactory(step -> new SimpleStringProperty(step.getValue().getTag()));

        TableColumn<Step, String> titleColumn = new TableColumn<>(COLUMN_NAME_TITLE);
        titleColumn.setCellValueFactory(step -> new SimpleStringProperty(step.getValue().getTitle()));

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