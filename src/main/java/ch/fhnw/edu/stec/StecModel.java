package ch.fhnw.edu.stec;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;

final class StecModel {

    private final ObjectProperty<File> rootDirectory = new SimpleObjectProperty<>(new File(System.getProperty("user.home")));

    ObjectProperty<File> rootDirectoryProperty() {
        return rootDirectory;
    }

}