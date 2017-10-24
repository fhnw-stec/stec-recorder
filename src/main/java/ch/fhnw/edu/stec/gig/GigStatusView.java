package ch.fhnw.edu.stec.gig;

import ch.fhnw.edu.stec.model.GigDir;
import ch.fhnw.edu.stec.util.Glyphs;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;

import static ch.fhnw.edu.stec.util.Labels.*;

public final class GigStatusView extends BorderPane {

    private static final String SYMBOL_CHECK_MARK = "\u2713";
    private static final String SYMBOL_X_MARK = "\u2717";

    public GigStatusView(ObjectProperty<GigDir> gigDirProperty, GigController controller) {

        Label label = new Label();
        label.textProperty().bind(dynamicLabel(gigDirProperty));
        label.textFillProperty().bind(dynamicColor(gigDirProperty));

        Button button = new Button(INIT_BUTTON_LABEL, Glyphs.FONT_AWESOME.create(FontAwesome.Glyph.GIT_SQUARE));
        button.visibleProperty().bind(Bindings.createBooleanBinding(() -> gigDirProperty.get() instanceof GigDir.UninitializedGigDir, gigDirProperty));
        button.setOnAction(e -> controller.initGig());

        label.prefHeightProperty().bind(button.heightProperty());
        HBox.setHgrow(label, Priority.ALWAYS);

        HBox center = new HBox(5, label, button);

        setCenter(center);
    }

    private ObjectBinding<Color> dynamicColor(ObjectProperty<GigDir> gigReadyProperty) {
        return Bindings.createObjectBinding(
                () -> gigReadyProperty.get() instanceof GigDir.ReadyGigDir ? Color.GREEN : Color.DARKRED,
                gigReadyProperty);
    }

    private static StringBinding dynamicLabel(ObjectProperty<GigDir> gigReadyProperty) {
        return Bindings.createStringBinding(
                () -> {
                    if (gigReadyProperty.get() instanceof GigDir.InvalidGigDir) {
                        return SYMBOL_X_MARK + " " + INVALID_GIG_DIR_LABEL;
                    } else if (gigReadyProperty.get() instanceof GigDir.UninitializedGigDir){
                        return SYMBOL_X_MARK + " " + UNINITIALIZED_GIG_DIR_LABEL;
                    } else {
                        return SYMBOL_CHECK_MARK + " " + READY_GIG_DIR_LABEL;
                    }
                },
                gigReadyProperty);
    }

}