package ch.fhnw.edu.stec;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableMap;

import java.io.File;

public final class StecModel {

    private final ObjectProperty<GigDir> gigDir = new SimpleObjectProperty<>();
    private final ObjectProperty<ObservableMap<String, String>> snapshots = new SimpleObjectProperty<>();

    ObjectProperty<GigDir> gigDirProperty() {
        return gigDir;
    }

    ObjectProperty<ObservableMap<String, String>> snapshots() { return snapshots; }

    public static abstract class GigDir {

        private final File dir;

        GigDir(File dir) {
            this.dir = dir;
        }

        public File getDir() {
            return dir;
        }

    }

    public static final class InvalidGigDir extends GigDir {
        InvalidGigDir(File dir) {
            super(dir);
        }
    }

    public static final class UninitializedGigDir extends GigDir {
        UninitializedGigDir(File dir) {
            super(dir);
        }
    }

    public static final class ReadyGigDir extends GigDir {
        ReadyGigDir(File dir) {
            super(dir);
        }
    }

}