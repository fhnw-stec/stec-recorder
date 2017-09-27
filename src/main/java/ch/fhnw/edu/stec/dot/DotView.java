package ch.fhnw.edu.stec.dot;

import ch.fhnw.edu.stec.model.Step;
import io.vavr.collection.List;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public final class DotView extends Region {

    private static final int PADDING_X = 5;
    private static final int DOT_RADIUS = 10;
    private static final int DOT_DEFAULT_SPACING = 3 * DOT_RADIUS;
    private static final Color DOT_DEFAULT_FILL = Color.web("#039ED366"); // official JavaFX blue (see modena.css)
    private static final Color DOT_HOVERED_FILL = Color.web("#039ED3");

    public DotView(ObservableList<Step> steps) {
        setMinHeight(3 * DOT_RADIUS);

        steps.addListener((ListChangeListener<Step>) c -> {
            getChildren().clear();
            getChildren().addAll(createDots(steps));
        });

        getChildren().addAll(createDots(steps));

    }

    private java.util.List<Circle> createDots(ObservableList<Step> steps) {

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

            return circle;
        }).toJavaList();
    }

}
