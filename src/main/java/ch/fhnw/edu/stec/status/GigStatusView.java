package ch.fhnw.edu.stec.status;

import ch.fhnw.edu.stec.StecModel;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
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

    public GigStatusView(ObjectProperty<StecModel.GigDir> gigDirProperty, GigStatusController controller) {

        setPadding(new Insets(5));

        Label label = new Label();
        label.textProperty().bind(dynamicLabel(gigDirProperty));
        label.textFillProperty().bind(dynamicColor(gigDirProperty));

        Button button = new Button(INIT_BUTTON_LABEL);
        button.visibleProperty().bind(Bindings.createBooleanBinding(() -> gigDirProperty.get() instanceof StecModel.UninitializedGigDir, gigDirProperty));
        button.setOnAction(e -> controller.initGig());

        label.prefHeightProperty().bind(button.heightProperty());
        HBox.setHgrow(label, Priority.ALWAYS);

        HBox center = new HBox(5, label, button);

        setCenter(center);
    }

    private ObjectBinding<Color> dynamicColor(ObjectProperty<StecModel.GigDir> gigReadyProperty) {
        return Bindings.createObjectBinding(
                () -> gigReadyProperty.get() instanceof StecModel.ReadyGigDir ? Color.GREEN : Color.DARKRED,
                gigReadyProperty);
    }

    private static StringBinding dynamicLabel(ObjectProperty<StecModel.GigDir> gigReadyProperty) {
        return Bindings.createStringBinding(
                () -> {
                    if (gigReadyProperty.get() instanceof StecModel.InvalidGigDir) {
                        return SYMBOL_X_MARK + " " + INVALID_GIG_DIR_LABEL;
                    } else if (gigReadyProperty.get() instanceof StecModel.UninitializedGigDir){
                        return SYMBOL_X_MARK + " " + UNINITIALIZED_GIG_DIR_LABEL;
                    } else {
                        return SYMBOL_CHECK_MARK + " " + READY_GIG_DIR_LABEL;
                    }
                },
                gigReadyProperty);
    }

}