package ch.fhnw.edu.stec.diff;

import ch.fhnw.edu.stec.model.InteractionMode;
import ch.fhnw.edu.stec.model.ProjectDir;
import ch.fhnw.edu.stec.model.StepDiffEntry;
import io.vavr.collection.Stream;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static ch.fhnw.edu.stec.model.StepDiffEntry.FileChangeType.*;

public class StagingAreaWatcher {

    private static final Logger LOG = LoggerFactory.getLogger(StagingAreaWatcher.class);

    private final Timeline watcher;

    public StagingAreaWatcher(ObjectProperty<ProjectDir> projectDirProperty, ObjectProperty<InteractionMode> interactionModeProperty, ObservableList<StepDiffEntry> stepDiffEntries) {
        watcher = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (projectDirProperty.get() instanceof ProjectDir.ReadyProjectDir) {
                File dir = projectDirProperty.get().getDir();
                if (interactionModeProperty.get() instanceof InteractionMode.Capture) {
                    LOG.debug(String.format("Watching staging area '%s'", dir));
                    try {
                        Git git = Git.open(dir);

                        // Display status like it would be captured (auto-adding files)
                        git.add().addFilepattern(".").call();
                        // Needed to properly display removed files (https://stackoverflow.com/a/40622293/57448)
                        git.add().setUpdate(true).addFilepattern(".").call();

                        Status status = git.status().call();

                        java.util.List<StepDiffEntry> added = Stream.ofAll(status.getAdded()).map(f -> new StepDiffEntry(ADD, new File(dir, f))).toJavaList();
                        java.util.List<StepDiffEntry> changed = Stream.ofAll(status.getChanged()).map(f -> new StepDiffEntry(MODIFY, new File(dir, f))).toJavaList();
                        java.util.List<StepDiffEntry> modified = Stream.ofAll(status.getModified()).map(f -> new StepDiffEntry(MODIFY, new File(dir, f))).toJavaList();
                        java.util.List<StepDiffEntry> removed = Stream.ofAll(status.getRemoved()).map(f -> new StepDiffEntry(DELETE, new File(dir, f))).toJavaList();

                        stepDiffEntries.clear();
                        stepDiffEntries.addAll(added);
                        stepDiffEntries.addAll(changed);
                        stepDiffEntries.addAll(modified);
                        stepDiffEntries.addAll(removed);

                    } catch (IOException | GitAPIException e) {
                        LOG.error("Watching staging area failed", e);
                    }
                }
            }
        }));
        watcher.setCycleCount(Timeline.INDEFINITE);
    }

    public void start() {
        watcher.play();
    }

}
