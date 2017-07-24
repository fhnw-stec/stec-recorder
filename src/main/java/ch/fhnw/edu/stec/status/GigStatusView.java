package ch.fhnw.edu.stec.status;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

import static ch.fhnw.edu.stec.util.Labels.*;

public final class GigStatusView extends BorderPane {

    private static final String SYMBOL_CHECK_MARK = "\u2713";
    private static final String SYMBOL_X_MARK = "\u2717";

    public GigStatusView(ReadOnlyBooleanProperty gigReadyProperty, GigStatusController controller) {

        setPadding(new Insets(5));

        Label label = new Label();
        label.textProperty().bind(dynamicLabel(gigReadyProperty));
        label.textFillProperty().bind(dynamicColor(gigReadyProperty));

        Button button = new Button(INIT_BUTTON_LABEL);
        button.visibleProperty().bind(gigReadyProperty.not());
        button.setOnAction(e -> controller.initGig());

        label.prefHeightProperty().bind(button.heightProperty());
        HBox.setHgrow(label, Priority.ALWAYS);

        HBox center = new HBox(5, label, button);

        setCenter(center);
    }

    private ObjectBinding<Color> dynamicColor(ReadOnlyBooleanProperty gigReadyProperty) {
        return Bindings.createObjectBinding(
                () -> gigReadyProperty.get() ? Color.GREEN : Color.DARKRED,
                gigReadyProperty);
    }

    private static StringBinding dynamicLabel(ReadOnlyBooleanProperty gigReadyProperty) {
        return Bindings.createStringBinding(
                () -> {
                    if (gigReadyProperty.get()) {
                        return SYMBOL_CHECK_MARK + " " + GIG_READY_LABEL;
                    } else {
                        return SYMBOL_X_MARK + " " + GIG_NOT_READY_LABEL;
                    }
                },
                gigReadyProperty);
    }

}