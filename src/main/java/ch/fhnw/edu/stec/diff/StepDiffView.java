package ch.fhnw.edu.stec.diff;

import ch.fhnw.edu.stec.model.StepDiffEntry;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.TextFlow;

public class StepDiffView extends ListView<StepDiffEntry> {

    private static final String STYLE_CLASS_DIFF = "diff";

    public StepDiffView(ObservableList<StepDiffEntry> entries) {
        super(entries);

        setCellFactory(lv -> new StepDiffEntryListCell());
        getStyleClass().add(STYLE_CLASS_DIFF);
    }

    private static final class StepDiffEntryListCell extends ListCell<StepDiffEntry> {

        private static final String STYLE_CLASS_PATH = "path";
        private static final String STYLE_CLASS_ADD = "add";
        private static final String STYLE_CLASS_DELETE = "delete";
        private static final String STYLE_CLASS_MODIFY = "modify";

        @Override
        protected void updateItem(StepDiffEntry entry, boolean empty) {
            super.updateItem(entry, empty);

            TextFlow textFlow = new TextFlow();
            setGraphic(textFlow);

            if (entry != null) {
                Insets insets = new Insets(0, 2, 0, 0);

                Label fileName = new Label(entry.getFile().getName());
                fileName.setPadding(insets);
                fileName.getStyleClass().add(styleClassByChangeType(entry.getFileChangeType()));

                Label filePath = new Label(entry.getFile().getParent());
                filePath.setPadding(insets);
                filePath.getStyleClass().add(STYLE_CLASS_PATH);

                textFlow.getChildren().addAll(fileName, filePath);
            }
        }

        private String styleClassByChangeType(StepDiffEntry.FileChangeType changeType) {
            switch (changeType) {
                case ADD:
                    return STYLE_CLASS_ADD;
                case DELETE:
                    return STYLE_CLASS_DELETE;
                default:
                    return STYLE_CLASS_MODIFY;
            }
        }

    }

}