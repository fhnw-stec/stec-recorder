package ch.fhnw.edu.stec.model;

import java.io.File;

public abstract class ProjectDir {

    private final File dir;

    ProjectDir(File dir) {
        this.dir = dir;
    }

    public File getDir() {
        return dir;
    }

    public static final class InvalidProjectDir extends ProjectDir {
        public InvalidProjectDir(File dir) {
            super(dir);
        }
    }

    public static final class UninitializedProjectDir extends ProjectDir {
        public UninitializedProjectDir(File dir) {
            super(dir);
        }
    }

    public static final class ReadyProjectDir extends ProjectDir {
        public ReadyProjectDir(File dir) {
            super(dir);
        }
    }

}