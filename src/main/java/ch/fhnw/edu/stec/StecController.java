package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.form.StepFormController;
import ch.fhnw.edu.stec.history.StepHistoryController;
import ch.fhnw.edu.stec.model.InteractionMode;
import ch.fhnw.edu.stec.model.ProjectDir;
import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.model.StepDiffEntry;
import ch.fhnw.edu.stec.notification.Notification;
import ch.fhnw.edu.stec.notification.NotificationController;
import ch.fhnw.edu.stec.project.ProjectController;
import io.vavr.collection.*;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

import static ch.fhnw.edu.stec.model.StepDiffEntry.FileChangeType.*;
import static ch.fhnw.edu.stec.util.Labels.*;
import static io.vavr.API.*;
import static java.lang.String.format;

final class StecController implements ProjectController, StepFormController, StepHistoryController, NotificationController {

    static final String GIT_REPO = ".git";
    static final String GIT_IGNORE_FILE_NAME = ".gitignore";
    static final String README_FILE_NAME = "README.adoc";

    private static final Logger LOG = LoggerFactory.getLogger(StecController.class);
    private static final String STEP_PREFIX = "step-";
    private static final String GIT_IGNORE_TEMPLATE_FILE_NAME = "/gitignore-template.txt";
    private static final String EDIT_BRANCH_PREFIX = "edit-";

    private final StecModel model;

    StecController(StecModel model) {
        this.model = model;
        model.interactionModeProperty().setValue(InteractionMode.capture(Step.UPCOMING_STEP_TAG));
        model.projectDirProperty().addListener((observable, oldValue, newValue) -> refresh());
        chooseDirectory(new File(System.getProperty("user.home")));
        switchToCaptureMode();
    }

    private static Try<Git> initGitRepo(File dir) {
        try {
            return Try.success(Git.init().setDirectory(dir).call());
        } catch (GitAPIException e) {
            LOG.error("Git init failed.", e);
            return Try.failure(e);
        }
    }

    private static void commitInitialStatus(Git git) {
        try (InputStream gitIgnoreSource = StecController.class.getResourceAsStream(GIT_IGNORE_TEMPLATE_FILE_NAME)) {
            File workTree = git.getRepository().getWorkTree();

            File gitIgnore = new File(workTree, GIT_IGNORE_FILE_NAME);
            Files.copy(gitIgnoreSource, gitIgnore.toPath(), StandardCopyOption.REPLACE_EXISTING);
            File readme = new File(workTree, README_FILE_NAME);
            if (!readme.exists()) {
                //noinspection ResultOfMethodCallIgnored
                readme.createNewFile();
            }

            git.add().addFilepattern(GIT_IGNORE_FILE_NAME).addFilepattern(README_FILE_NAME).call();

            git.commit().setMessage(INITIAL_STATUS_COMMIT_MSG).call();

        } catch (IOException | GitAPIException e) {
            LOG.error("Committing initial status failed.", e);
        }
    }

    private static String nextTag(Set<String> existingTags) {
        // assuming a "step-42" format, find highest numerical suffix among existing tags
        Integer maxIntSuffix = existingTags.map(StecController::tagToInt).max().getOrElse(0);
        return STEP_PREFIX + (maxIntSuffix + 1);
    }

    private static Integer tagToInt(String tag) {
        String suffix = tag.substring(STEP_PREFIX.length());
        if (suffix.chars().allMatch(Character::isDigit)) {
            return Integer.parseInt(suffix);
        } else {
            return 0;
        }
    }

    private static Seq<Step> loadSteps(Git git) {
        Repository repository = git.getRepository();
        Map<String, Ref> tags = HashMap.ofAll(repository.getTags());
        Seq<Step> steps = tags.flatMap(tag -> loadStep(repository, tag._1, tag._2));
        return steps.sortBy(s -> tagToInt(s.getTag()));
    }

    private static Try<Step> loadStep(Repository repository, String tagName, Ref tagRef) {
        RevWalk revWalk = new RevWalk(repository);
        try {
            ObjectId tagId = tagRef.getObjectId();
            RevCommit commit = revWalk.parseCommit(tagId);
            String description = loadDescription(repository, commit);
            RevTag revTag = revWalk.parseTag(tagId);
            Step step = new Step(tagName, revTag.getFullMessage().trim(), description);
            return Try.success(step);
        } catch (Throwable t) {
            LOG.error("Loading step details failed", t);
            return Try.failure(t);
        } finally {
            revWalk.dispose();
        }
    }

    private static String loadDescription(Repository repository, RevCommit commit) throws IOException {
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(commit.getTree());
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(README_FILE_NAME));

        if (treeWalk.next()) {
            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = repository.open(objectId);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            loader.copyTo(bos);

            return bos.toString(StandardCharsets.UTF_8.name());
        } else {
            LOG.warn("Unable to load description (missing README.adoc in " + commit + ")");
            return "";
        }
    }

    private static Try<List<StepDiffEntry>> loadStepDiff(File dir, String focusStepTag, List<Step> steps) {
        try {
            Git git = Git.open(dir);
            Repository repository = git.getRepository();

            int focusStepIndex = steps.zipWithIndex().find(s -> s._1.getTag().equals(focusStepTag)).get()._2;

            Map<String, Ref> tags = HashMap.ofAll(repository.getTags());
            AnyObjectId focusStepCommitId = tags.get(focusStepTag).get().getObjectId();
            AnyObjectId referenceCommitId;

            if (focusStepIndex == 0) {
                referenceCommitId = resolveReferenceCommitForFirstStep(repository, focusStepCommitId);
            } else {
                referenceCommitId = resolveReferenceCommitForSubsequentSteps(steps, focusStepIndex, tags);
            }

            AbstractTreeIterator newTreeIt = createTreeIterator(repository, focusStepCommitId);
            AbstractTreeIterator oldTreeIt = createTreeIterator(repository, referenceCommitId);

            List<DiffEntry> diffEntries = List.ofAll(git.diff()
                    .setOldTree(oldTreeIt)
                    .setNewTree(newTreeIt)
                    .call());

            List<StepDiffEntry> entries = diffEntries.map(e -> Match(e.getChangeType()).of(
                    Case($(DiffEntry.ChangeType.ADD), new StepDiffEntry(ADD, new File(dir, e.getNewPath()))),
                    Case($(DiffEntry.ChangeType.DELETE), new StepDiffEntry(DELETE, new File(dir, e.getOldPath()))),
                    Case($(), new StepDiffEntry(MODIFY, new File(dir, e.getNewPath()))) // modify/rename/copy all map to modify
            ));

            return Try.success(entries);
        } catch (Throwable t) {
            return Try.failure(t);
        }

    }

    private static AnyObjectId resolveReferenceCommitForSubsequentSteps(List<Step> steps, int focusStepIndex, Map<String, Ref> tags) {
        Step previousStep = steps.get(focusStepIndex - 1);
        return tags.get(previousStep.getTag()).get().getObjectId();
    }

    private static AnyObjectId resolveReferenceCommitForFirstStep(Repository repository, AnyObjectId focusStepCommitId) throws IOException {
        RevWalk walk = new RevWalk(repository);
        RevCommit focusStepCommit = walk.parseCommit(focusStepCommitId);
        walk.dispose();
        RevCommit parentCommit = focusStepCommit.getParent(0);
        return parentCommit.getId();
    }

    private static AbstractTreeIterator createTreeIterator(Repository repository, AnyObjectId commitId) throws IOException {
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(commitId);
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();

            return treeParser;
        }
    }

    @Override
    public List<Notification> appendNotification(Notification notification) {
        model.getNotifications().add(notification);
        return List.ofAll(model.getNotifications());
    }

    public void refresh() {
        ProjectDir projectDir = model.projectDirProperty().get();
        if (projectDir instanceof ProjectDir.ReadyProjectDir) {
            try {
                Git git = Git.open(projectDir.getDir());
                model.getSteps().setAll(loadSteps(git).asJava());
            } catch (IOException e) {
                LOG.error("Loading existing steps failed", e);
            }
        } else {
            model.getSteps().clear();
        }
    }

    @Override
    public void chooseDirectory(File dir) {
        if (dir == null) {
            model.projectDirProperty().setValue(new ProjectDir.InvalidProjectDir(new File("")));
        } else if (!dir.isDirectory()) {
            model.projectDirProperty().setValue(new ProjectDir.InvalidProjectDir(dir));
        } else {
            File gitRepo = new File(dir, GIT_REPO);
            boolean isInitialized = RepositoryCache.FileKey.isGitRepository(gitRepo, FS.detect());
            if (isInitialized) {
                model.projectDirProperty().setValue(new ProjectDir.ReadyProjectDir(dir));
                switchToCaptureMode();
            } else {
                model.projectDirProperty().setValue(new ProjectDir.UninitializedProjectDir(dir));
            }
        }
    }

    @Override
    public void initProject() {
        if (model.projectDirProperty().get() instanceof ProjectDir.UninitializedProjectDir) {
            File dir = model.projectDirProperty().get().getDir();
            Try<Git> tryInitGit = initGitRepo(dir);
            tryInitGit.onSuccess(git -> {
                model.projectDirProperty().setValue(new ProjectDir.ReadyProjectDir(dir));
                commitInitialStatus(git);
            });
        }
    }

    @Override
    public Try<String> captureStep(String title, String description) {
        try {
            File dir = model.projectDirProperty().get().getDir();
            Git git = Git.open(dir);

            Files.write(new File(dir, README_FILE_NAME).toPath(), description.getBytes());

            git.add().addFilepattern(".").call();

            git.commit().setAll(true).setMessage(format(CAPTURE_COMMIT_MSG_TEMPLATE, title)).call();

            Set<String> existingTags = HashSet.ofAll(git.getRepository().getTags().keySet());
            String tag = nextTag(existingTags);

            git.tag()
                    .setName(tag)
                    .setMessage(title)

                    // required for edit mode (moving rather than creating a tag)
                    .setForceUpdate(true)
                    .setAnnotated(true)

                    .call();


            switchToCaptureModeImpl(git);

            return Try.success(CAPTURE_SUCCESSFUL);
        } catch (GitAPIException | IOException e) {
            return Try.failure(e);
        }
    }

    @Override
    public Try<String> saveStep(String tag, String title, String description) {
        try {
            File dir = model.projectDirProperty().get().getDir();
            Git git = Git.open(dir);

            String editBranchName = EDIT_BRANCH_PREFIX + tag;
            git.branchCreate().setName(editBranchName).setStartPoint(tag).setForce(true).call();
            git.checkout().setName(editBranchName).call();

            Files.write(new File(dir, README_FILE_NAME).toPath(), description.getBytes());
            git.add().setUpdate(true).addFilepattern(README_FILE_NAME).call();
            git.commit().setMessage(format(EDIT_COMMIT_MSG_TEMPLATE, title)).call();

            git.tag()
                    .setName(tag)
                    .setMessage(title)

                    // required for edit mode (moving rather than creating a tag)
                    .setForceUpdate(true)
                    .setAnnotated(true)

                    .call();

            refresh();

            switchToEditMode(tag);

            return Try.success(SAVE_SUCCESSFUL);
        } catch (GitAPIException | IOException e) {
            return Try.failure(e);
        }
    }

    @Override
    public Try<String> switchToCaptureMode() {
        try {
            File dir = model.projectDirProperty().get().getDir();
            Git git = Git.open(dir);

            switchToCaptureModeImpl(git);

            return Try.success(ENTERING_CAPTURE_MODE_SUCCESSFUL);
        } catch (Throwable t) {
            return Try.failure(t);
        }
    }

    private void switchToCaptureModeImpl(Git git) throws GitAPIException {
        git.checkout().setName("master").call();
        String tag = Step.UPCOMING_STEP_TAG;
        model.interactionModeProperty().setValue(InteractionMode.capture(tag));

        Option<Step> stepOption = model.getStepByTag(tag);
        stepOption.forEach(updateStepDetails());

        refresh();
    }

    private Consumer<Step> updateStepDetails() {
        return step -> {
            model.titleProperty().setValue(step.getTitle());
            model.descriptionProperty().setValue(step.getDescription());
            model.getStepDiffEntries().clear();
        };
    }

    @Override
    public Try<String> switchToEditMode(String tag) {
        try {
            File dir = model.projectDirProperty().get().getDir();
            Git git = Git.open(dir);

            git.checkout()
                    .setName(tag)
                    .setStartPoint(tag)
                    .call();

            model.interactionModeProperty().set(InteractionMode.edit(tag));
            Option<Step> stepOption = model.getStepByTag(tag);
            stepOption.forEach(updateStepDetails());
            stepOption.forEach(step -> loadStepDiff(dir, step));

            refresh();

            return Try.success(CHECKOUT_SUCCESSFUL);
        } catch (Throwable t) {
            return Try.failure(t);
        }
    }

    private void loadStepDiff(File dir, Step step) {
        Try<List<StepDiffEntry>> tryStepDiffEntries = loadStepDiff(dir, step.getTag(), List.ofAll(model.getSteps()));
        tryStepDiffEntries.onSuccess(entries -> model.getStepDiffEntries().setAll(entries.toJavaList()));
        tryStepDiffEntries.onFailure(t -> notifyError(LOADING_STEP_FILE_CHANGES_FAILED, t));
    }

    @Override
    public Try<String> deleteStep(String tag) {
        try {
            File dir = model.projectDirProperty().get().getDir();
            Git git = Git.open(dir);

            git.tagDelete().setTags(tag).call();

            switchToCaptureModeImpl(git);

            return Try.success(DELETE_STEP_SUCCESSFUL);
        } catch (Throwable t) {
            return Try.failure(t);
        }
    }

}