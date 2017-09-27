package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.capture.StepCaptureView;
import ch.fhnw.edu.stec.chooser.GigChooserView;
import ch.fhnw.edu.stec.dot.DotView;
import ch.fhnw.edu.stec.model.GigDir;
import ch.fhnw.edu.stec.status.GigStatusView;
import ch.fhnw.edu.stec.steps.StepTableView;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
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
        VBox gigSectionContent = new VBox(5, gigChooserView, gigStatusView);
        gigSectionContent.setPadding(new Insets(5));
        TitledPane gigPane = new TitledPane(GIG_SECTION_TITLE, gigSectionContent);
        gigPane.setCollapsible(false);

        StepCaptureView stepCaptureView = new StepCaptureView(controller, controller);
        stepCaptureView.setMinHeight(0);
        TitledPane stepCapturePane = new TitledPane(STEP_CAPTURE_SECTION_TITLE, stepCaptureView);
        stepCapturePane.setMaxHeight(Double.MAX_VALUE);
        stepCapturePane.setCollapsible(false);

        DotView dotView = new DotView(model.getSteps());
        Tab dotViewTab = new Tab(DOT_VIEW_TAB_TITLE, dotView);
        dotViewTab.setClosable(false);

        StepTableView stepTableView = new StepTableView(model.getSteps());
        Tab stepTableTab = new Tab(STEP_TABLE_TAB_TITLE, stepTableView);
        stepTableTab.setClosable(false);

        TabPane tabPane = new TabPane(dotViewTab, stepTableTab);
        TitledPane existingStepsPane = new TitledPane(EXISTING_STEPS_SECTION_TITLE, tabPane);
        existingStepsPane.setMaxHeight(Double.MAX_VALUE);
        existingStepsPane.setCollapsible(false);

        SplitPane stepsSplitPane = new SplitPane(stepCapturePane, existingStepsPane);
        stepsSplitPane.setOrientation(Orientation.VERTICAL);
        BooleanBinding gigReady = Bindings.createBooleanBinding(() -> (model.gigDirProperty().get() instanceof GigDir.ReadyGigDir), model.gigDirProperty());
        stepsSplitPane.disableProperty().bind(gigReady.not());

        VBox.setVgrow(stepsSplitPane, Priority.ALWAYS);
        getChildren().addAll(gigPane, stepsSplitPane);

    }

}