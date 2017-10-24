package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.form.StepFormView;
import ch.fhnw.edu.stec.gig.GigChooserView;
import ch.fhnw.edu.stec.gig.GigStatusView;
import ch.fhnw.edu.stec.history.StepHistoryDotView;
import ch.fhnw.edu.stec.model.GigDir;
import ch.fhnw.edu.stec.util.Glyphs;
import ch.fhnw.edu.stec.util.Labels;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.controlsfx.control.StatusBar;
import org.controlsfx.glyphfont.FontAwesome;

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

        StatusBar statusBar = createStatusBar(model);

        getChildren().addAll(gigPane, stepsPane, statusBar);

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

    private static TitledPane createStepFormPane(StecModel model, StecController controller) {
        StepFormView stepFormView = new StepFormView(model, controller, controller);
        stepFormView.setMinHeight(0);
        TitledPane stepFormPane = new TitledPane(STEP_FORM_SECTION_TITLE, stepFormView);

        stepFormPane.setMaxHeight(Double.MAX_VALUE);
        stepFormPane.setCollapsible(false);
        return stepFormPane;
    }

    private static VBox createStepHistoryPane(StecModel model, StecController controller) {
        StepHistoryDotView stepHistoryDotView = new StepHistoryDotView(model, controller, controller);

        TitledPane stepHistoryPane = new TitledPane(STEP_HISTORY_SECTION_TITLE, stepHistoryDotView);
        stepHistoryPane.setMaxHeight(Double.MAX_VALUE);
        stepHistoryPane.setCollapsible(false);

        Button refreshButton = new Button("", Glyphs.FONT_AWESOME.create(FontAwesome.Glyph.REFRESH));
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
        TitledPane stepCapturePane = createStepFormPane(model, controller);
        Pane stepHistoryPane = createStepHistoryPane(model, controller);

        SplitPane stepsSplitPane = new SplitPane(stepCapturePane, stepHistoryPane);
        stepsSplitPane.setOrientation(Orientation.VERTICAL);
        BooleanBinding gigReady = Bindings.createBooleanBinding(() -> (model.gigDirProperty().get() instanceof GigDir.ReadyGigDir), model.gigDirProperty());
        stepsSplitPane.disableProperty().bind(gigReady.not());
        stepsSplitPane.setDividerPositions(0.85);

        return stepsSplitPane;
    }

    private static StatusBar createStatusBar(StecModel model) {
        StatusBar statusBar = new StatusBar();
        statusBar.setText(""); // purge the default of showing OK
        Bindings.bindContentBidirectional(statusBar.getLeftItems(), model.getStatusBarLeftItems());
        return statusBar;
    }

}