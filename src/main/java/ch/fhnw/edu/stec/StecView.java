package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.capture.StepCaptureView;
import ch.fhnw.edu.stec.chooser.GigChooserView;
import ch.fhnw.edu.stec.model.GigDir;
import ch.fhnw.edu.stec.status.GigStatusView;
import ch.fhnw.edu.stec.steps.StepTableView;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import static ch.fhnw.edu.stec.util.Labels.*;

final class StecView extends VBox {

    StecView(StecModel model, Window owner, StecController controller) {

        setPadding(new Insets(5));
        setSpacing(5);

        GigChooserView gigChooserView = new GigChooserView(model.gigDirProperty(), owner, controller);

        GigStatusView gigStatusView = new GigStatusView(model.gigDirProperty(), controller);
        StepCaptureView stepCaptureView = new StepCaptureView(controller, controller);
        StepTableView stepTableView = new StepTableView(model.getSteps());

        TitledPane gigPane = new TitledPane(GIG_SECTION_TITLE, new VBox(gigChooserView, gigStatusView));
        gigPane.setCollapsible(false);

        Tab stepCaptureTab = new Tab(TAB_TITLE_CAPTURE_STEP, stepCaptureView);
        stepCaptureTab.setClosable(false);

        Tab stepTableTab = new Tab(TAB_TITLE_EXISTING_STEPS, stepTableView);
        stepTableTab.setClosable(false);

        TabPane tabPane = new TabPane(stepCaptureTab, stepTableTab);

        BooleanBinding gigReady = Bindings.createBooleanBinding(() -> (model.gigDirProperty().get() instanceof GigDir.ReadyGigDir), model.gigDirProperty());
        tabPane.disableProperty().bind(gigReady.not());

        VBox.setVgrow(tabPane, Priority.ALWAYS);
        getChildren().addAll(gigPane, tabPane);

    }

}