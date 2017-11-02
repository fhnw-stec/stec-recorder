package ch.fhnw.edu.stec.project;

import ch.fhnw.edu.stec.model.ProjectDir;
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

public final class ProjectStatusView extends BorderPane {

    private static final String SYMBOL_CHECK_MARK = "\u2713";
    private static final String SYMBOL_X_MARK = "\u2717";

    public ProjectStatusView(ObjectProperty<ProjectDir> projectDirProperty, ProjectController controller) {

        Label label = new Label();
        label.textProperty().bind(dynamicLabel(projectDirProperty));
        label.textFillProperty().bind(dynamicColor(projectDirProperty));

        Button button = new Button(INIT_BUTTON_LABEL, Glyphs.FONT_AWESOME.create(FontAwesome.Glyph.GIT_SQUARE));
        button.visibleProperty().bind(Bindings.createBooleanBinding(() -> projectDirProperty.get() instanceof ProjectDir.UninitializedProjectDir, projectDirProperty));
        button.setOnAction(e -> controller.initProject());

        label.prefHeightProperty().bind(button.heightProperty());
        HBox.setHgrow(label, Priority.ALWAYS);

        HBox center = new HBox(5, label, button);

        setCenter(center);
    }

    private ObjectBinding<Color> dynamicColor(ObjectProperty<ProjectDir> projectReadyProperty) {
        return Bindings.createObjectBinding(
                () -> projectReadyProperty.get() instanceof ProjectDir.ReadyProjectDir ? Color.GREEN : Color.DARKRED,
                projectReadyProperty);
    }

    private static StringBinding dynamicLabel(ObjectProperty<ProjectDir> projectReadyProperty) {
        return Bindings.createStringBinding(
                () -> {
                    if (projectReadyProperty.get() instanceof ProjectDir.InvalidProjectDir) {
                        return SYMBOL_X_MARK + " " + INVALID_PROJECT_DIR_LABEL;
                    } else if (projectReadyProperty.get() instanceof ProjectDir.UninitializedProjectDir){
                        return SYMBOL_X_MARK + " " + UNINITIALIZED_PROJECT_DIR_LABEL;
                    } else {
                        return SYMBOL_CHECK_MARK + " " + READY_PROJECT_DIR_LABEL;
                    }
                },
                projectReadyProperty);
    }

}