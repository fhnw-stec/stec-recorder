package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.model.GigDir;
import io.vavr.collection.List;
import javafx.stage.Stage;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.ExternalResourceSupport;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ExternalResourceSupport.class)
class StecControllerTest {

    private static final Stage POPUP_OWNER = null; // creating an actual Stage could fail on a headless CI server

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    void chooseDirectory() throws IOException {
        StecModel model = new StecModel();
        StecController controller = new StecController(POPUP_OWNER, model);

        File initialDir = tmpFolder.newFolder("initial");
        model.gigDirProperty().set(new GigDir.UninitializedGigDir(initialDir));

        controller.chooseDirectory(null);
        assertTrue(model.gigDirProperty().get() instanceof GigDir.InvalidGigDir, "null safety");
        assertEquals(new File(""), model.gigDirProperty().get().getDir(), "null safety");

        File file = tmpFolder.newFile();
        controller.chooseDirectory(file);
        assertTrue(model.gigDirProperty().get() instanceof GigDir.InvalidGigDir, "not a directory");
        assertEquals(file, model.gigDirProperty().get().getDir(), "null safety");

        File newDir = tmpFolder.newFolder("new");
        controller.chooseDirectory(newDir);
        assertTrue(model.gigDirProperty().get() instanceof GigDir.UninitializedGigDir);
        assertEquals(newDir, model.gigDirProperty().get().getDir());
    }

    @Test
    void initGig() throws IOException, GitAPIException {
        File gigDir = tmpFolder.newFolder();
        File gitignoreFile = new File(gigDir, StecController.GIT_IGNORE_FILE_NAME);

        StecModel model = new StecModel();
        StecController controller = new StecController(POPUP_OWNER, model);
        controller.chooseDirectory(gigDir);

        assertTrue(model.gigDirProperty().get() instanceof GigDir.UninitializedGigDir);
        assertFalse(gitignoreFile.exists());

        controller.initGig();

        assertTrue(model.gigDirProperty().get() instanceof GigDir.ReadyGigDir);
        assertTrue(gitignoreFile.exists());

        File gitRepo = new File(gigDir, StecController.GIT_REPO);
        List<RevCommit> commits = List.ofAll(Git.open(gitRepo).log().call());
        assertEquals(1, commits.size());
        assertEquals(StecController.ADD_GIT_IGNORE_COMMIT_MSG, commits.get(0).getShortMessage());
    }

}