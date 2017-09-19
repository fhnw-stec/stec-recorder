package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.model.GigDir;
import ch.fhnw.edu.stec.model.Step;
import io.vavr.collection.List;
import io.vavr.control.Try;
import javafx.stage.Stage;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.ExternalResourceSupport;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static ch.fhnw.edu.stec.StecController.README_FILE_NAME;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ExternalResourceSupport.class)
class StecControllerTest {

    private static final Stage POPUP_OWNER = null; // creating an actual Stage could fail on a headless CI server

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private static StecController createInitializedGig(File gigDir, StecModel model) {
        StecController controller = new StecController(POPUP_OWNER, model);
        controller.chooseDirectory(gigDir);
        controller.initGig();
        return controller;
    }

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

    @Test
    void captureStep() throws IOException {
        File gigDir = tmpFolder.newFolder();
        StecModel model = new StecModel();
        StecController controller = createInitializedGig(gigDir, model);

        String newFile = "new.txt";
        assertTrue(new File(gigDir, newFile).createNewFile());

        String description = "Description of Step 1";
        Try<String> result = controller.captureStep("Step 1", description);

        assertTrue(result.isSuccess());

        // assert that a README has been created

        File readme = new File(gigDir, README_FILE_NAME);
        assertTrue(readme.exists());
        String readmeContents = new String(Files.readAllBytes(readme.toPath()));
        assertEquals(description, readmeContents);

        assertEquals(1, model.getSteps().size());

        // assert that a tagged commit representing the step has been created

        Repository repository = Git.open(gigDir).getRepository();
        assertTrue(repository.getTags().containsKey("step-42"));

        // assert that the model has been updated accordingly

        Step step = model.getSteps().get(0);
        assertEquals("Step 1", step.getTitle());

        // assert that the step contains all working dir modifications

        ObjectId headId = repository.resolve(Constants.HEAD);
        RevWalk walk = new RevWalk(repository);
        RevCommit headCommit = walk.parseCommit(headId);
        RevTree tree = walk.parseTree(headCommit.getTree().getId());
        ObjectLoader loader = repository.open(tree.getId());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        loader.copyTo(out);
        String treeContents = new String(out.toByteArray());
        assertTrue(treeContents.contains(newFile));
    }

}