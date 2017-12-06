package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.model.InteractionMode;
import ch.fhnw.edu.stec.model.ProjectDir;
import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.model.StepDiffEntry;
import ch.fhnw.edu.stec.notification.Notification;
import io.vavr.collection.List;
import io.vavr.control.Try;
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
import java.util.Set;

import static ch.fhnw.edu.stec.StecController.README_FILE_NAME;
import static ch.fhnw.edu.stec.util.Labels.INITIAL_STATUS_COMMIT_MSG;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ExternalResourceSupport.class)
class StecControllerTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private static StecController createInitializedProject(File projectDir, StecModel model) {
        StecController controller = new StecController(model);
        controller.chooseDirectory(projectDir);
        controller.initProject();
        return controller;
    }

    @Test
    void appendNotification() {
        StecModel model = new StecModel();
        StecController controller = new StecController(model);

        assertTrue(model.getNotifications().isEmpty());

        Notification notification = Notification.error("test");
        controller.appendNotification(notification);

        assertFalse(model.getNotifications().isEmpty());
        assertEquals(notification, model.getNotifications().get(0));
    }

    @Test
    void chooseDirectory() throws IOException {
        StecModel model = new StecModel();
        StecController controller = new StecController(model);

        File initialDir = tmpFolder.newFolder("initial");
        model.projectDirProperty().set(new ProjectDir.UninitializedProjectDir(initialDir));

        controller.chooseDirectory(null);
        assertTrue(model.projectDirProperty().get() instanceof ProjectDir.InvalidProjectDir, "null safety");
        assertEquals(new File(""), model.projectDirProperty().get().getDir(), "null safety");

        File invalidDir = tmpFolder.newFile();
        controller.chooseDirectory(invalidDir);
        assertTrue(model.projectDirProperty().get() instanceof ProjectDir.InvalidProjectDir, "not a directory");
        assertEquals(invalidDir, model.projectDirProperty().get().getDir(), "null safety");

        File newDir = tmpFolder.newFolder("new");
        controller.chooseDirectory(newDir);
        assertTrue(model.projectDirProperty().get() instanceof ProjectDir.UninitializedProjectDir);
        assertEquals(newDir, model.projectDirProperty().get().getDir());

        controller.initProject();
        assertTrue(model.projectDirProperty().get() instanceof ProjectDir.ReadyProjectDir);
        controller.captureStep("test-step", "test description");
        assertEquals(1, model.getSteps().size());

        controller.chooseDirectory(invalidDir);
        assertEquals(0, model.getSteps().size());
    }

    @Test
    void initProject() throws IOException, GitAPIException {
        File projectDir = tmpFolder.newFolder();
        File gitignoreFile = new File(projectDir, StecController.GIT_IGNORE_FILE_NAME);
        File readmeFile = new File(projectDir, StecController.README_FILE_NAME);

        StecModel model = new StecModel();
        StecController controller = new StecController(model);
        controller.chooseDirectory(projectDir);

        assertTrue(model.projectDirProperty().get() instanceof ProjectDir.UninitializedProjectDir);
        assertFalse(gitignoreFile.exists());
        assertFalse(readmeFile.exists());

        controller.initProject();

        assertTrue(model.projectDirProperty().get() instanceof ProjectDir.ReadyProjectDir);
        assertTrue(gitignoreFile.exists());
        assertTrue(readmeFile.exists());

        File gitRepo = new File(projectDir, StecController.GIT_REPO);
        List<RevCommit> commits = List.ofAll(Git.open(gitRepo).log().call());
        assertEquals(1, commits.size());
        assertEquals(INITIAL_STATUS_COMMIT_MSG, commits.get(0).getFullMessage());
    }

    @Test
    void captureStep() throws IOException {
        File projectDir = tmpFolder.newFolder();
        StecModel model = new StecModel();
        StecController controller = createInitializedProject(projectDir, model);

        String newFile = "new.txt";
        assertTrue(new File(projectDir, newFile).createNewFile());

        String description = "Description of Step 1";
        Try<String> result = controller.captureStep("Step 1", description);

        assertTrue(result.isSuccess());

        // assert that a README has been created

        File readme = new File(projectDir, README_FILE_NAME);
        assertTrue(readme.exists());
        String readmeContents = new String(Files.readAllBytes(readme.toPath()));
        assertEquals(description, readmeContents);

        assertEquals(1, model.getSteps().size());

        // assert that a tagged commit representing the step has been created

        Repository repository = Git.open(projectDir).getRepository();
        assertTrue(repository.getTags().containsKey("step-1"));

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

    @Test
    void captureStepAutoIncrementTag() throws IOException, GitAPIException {
        File projectDir = tmpFolder.newFolder();
        StecModel model = new StecModel();
        StecController controller = createInitializedProject(projectDir, model);

        assertTrue(controller.captureStep("Step 1", "Description of Step 1").isSuccess());
        assertTrue(controller.captureStep("Step 2", "Description of Step 2").isSuccess());
        assertTrue(controller.captureStep("Step 3", "Description of Step 3").isSuccess());

        Git git = Git.open(projectDir);
        Repository repository = git.getRepository();
        Set<String> tags = repository.getTags().keySet();

        assertTrue(tags.contains("step-1"));
        assertTrue(tags.contains("step-2"));
        assertTrue(tags.contains("step-3"));

        // assert that auto-incrementing logic can cope with tags which were created behind the controller's back
        git.tag().setName("step-42").setMessage("A manually set tag").call();

        assertTrue(controller.captureStep("Step 4", "Description of Step 4").isSuccess());
        assertTrue(repository.getTags().keySet().contains("step-43"));
    }

    @Test
    void loadSteps() throws IOException {
        File projectDir = tmpFolder.newFolder();
        StecModel model = new StecModel();
        StecController controller = createInitializedProject(projectDir, model);

        assertTrue(controller.captureStep("Step 1", "Description of Step 1").isSuccess());
        assertTrue(controller.captureStep("Step 2", "Description of Step 2").isSuccess());
        assertTrue(controller.captureStep("Step 3", "Description of Step 3").isSuccess());

        List<Step> steps = model.getSteps();
        assertEquals(3, steps.size());

        Step firstStep = steps.get(0);
        assertEquals("Step 1", firstStep.getTitle());
        assertEquals("Description of Step 1", firstStep.getDescription());

        Step secondStep = steps.get(1);
        assertEquals("Step 2", secondStep.getTitle());
        assertEquals("Description of Step 2", secondStep.getDescription());

        Step thirdStep = steps.get(2);
        assertEquals("Step 3", thirdStep.getTitle());
        assertEquals("Description of Step 3", thirdStep.getDescription());
    }

    @Test
    void switchToEditMode() throws IOException {
        File projectDir = tmpFolder.newFolder();
        StecModel model = new StecModel();
        StecController controller = createInitializedProject(projectDir, model);

        assertTrue(controller.captureStep("Step 1", "Description of Step 1").isSuccess());
        assertTrue(controller.captureStep("Step 2", "Description of Step 2").isSuccess());
        assertTrue(controller.captureStep("Step 3", "Description of Step 3").isSuccess());

        Step stepToEdit = model.getSteps().get(0);

        Try<String> result = controller.switchToEditMode(stepToEdit.getTag());
        assertTrue(result.isSuccess());

        assertTrue(model.interactionModeProperty().get() instanceof InteractionMode.Edit);

        assertEquals(stepToEdit.getTitle(), model.titleProperty().get());
        assertEquals(stepToEdit.getDescription(), model.descriptionProperty().get());
    }

    @Test
    void stepDiff() throws IOException {
        File projectDir = tmpFolder.newFolder();

        StecModel model = new StecModel();
        StecController controller = createInitializedProject(projectDir, model);

        File readmeFile = new File(projectDir, README_FILE_NAME);
        File fooFile = new File(projectDir, "foo.txt");

        {
            // create a step with
            // - one modification (README.adoc)
            // - one addition (foo.txt)

            assertTrue(fooFile.createNewFile());
            assertTrue(controller.captureStep("Step 1", "Description of Step 1").isSuccess());
            Step stepToEdit = model.getSteps().get(0);

            Try<String> result = controller.switchToEditMode(stepToEdit.getTag());
            assertTrue(result.isSuccess());

            assertEquals(2, model.getStepDiffEntries().size());

            assertEquals(readmeFile, model.getStepDiffEntries().get(0).getFile());
            assertEquals(StepDiffEntry.FileChangeType.MODIFY, model.getStepDiffEntries().get(0).getFileChangeType());

            assertEquals(fooFile, model.getStepDiffEntries().get(1).getFile());
            assertEquals(StepDiffEntry.FileChangeType.ADD, model.getStepDiffEntries().get(1).getFileChangeType());
        }

        {
            // create a step with
            // - one modification (README.adoc)
            // - one addition (bar.txt)
            // - one deletion (foo.txt)

            File barFile = new File(projectDir, "bar.txt");
            assertTrue(barFile.createNewFile());
            assertTrue(fooFile.delete());

            assertTrue(controller.captureStep("Step 2", "Description of Step 2").isSuccess());

            Step stepToEdit = model.getSteps().get(1);

            Try<String> result = controller.switchToEditMode(stepToEdit.getTag());
            assertTrue(result.isSuccess());

            assertEquals(3, model.getStepDiffEntries().size());

            model.getStepDiffEntries().forEach(e -> {
                System.out.println(e.getFileChangeType());
                System.out.println(e.getFile());
            });

            assertEquals(readmeFile, model.getStepDiffEntries().get(0).getFile());
            assertEquals(StepDiffEntry.FileChangeType.MODIFY, model.getStepDiffEntries().get(0).getFileChangeType());

            assertEquals(barFile, model.getStepDiffEntries().get(1).getFile());
            assertEquals(StepDiffEntry.FileChangeType.ADD, model.getStepDiffEntries().get(1).getFileChangeType());

            assertEquals(fooFile, model.getStepDiffEntries().get(2).getFile());
            assertEquals(StepDiffEntry.FileChangeType.DELETE, model.getStepDiffEntries().get(2).getFileChangeType());
        }
    }

    @Test
    void saveStep() throws IOException {
        File projectDir = tmpFolder.newFolder();
        StecModel model = new StecModel();
        StecController controller = createInitializedProject(projectDir, model);

        assertTrue(controller.captureStep("Step 1", "Description of Step 1").isSuccess());
        assertTrue(controller.captureStep("Step 2", "Description of Step 2").isSuccess());
        assertTrue(controller.captureStep("Step 3", "Description of Step 3").isSuccess());

        Step step = model.getSteps().get(0);

        final String NEW_TITLE = "New Title";
        final String NEW_DESCRIPTION = "New Description";

        Try<String> result = controller.saveStep(step.getTag(), NEW_TITLE, NEW_DESCRIPTION);
        assertTrue(result.isSuccess());

        assertEquals(NEW_TITLE, model.getSteps().get(0).getTitle());
        assertEquals(NEW_DESCRIPTION, model.getSteps().get(0).getDescription());
    }

    @Test
    void deleteStep() throws IOException {
        File projectDir = tmpFolder.newFolder();
        StecModel model = new StecModel();
        StecController controller = createInitializedProject(projectDir, model);

        assertTrue(controller.captureStep("Step 1", "Description of Step 1").isSuccess());
        assertTrue(controller.captureStep("Step 2", "Description of Step 2").isSuccess());
        assertTrue(controller.captureStep("Step 3", "Description of Step 3").isSuccess());

        Step step = model.getSteps().get(0);

        Try<String> result = controller.deleteStep(step.getTag());
        assertTrue(result.isSuccess());

        assertEquals(2, model.getSteps().size());
    }

}