package ch.fhnw.edu.stec.status;

import ch.fhnw.edu.stec.model.InteractionMode;
import ch.fhnw.edu.stec.model.ProjectDir;
import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.util.DotPlus;
import ch.fhnw.edu.stec.util.Glyphs;
import io.vavr.control.Option;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.text.Text;
import org.controlsfx.glyphfont.FontAwesome;

import java.util.function.Function;

public class StatusBarController {

    private static final int STATUS_BAR_DOT_RADIUS = 8;

    private StatusBarModel model;

    public StatusBarController(StatusBarModel model) {
        this.model = model;
        this.model.getStatusBarLeftItems().clear();
        this.model.getStatusBarRightItems().clear();
    }

    public ChangeListener<InteractionMode> asInteractionModeListener() {
        return (observable, oldValue, newValue) -> update();
    }

    public ChangeListener<ProjectDir> asProjectDirListener() {
        return (observable, oldValue, newValue) -> update();
    }

    private void update() {
        update(model.projectDirProperty().get(), model.interactionModeProperty().get(), model.getStatusBarLeftItems(), model::getStepByTag);
    }

    private static void update(ProjectDir projectDir, InteractionMode interactionMode, ObservableList<Node> statusBarLeftItems, Function<String, Option<Step>> stepByTag) {
        if (!(projectDir instanceof ProjectDir.ReadyProjectDir)) {
            Text textOne = new Text("Configure your project by specifying the location of a ");
            Text textTwo = new Text(" repository");
            statusBarLeftItems.setAll(textOne, Glyphs.FONT_AWESOME.create(FontAwesome.Glyph.GIT_SQUARE), textTwo);
        } else if (interactionMode instanceof InteractionMode.Capture) {
            Text text = new Text("Ready – Capture a step via ");
            statusBarLeftItems.setAll(text, Glyphs.FONT_AWESOME.create(FontAwesome.Glyph.CAMERA));
        } else if (interactionMode instanceof InteractionMode.Edit) {
            String stepTitle = stepByTag.apply(interactionMode.getTag()).map(Step::getTitle).getOrElse("Undefined");
            Text text = new Text("Checked out existing step '" + stepTitle + "'. Make edits or continue capturing new steps via ");
            statusBarLeftItems.setAll(text, new DotPlus(STATUS_BAR_DOT_RADIUS));
        }
    }

}