package ch.fhnw.edu.stec.gig;

import ch.fhnw.edu.stec.model.GigDir;
import ch.fhnw.edu.stec.util.Labels;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;

public final class GigChooserView extends HBox {

    public GigChooserView(ObjectProperty<GigDir> gigDirProperty, Window owner, GigController controller) {

        setSpacing(5);

        TextField rootDirectoryValueField = new TextField();
        rootDirectoryValueField.setEditable(true);

        // binding text property to gig dir property would reset caret on every key press

        rootDirectoryValueField.setText(gigDirProperty.get().getDir().getAbsolutePath());

        rootDirectoryValueField.textProperty().addListener((observable, oldValue, newValue) -> controller.chooseDirectory(new File(newValue)));

        DirectoryChooser directoryChooser = new DirectoryChooser();

        if (!(gigDirProperty.get() instanceof GigDir.InvalidGigDir)) {
            directoryChooser.setInitialDirectory(gigDirProperty.get().getDir());
        }

        Button chooserButton = new Button(Labels.DIR_CHOOSER_BUTTON_LABEL);
        chooserButton.setOnAction(e -> {
            File dir = directoryChooser.showDialog(owner);
            if (dir != null) {
                rootDirectoryValueField.textProperty().setValue(dir.getAbsolutePath());
            }
        });

        HBox.setHgrow(rootDirectoryValueField, Priority.ALWAYS);
        getChildren().addAll(rootDirectoryValueField, chooserButton);

    }

}