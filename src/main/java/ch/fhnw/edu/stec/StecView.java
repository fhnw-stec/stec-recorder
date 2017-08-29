package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.chooser.GigChooserView;
import ch.fhnw.edu.stec.status.GigStatusView;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import static ch.fhnw.edu.stec.util.Labels.GIG_SECTION_TITLE;
import static ch.fhnw.edu.stec.util.Labels.SNAPSHOT_SECTION_TITLE;

final class StecView extends VBox {

    StecView(StecModel model, Window owner, StecController controller) {

        setPadding(new Insets(5));
        setSpacing(5);

        GigChooserView gigChooserView = new GigChooserView(model.gigDirectoryProperty(), owner, controller);
        GigStatusView gigStatusView = new GigStatusView(model.gigReady(), controller);

        TitledPane gigPane = new TitledPane(GIG_SECTION_TITLE, new VBox(gigChooserView, gigStatusView));
        gigPane.setCollapsible(false);

        TitledPane snapshotPane = new TitledPane(SNAPSHOT_SECTION_TITLE, new Group());
        snapshotPane.setCollapsible(false);
        snapshotPane.disableProperty().bind(model.gigReady().not());

        ListView<String> list = new ListView<>();
        model.snapshots_names().addListener((observable, oldValue, newValue) -> list.setItems(newValue));
        snapshotPane.setContent(list);


        getChildren().addAll(gigPane, snapshotPane);

    }

}