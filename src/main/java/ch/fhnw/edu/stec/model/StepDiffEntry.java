package ch.fhnw.edu.stec.model;

import java.io.File;

public final class StepDiffEntry {

    private final FileChangeType fileChangeType;
    private final File file;

    public StepDiffEntry(FileChangeType fileChangeType, File file) {
        this.fileChangeType = fileChangeType;
        this.file = file;
    }

    public FileChangeType getFileChangeType() {
        return fileChangeType;
    }

    public File getFile() {
        return file;
    }

    public enum FileChangeType {ADD, MODIFY, DELETE}

}