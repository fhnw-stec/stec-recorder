package ch.fhnw.edu.stec;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import org.eclipse.jgit.lib.Ref;

import java.io.File;
import java.util.Map;

final class StecModel {

    private final ObjectProperty<File> gigDirectory = new SimpleObjectProperty<>();
    private final BooleanProperty gigReady = new SimpleBooleanProperty(false);
    private final ObjectProperty<Map<String, Ref>> snapshots = new SimpleObjectProperty<>();
    private final ObjectProperty<ObservableList<String>> snapshots_names = new SimpleObjectProperty<>();

    ObjectProperty<File> gigDirectoryProperty() {
        return gigDirectory;
    }

    ObjectProperty<Map<String, Ref>> snapshots() { return snapshots; }

    ObjectProperty<ObservableList<String>> snapshots_names() { return snapshots_names; }

    BooleanProperty gigReady() {
        return gigReady;
    }

}