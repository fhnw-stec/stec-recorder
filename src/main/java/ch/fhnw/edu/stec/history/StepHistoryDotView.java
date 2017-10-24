package ch.fhnw.edu.stec.history;

import ch.fhnw.edu.stec.model.InteractionMode;
import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.notification.NotificationController;
import ch.fhnw.edu.stec.util.DotPlus;
import ch.fhnw.edu.stec.util.Labels;
import io.vavr.collection.List;
import io.vavr.control.Try;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.collections.ListChangeListener;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.Collections;

public final class StepHistoryDotView extends Region {

    private static final int PADDING_X = 5;
    private static final int DOT_RADIUS = 15;
    private static final int DOT_DEFAULT_SPACING = 3 * DOT_RADIUS;
    private static final Color DOT_DEFAULT_FILL = Color.web("#039ED366"); // official JavaFX blue (see modena.css)
    private static final Color DOT_BEING_EDITED_STROKE = Color.BLACK;
    private static final Color DOT_DEFAULT_STROKE = Color.TRANSPARENT;
    private static final double SLIGHTLY_VISIBLE = 0.15;

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
                if (e.getButton().equals(MouseButton.PRIMARY)) {
                    Try<String> result = historyController.switchToEditMode(step.getTag());
                    result.onSuccess(notificationController::notifyInfo);
                    result.onFailure(error -> notificationController.notifyError(Labels.CHECKOUT_FAILED, error));
                }
            });

            ContextMenu contextMenu = new StepHistoryContextMenu(() -> step, historyController, notificationController);
            circle.setOnContextMenuRequested(e -> contextMenu.show(circle, e.getScreenX(), e.getScreenY()));

            return circle;
        });
    }

    private static Node createPlusDot(StepHistoryController historyController, NotificationController notificationController, DoubleBinding y, NumberBinding dx, int existingStepCount) {
        DotPlus dotPlus = createUpcomingStepDot(y, dx, existingStepCount);

        // use opacity instead of visibility to keep mouse interactivity
        dotPlus.setOpacity(SLIGHTLY_VISIBLE);
        dotPlus.setOnMouseEntered(e -> dotPlus.setOpacity(1));
        dotPlus.setOnMouseExited(e -> dotPlus.setOpacity(SLIGHTLY_VISIBLE));

        dotPlus.setOnMouseClicked(e -> {
            Try<String> result = historyController.switchToCaptureMode();
            result.onSuccess(notificationController::notifyInfo);
            result.onFailure(error -> notificationController.notifyError(Labels.ENTERING_CAPTURE_MODE_FAILED, error));
        });

        return dotPlus;
    }

    private static DotPlus createUpcomingStepDot(DoubleBinding y, NumberBinding dx, int existingStepCount) {
        NumberBinding x = dx.multiply(existingStepCount).add(DOT_RADIUS).add(PADDING_X);
        DotPlus dotPlus = new DotPlus(DOT_RADIUS);
        dotPlus.translateXProperty().bind(x);
        dotPlus.translateYProperty().bind(y);
        return dotPlus;
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
