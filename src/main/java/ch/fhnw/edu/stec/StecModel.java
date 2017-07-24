package ch.fhnw.edu.stec;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;

final class StecModel {

    private final ObjectProperty<File> gigDirectory = new SimpleObjectProperty<>();
    private final BooleanProperty gigReady = new SimpleBooleanProperty(false);

    ObjectProperty<File> gigDirectoryProperty() {
        return gigDirectory;
    }

    BooleanProperty gigReady() {
        return gigReady;
    }

}