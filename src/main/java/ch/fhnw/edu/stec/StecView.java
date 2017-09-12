package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.capture.StepCaptureView;
import ch.fhnw.edu.stec.chooser.GigChooserView;
import ch.fhnw.edu.stec.model.GigDir;
import ch.fhnw.edu.stec.status.GigStatusView;
import ch.fhnw.edu.stec.steps.StepTableView;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import static ch.fhnw.edu.stec.util.Labels.*;

final class StecView extends VBox {

    StecView(StecModel model, Window owner, StecController controller) {

        setPadding(new Insets(5));
        setSpacing(5);

        GigChooserView gigChooserView = new GigChooserView(model.gigDirProperty(), owner, controller);

        BooleanBinding gigReady = Bindings.createBooleanBinding(() -> (model.gigDirProperty().get() instanceof GigDir.ReadyGigDir), model.gigDirProperty());

        GigStatusView gigStatusView = new GigStatusView(model.gigDirProperty(), controller);
        StepCaptureView stepCaptureView = new StepCaptureView(controller);

        TitledPane gigPane = new TitledPane(GIG_SECTION_TITLE, new VBox(gigChooserView, gigStatusView));
        gigPane.setCollapsible(false);

        TitledPane stepsPane = new TitledPane(STEPS_SECTION_TITLE, new StepTableView(model.steps()));
        stepsPane.setCollapsible(false);
        stepsPane.disableProperty().bind(gigReady.not());

        TitledPane capturePane = new TitledPane(CAPTURE_SECTION_TITLE, new VBox(stepCaptureView));
        capturePane.setCollapsible(false);
        capturePane.disableProperty().bind(gigReady.not());


        getChildren().addAll(gigPane, stepsPane, capturePane);

    }

}