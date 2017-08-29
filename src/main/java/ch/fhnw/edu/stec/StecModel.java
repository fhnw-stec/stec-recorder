package ch.fhnw.edu.stec;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableMap;

import java.io.File;

final class StecModel {

    private final ObjectProperty<File> gigDirectory = new SimpleObjectProperty<>();
    private final BooleanProperty gigReady = new SimpleBooleanProperty(false);
    private final ObjectProperty<ObservableMap<String, String>> snapshots = new SimpleObjectProperty<>();

    ObjectProperty<File> gigDirectoryProperty() {
        return gigDirectory;
    }

    ObjectProperty<ObservableMap<String, String>> snapshots() { return snapshots; }

    BooleanProperty gigReady() {
        return gigReady;
    }

}