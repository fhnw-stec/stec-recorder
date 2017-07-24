package ch.fhnw.edu.stec.chooser;

import ch.fhnw.edu.stec.util.Labels;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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
        rootDirectoryValueField.setEditable(false);
        rootDirectoryValueField.textProperty().bind(StringBinding.stringExpression(gigDirectoryProperty));

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