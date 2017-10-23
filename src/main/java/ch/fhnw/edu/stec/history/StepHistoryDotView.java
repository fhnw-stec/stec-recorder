package ch.fhnw.edu.stec.history;

import ch.fhnw.edu.stec.model.InteractionMode;
import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.notification.NotificationController;
import ch.fhnw.edu.stec.util.Glyphs;
import ch.fhnw.edu.stec.util.Labels;
import io.vavr.collection.List;
import io.vavr.control.Try;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.collections.ListChangeListener;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.controlsfx.glyphfont.Glyph;

import java.util.Collections;

public final class StepHistoryDotView extends Region {

    private static final int PADDING_X = 5;
    private static final int DOT_RADIUS = 10;
    private static final int DOT_DEFAULT_SPACING = 3 * DOT_RADIUS;
    private static final Color DOT_DEFAULT_FILL = Color.web("#039ED366"); // official JavaFX blue (see modena.css)
    private static final Color DOT_BEING_EDITED_STROKE = Color.BLACK;
    private static final Color DOT_DEFAULT_STROKE = Color.TRANSPARENT;
    private static final Color DOT_UPCOMPING_FILL = Color.TRANSPARENT;
    private static final double SLIGHTLY_VISIBLE = 0.2;

    public StepHistoryDotView(StepHistoryModel model, StepHistoryController historyController, NotificationController notificationController) {
        setMinHeight(3 * DOT_RADIUS);

        model.getSteps().addListener((ListChangeListener<Step>) c -> {
            getChildren().clear();
            getChildren().addAll(createDots(model, historyController, notificationController));
        });

        getChildren().addAll(createDots(model, historyController, notificationController));

    }

    private static List<Node> createDotsForExistingSteps(StepHistoryModel model, StepHistoryController historyController, NotificationController notificationController, DoubleBinding y, NumberBinding dx) {
        return List.ofAll(model.getSteps()).zipWithIndex().map(t -> {
            Step step = t._1;
            int stepIndex = t._2;
            NumberBinding x = dx.multiply(stepIndex).add(DOT_RADIUS).add(PADDING_X);

            Circle circle = new Circle(DOT_RADIUS, DOT_DEFAULT_FILL);
            circle.centerXProperty().bind(x);
            circle.centerYProperty().bind(y);
            circle.setCursor(Cursor.HAND);

            Tooltip tooltip = new Tooltip(step.getTitle());
            Tooltip.install(circle, tooltip);

            updateStroke(circle, model.isBeingEdited(step));

            circle.setOnMouseEntered(e -> circle.setStroke(DOT_BEING_EDITED_STROKE));
            circle.setOnMouseExited(e -> updateStroke(circle, model.isBeingEdited(step)));

            circle.setOnMouseClicked(e -> {
                Try<String> result = historyController.switchToEditMode(step);
                result.onSuccess(notificationController::notifyInfo);
                result.onFailure(error -> notificationController.notifyError(Labels.CHECKOUT_FAILED, error));
            });

            ContextMenu contextMenu = new StepHistoryContextMenu(() -> step, historyController, notificationController);
            circle.setOnContextMenuRequested(e -> contextMenu.show(circle, e.getScreenX(), e.getScreenY()));

            return circle;
        });
    }

    private static Node createPlusDot(StepHistoryController historyController, NotificationController notificationController, DoubleBinding y, NumberBinding dx, int existingStepCount) {
        NumberBinding x = dx.multiply(existingStepCount).add(DOT_RADIUS).add(PADDING_X);

        Glyph plus = Glyphs.PLUS;
        plus.translateXProperty().bind(x.subtract(plus.widthProperty().divide(2)));
        plus.translateYProperty().bind(y.subtract(plus.heightProperty().divide(2)));

        Circle circle = createUpcomingStepDot(y, dx, existingStepCount);

        Group group = new Group(plus, circle);
        group.setCursor(Cursor.HAND);

        // use opacity instead of visibility to keep mouse interactivity
        group.setOpacity(SLIGHTLY_VISIBLE);
        group.setOnMouseEntered(e -> group.setOpacity(1));
        group.setOnMouseExited(e -> group.setOpacity(SLIGHTLY_VISIBLE));

        group.setOnMouseClicked(e -> {
            Try<String> result = historyController.switchToCaptureMode();
            result.onSuccess(notificationController::notifyInfo);
            result.onFailure(error -> notificationController.notifyError(Labels.ENTERING_CAPTURE_MODE_FAILED, error));
        });

        return group;
    }

    private static Circle createUpcomingStepDot(DoubleBinding y, NumberBinding dx, int existingStepCount) {
        NumberBinding x = dx.multiply(existingStepCount).add(DOT_RADIUS).add(PADDING_X);

        Circle circle = new Circle(DOT_RADIUS, DOT_UPCOMPING_FILL);
        circle.setStroke(DOT_BEING_EDITED_STROKE);
        circle.centerXProperty().bind(x);
        circle.centerYProperty().bind(y);
        circle.setCursor(Cursor.HAND);

        return circle;
    }

    private static void updateStroke(Circle circle, boolean isBeingEdited) {
        if (isBeingEdited) {
            circle.setStroke(DOT_BEING_EDITED_STROKE);
        } else {
            circle.setStroke(DOT_DEFAULT_STROKE);
        }

    }

    private java.util.List<? extends Node> createDots(StepHistoryModel model, StepHistoryController historyController, NotificationController notificationController) {

        DoubleBinding y = heightProperty().divide(2);
        DoubleBinding maxDx = widthProperty().subtract(2 * DOT_RADIUS).subtract(2 * PADDING_X).divide(model.getSteps().size() - 1);
        NumberBinding dx = Bindings.min(DOT_DEFAULT_SPACING, maxDx);

        List<Node> existingSteps = createDotsForExistingSteps(model, historyController, notificationController, y, dx);

        if (!existingSteps.isEmpty()) {
            if (model.interactionModeProperty().get() instanceof InteractionMode.Capture) {
                Node upcomingStep = createUpcomingStepDot(y, dx, existingSteps.length());
                return existingSteps.append(upcomingStep).toJavaList();
            } else {
                Node plus = createPlusDot(historyController, notificationController, y, dx, existingSteps.length());
                return existingSteps.append(plus).toJavaList();
            }
        } else {
            return Collections.emptyList();
        }
    }

}
