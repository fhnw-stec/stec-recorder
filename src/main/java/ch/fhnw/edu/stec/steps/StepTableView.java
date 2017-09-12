package ch.fhnw.edu.stec.steps;

import ch.fhnw.edu.stec.model.Step;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public final class StepTableView extends TableView<Step> {

    private static final String COLUMN_NAME_TAG = "Tag";
    private static final String COLUMN_NAME_TITLE = "Title";

    public StepTableView(ObservableList<Step> steps) {
        super(steps);

        TableColumn<Step, String> tagColumn = new TableColumn<>(COLUMN_NAME_TAG);
        tagColumn.setCellValueFactory(step -> new SimpleStringProperty(step.getValue().getTag()));

        TableColumn<Step, String> titleColumn = new TableColumn<>(COLUMN_NAME_TITLE);
        titleColumn.setCellValueFactory(step -> new SimpleStringProperty(step.getValue().getTitle()));

        getColumns().add(tagColumn);
        getColumns().add(titleColumn);

        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

}