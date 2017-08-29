package ch.fhnw.edu.stec.chooser;

import ch.fhnw.edu.stec.util.Labels;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;

public final class GigChooserView extends HBox {

    public GigChooserView(ObjectProperty<File> gigDirectoryProperty, Window owner, GigChooserController controller) {

        setSpacing(5);
        setPadding(new Insets(5));

        TextField rootDirectoryValueField = new TextField();
        rootDirectoryValueField.setEditable(true);
        rootDirectoryValueField.setText(gigDirectoryProperty.get().getAbsolutePath());
        rootDirectoryValueField.focusedProperty().addListener((arg0, oldVal, newValue) -> {
            File file = new File(rootDirectoryValueField.getText());
            if (!file.exists() || !file.isDirectory()){
                rootDirectoryValueField.setStyle("-fx-background-color: rgba(255, 0, 0, 0.3);");
            }
            else if (file.exists() && file.isDirectory()){
                rootDirectoryValueField.setStyle("");
                gigDirectoryProperty.setValue(file);
            }
        });

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.initialDirectoryProperty().bind(gigDirectoryProperty);

        Button chooserButton = new Button(Labels.DIR_CHOOSER_BUTTON_LABEL);
        chooserButton.setOnAction(e -> {
            File dir = directoryChooser.showDialog(owner);
            controller.chooseDirectory(dir);
        });

        HBox.setHgrow(rootDirectoryValueField, Priority.ALWAYS);
        getChildren().addAll(rootDirectoryValueField, chooserButton);

    }

}