package ch.fhnw.edu.stec.model;

import java.io.File;

public abstract class GigDir {

    private final File dir;

    GigDir(File dir) {
        this.dir = dir;
    }

    public File getDir() {
        return dir;
    }

    public static final class InvalidGigDir extends GigDir {
        public InvalidGigDir(File dir) {
            super(dir);
        }
    }

    public static final class UninitializedGigDir extends GigDir {
        public UninitializedGigDir(File dir) {
            super(dir);
        }
    }

    public static final class ReadyGigDir extends GigDir {
        public ReadyGigDir(File dir) {
            super(dir);
        }
    }

}