package ch.fhnw.edu.stec.history;

import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.notification.NotificationController;
import ch.fhnw.edu.stec.util.Labels;
import io.vavr.collection.List;
import io.vavr.control.Try;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public final class StepHistoryDotView extends Region {

    private static final int PADDING_X = 5;
    private static final int DOT_RADIUS = 10;
    private static final int DOT_DEFAULT_SPACING = 3 * DOT_RADIUS;
    private static final Color DOT_DEFAULT_FILL = Color.web("#039ED366"); // official JavaFX blue (see modena.css)
    private static final Color DOT_HOVERED_FILL = Color.web("#039ED3");

    public StepHistoryDotView(ObservableList<Step> steps, StepHistoryController historyController, NotificationController notificationController) {
        setMinHeight(3 * DOT_RADIUS);

        steps.addListener((ListChangeListener<Step>) c -> {
            getChildren().clear();
            getChildren().addAll(createDots(steps, historyController, notificationController));
        });

        getChildren().addAll(createDots(steps, historyController, notificationController));

    }

    private java.util.List<Circle> createDots(ObservableList<Step> steps, StepHistoryController historyController, NotificationController notificationController) {

        DoubleBinding y = heightProperty().divide(2);
        DoubleBinding maxDx = widthProperty().subtract(2 * DOT_RADIUS).subtract(2 * PADDING_X).divide(steps.size() - 1);
        NumberBinding dx = Bindings.min(DOT_DEFAULT_SPACING, maxDx);

        return List.ofAll(steps).zipWithIndex().map(t -> {
            Step step = t._1;
            int stepIndex = t._2;
            NumberBinding x = dx.multiply(stepIndex).add(DOT_RADIUS).add(PADDING_X);
            Circle circle = new Circle(DOT_RADIUS, DOT_DEFAULT_FILL);
            circle.centerXProperty().bind(x);
            circle.centerYProperty().bind(y);

            if (step.isHead()) {
                circle.setStroke(Color.BLACK);
            }

            Tooltip tooltip = new Tooltip(step.getTitle());
            Tooltip.install(circle, tooltip);

            circle.setOnMouseEntered(e -> {
                circle.setFill(DOT_HOVERED_FILL);
                circle.setCursor(Cursor.HAND);
            });
            circle.setOnMouseExited(e -> {
                circle.setFill(DOT_DEFAULT_FILL);
                circle.setCursor(Cursor.DEFAULT);
            });

            ContextMenu contextMenu = new ContextMenu();
            MenuItem checkoutItem = new MenuItem("Checkout");
            checkoutItem.setOnAction(e -> {
                Try<String> result = historyController.checkoutStep(step.getTag());
                result.onSuccess(notificationController::notifyInfo);
                result.onFailure(error -> notificationController.notifyError(Labels.CHECKOUT_FAILED, error));
            });
            contextMenu.getItems().add(checkoutItem);

            circle.setOnContextMenuRequested(e -> contextMenu.show(circle, e.getScreenX(), e.getScreenY()));

            return circle;
        }).toJavaList();
    }

}
