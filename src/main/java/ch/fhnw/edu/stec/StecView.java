package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.capture.StepCaptureView;
import ch.fhnw.edu.stec.chooser.GigChooserView;
import ch.fhnw.edu.stec.status.GigStatusView;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.Map;

import static ch.fhnw.edu.stec.util.Labels.*;

final class StecView extends VBox {

    StecView(StecModel model, Window owner, StecController controller) {

        setPadding(new Insets(5));
        setSpacing(5);

        GigChooserView gigChooserView = new GigChooserView(model.gigDirProperty(), owner, controller);

        BooleanBinding gigReady = Bindings.createBooleanBinding(() -> (model.gigDirProperty().get() instanceof StecModel.ReadyGigDir), model.gigDirProperty());

        GigStatusView gigStatusView = new GigStatusView(model.gigDirProperty(), controller);
        StepCaptureView stepCaptureView = new StepCaptureView(controller);

        TitledPane gigPane = new TitledPane(GIG_SECTION_TITLE, new VBox(gigChooserView, gigStatusView));
        gigPane.setCollapsible(false);

        TitledPane snapshotPane = new TitledPane(SNAPSHOT_SECTION_TITLE, new Group());
        snapshotPane.setCollapsible(false);
        snapshotPane.disableProperty().bind(gigReady.not());

        ListView<String> list = new ListView<>();
        model.snapshots().addListener((arg0, oldVal, newValue) -> {
            ArrayList<String> stringList = new ArrayList<>();
            for (Map.Entry<String, String> entry : newValue.entrySet()) {
                stringList.add(entry.getKey() + ": " + entry.getValue());
            }
            list.setItems(FXCollections.observableArrayList(stringList));
        });
        snapshotPane.setContent(list);

        TitledPane capturePane = new TitledPane(CAPTURE_SECTION_TITLE, new VBox(stepCaptureView));
        capturePane.setCollapsible(false);
        capturePane.disableProperty().bind(gigReady.not());


        getChildren().addAll(gigPane, snapshotPane, capturePane);

    }

}