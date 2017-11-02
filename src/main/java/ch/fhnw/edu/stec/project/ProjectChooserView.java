package ch.fhnw.edu.stec.project;

import ch.fhnw.edu.stec.model.ProjectDir;
import ch.fhnw.edu.stec.util.Labels;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;

public final class ProjectChooserView extends HBox {

    public ProjectChooserView(ObjectProperty<ProjectDir> projectDirProperty, Window owner, ProjectController controller) {

        setSpacing(5);

        TextField rootDirectoryValueField = new TextField();
        rootDirectoryValueField.setEditable(true);

        // binding text property to project dir property would reset caret on every key press

        rootDirectoryValueField.setText(projectDirProperty.get().getDir().getAbsolutePath());

        rootDirectoryValueField.textProperty().addListener((observable, oldValue, newValue) -> controller.chooseDirectory(new File(newValue)));

        DirectoryChooser directoryChooser = new DirectoryChooser();

        if (!(projectDirProperty.get() instanceof ProjectDir.InvalidProjectDir)) {
            directoryChooser.setInitialDirectory(projectDirProperty.get().getDir());
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