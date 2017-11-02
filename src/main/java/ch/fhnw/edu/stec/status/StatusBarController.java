package ch.fhnw.edu.stec.status;

import ch.fhnw.edu.stec.model.ProjectDir;
import ch.fhnw.edu.stec.model.InteractionMode;
import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.util.DotPlus;
import ch.fhnw.edu.stec.util.Glyphs;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.text.Text;
import org.controlsfx.glyphfont.FontAwesome;

public class StatusBarController implements ChangeListener<InteractionMode> {

    private static final int STATUS_BAR_DOT_RADIUS = 8;

    private StatusBarModel model;

    public StatusBarController(StatusBarModel model) {
        this.model = model;
        this.model.getStatusBarLeftItems().clear();
        this.model.getStatusBarRightItems().clear();
    }

    @Override
    public void changed(ObservableValue<? extends InteractionMode> observable, InteractionMode oldValue, InteractionMode newValue) {
        if (!(model.projectDirProperty().get() instanceof ProjectDir.ReadyProjectDir)) {
            Text textOne = new Text("Configure your project by specifying the location of a ");
            Text textTwo = new Text(" repository");
            model.getStatusBarLeftItems().setAll(textOne, Glyphs.FONT_AWESOME.create(FontAwesome.Glyph.GIT_SQUARE), textTwo);
        } else if (newValue instanceof InteractionMode.Capture) {
            Text text = new Text("Ready – Capture a step via ");
            model.getStatusBarLeftItems().setAll(text, Glyphs.FONT_AWESOME.create(FontAwesome.Glyph.CAMERA));
        } else if (newValue instanceof InteractionMode.Edit) {
            String stepTitle = model.getStepByTag(newValue.getTag()).map(Step::getTitle).getOrElse("Undefined");
            Text text = new Text("Checked out existing step '" + stepTitle + "'. Make edits or continue capturing new steps via ");
            model.getStatusBarLeftItems().setAll(text, new DotPlus(STATUS_BAR_DOT_RADIUS));
        }
    }

}