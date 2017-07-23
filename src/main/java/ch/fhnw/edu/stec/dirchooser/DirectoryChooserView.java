package ch.fhnw.edu.stec.dirchooser;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;

public final class DirectoryChooserView extends HBox {

    public DirectoryChooserView(ObjectProperty<File> rootDirectoryProperty, Window owner, DirectoryChooserController controller) {

        setSpacing(5);
        setPadding(new Insets(5));

        TextField rootDirectoryValueField = new TextField();
        rootDirectoryValueField.setEditable(false);
        rootDirectoryValueField.textProperty().bind(StringBinding.stringExpression(rootDirectoryProperty));

        Label rootDirectoryLabel = new Label("Root Directory:");
        rootDirectoryLabel.setLabelFor(rootDirectoryValueField);
        rootDirectoryLabel.prefHeightProperty().bind(rootDirectoryValueField.heightProperty());

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.initialDirectoryProperty().bind(rootDirectoryProperty);

        Button chooserButton = new Button("...");
        chooserButton.setOnAction(e -> {
            File dir = directoryChooser.showDialog(owner);
            controller.chooseDirectory(dir);
        });

        HBox.setHgrow(rootDirectoryValueField, Priority.ALWAYS);
        getChildren().addAll(rootDirectoryLabel, rootDirectoryValueField, chooserButton);

    }

}