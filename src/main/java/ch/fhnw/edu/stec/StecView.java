package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.capture.StepCaptureView;
import ch.fhnw.edu.stec.history.StepHistoryDotView;
import ch.fhnw.edu.stec.gig.GigChooserView;
import ch.fhnw.edu.stec.gig.GigStatusView;
import ch.fhnw.edu.stec.model.GigDir;
import ch.fhnw.edu.stec.history.StepHistoryTableView;
import ch.fhnw.edu.stec.util.Labels;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import static ch.fhnw.edu.stec.util.Labels.*;

final class StecView extends VBox {

    private static final String IDLE_REFRESH_BUTTON_STYLE = "-fx-background-color: transparent;";
    private static final String HOVERED_REFRESH_BUTTON_STYLE = "-fx-background-color: -fx-shadow-highlight-color, -fx-outer-border, -fx-inner-border, -fx-body-color;"; // from official JavaFX modena.css

    StecView(StecModel model, Window owner, StecController controller) {

        setPadding(new Insets(5));
        setSpacing(5);

        TitledPane gigPane = createGitPane(model, owner, controller);
        SplitPane stepsPane = createStepsPane(model, controller);

        VBox.setVgrow(stepsPane, Priority.ALWAYS);

        getChildren().addAll(gigPane, stepsPane);

    }

    private static TitledPane createGitPane(StecModel model, Window owner, StecController controller) {
        GigChooserView gigChooserView = new GigChooserView(model.gigDirProperty(), owner, controller);
        GigStatusView gigStatusView = new GigStatusView(model.gigDirProperty(), controller);
        VBox gigSectionContent = new VBox(5, gigChooserView, gigStatusView);
        gigSectionContent.setPadding(new Insets(5));
        TitledPane gigPane = new TitledPane(GIG_SECTION_TITLE, gigSectionContent);
        gigPane.setCollapsible(false);
        return gigPane;
    }

    private static TitledPane createStepCapturePane(StecController controller) {
        StepCaptureView stepCaptureView = new StepCaptureView(controller, controller);
        stepCaptureView.setMinHeight(0);
        TitledPane stepCapturePane = new TitledPane(STEP_CAPTURE_SECTION_TITLE, stepCaptureView);
        stepCapturePane.setMaxHeight(Double.MAX_VALUE);
        stepCapturePane.setCollapsible(false);
        return stepCapturePane;
    }

    private static VBox createStepHistoryPane(StecModel model, StecController controller) {
        StepHistoryDotView stepHistoryDotView = new StepHistoryDotView(model.getSteps(), controller, controller);
        Tab dotViewTab = new Tab(DOT_VIEW_TAB_TITLE, stepHistoryDotView);
        dotViewTab.setClosable(false);

        StepHistoryTableView stepHistoryTableView = new StepHistoryTableView(model.getSteps(), controller, controller);
        Tab stepTableTab = new Tab(STEP_TABLE_TAB_TITLE, stepHistoryTableView);
        stepTableTab.setClosable(false);

        TabPane tabPane = new TabPane(dotViewTab, stepTableTab);

        TitledPane stepHistoryPane = new TitledPane(STEP_HISTORY_SECTION_TITLE, tabPane);
        stepHistoryPane.setMaxHeight(Double.MAX_VALUE);
        stepHistoryPane.setCollapsible(false);

        GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
        Button refreshButton = new Button("", fontAwesome.create(FontAwesome.Glyph.REFRESH));
        refreshButton.setTooltip(new Tooltip(Labels.REFRESH_BUTTON_TOOLTIP));
        refreshButton.setOnAction(e -> controller.refresh());

        // tweak button appearance to be more lightweight, only showing border if hovered
        refreshButton.setStyle(IDLE_REFRESH_BUTTON_STYLE);
        refreshButton.setOnMouseEntered(mouseEvent -> refreshButton.setStyle(HOVERED_REFRESH_BUTTON_STYLE));
        refreshButton.setOnMouseExited(mouseEvent -> refreshButton.setStyle(IDLE_REFRESH_BUTTON_STYLE));

        HBox refreshButtonPane = new HBox(refreshButton);
        refreshButtonPane.setPadding(new Insets(5));
        refreshButtonPane.setAlignment(Pos.CENTER_RIGHT);

        VBox pane = new VBox(stepHistoryPane, refreshButtonPane);
        VBox.setVgrow(stepHistoryPane, Priority.ALWAYS);
        VBox.setVgrow(refreshButtonPane, Priority.NEVER);

        return pane;
    }

    private static SplitPane createStepsPane(StecModel model, StecController controller) {
        TitledPane stepCapturePane = createStepCapturePane(controller);
        Pane stepHistoryPane = createStepHistoryPane(model, controller);

        SplitPane stepsSplitPane = new SplitPane(stepCapturePane, stepHistoryPane);
        stepsSplitPane.setOrientation(Orientation.VERTICAL);
        BooleanBinding gigReady = Bindings.createBooleanBinding(() -> (model.gigDirProperty().get() instanceof GigDir.ReadyGigDir), model.gigDirProperty());
        stepsSplitPane.disableProperty().bind(gigReady.not());
        stepsSplitPane.setDividerPositions(0.8);

        return stepsSplitPane;
    }
}