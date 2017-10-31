package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.form.StepFormController;
import ch.fhnw.edu.stec.gig.GigController;
import ch.fhnw.edu.stec.history.StepHistoryController;
import ch.fhnw.edu.stec.model.GigDir;
import ch.fhnw.edu.stec.model.InteractionMode;
import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.model.StepDiffEntry;
import ch.fhnw.edu.stec.notification.Notification;
import ch.fhnw.edu.stec.notification.NotificationController;
import ch.fhnw.edu.stec.util.Labels;
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

import static io.vavr.API.*;

final class StecController implements GigController, StepFormController, StepHistoryController, NotificationController {

    static final String GIT_REPO = ".git";
    static final String GIT_IGNORE_FILE_NAME = ".gitignore";
    static final String INITIAL_STATUS_COMMIT_MSG = "Initial status";
    static final String README_FILE_NAME = "README.adoc";

    private static final String STEP_PREFIX = "step-";
    private static final Logger LOG = LoggerFactory.getLogger(StecController.class);
    private static final String GIT_IGNORE_TEMPLATE_FILE_NAME = "/gitignore-template.txt";

    private final StecModel model;

    StecController(StecModel model) {
        this.model = model;

        model.gigDirProperty().addListener((observable, oldValue, newValue) -> refresh());
        model.interactionModeProperty().addListener((observable, oldValue, newValue) -> {
            Option<Step> stepOption = model.getStepByTag(newValue.getTag());
            stepOption.forEach(step -> {
                model.titleProperty().setValue(step.getTitle());
                model.descriptionProperty().setValue(step.getDescription());
                model.getStepDiffEntries().clear();
                if (newValue instanceof InteractionMode.Edit) {
                    File dir = model.gigDirProperty().get().getDir();
                    Try<List<StepDiffEntry>> tryStepDiffEntries = loadStepDiff(dir, step.getTag(), List.ofAll(model.getSteps()));
                    tryStepDiffEntries.onSuccess(entries -> model.getStepDiffEntries().setAll(entries.toJavaList()));
                    tryStepDiffEntries.onFailure(t -> notifyError(Labels.LOADING_STEP_FILE_CHANGES_FAILED, t));
                }
            });

        });

        initModel();

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
        Integer maxIntSuffix = existingTags.map(t -> {
            String suffix = t.substring(STEP_PREFIX.length());
            if (suffix.chars().allMatch(Character::isDigit)) {
                return Integer.parseInt(suffix);
            } else {
                return 0;
            }
        }).max().getOrElse(0);
        return STEP_PREFIX + (maxIntSuffix + 1);
    }

    private static Seq<Step> loadSteps(Git git) {
        Repository repository = git.getRepository();
        Map<String, Ref> tags = HashMap.ofAll(repository.getTags());
        Seq<Step> steps = tags.flatMap(tag -> loadStep(repository, tag._1, tag._2));
        return steps.sortBy(Step::getTag);
    }

    private static Try<Step> loadStep(Repository repository, String tagName, Ref tagRef) {
        RevWalk revWalk = new RevWalk(repository);
        try {
            ObjectId tagId = tagRef.getObjectId();
            RevCommit commit = revWalk.parseCommit(tagId);
            String description = loadDescription(repository, commit);
            RevTag revTag = revWalk.parseTag(tagId);
            Step step = new Step(tagName, revTag.getFullMessage(), description);
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

            if (focusStepIndex == 0) {
                // TODO: Compare with parent commit (rather than previous step)
                return Try.success(List.empty());
            }

            Step previousStep = steps.get(focusStepIndex - 1);

            Map<String, Ref> tags = HashMap.ofAll(repository.getTags());
            AnyObjectId focusStepCommitId = tags.get(focusStepTag).get().getObjectId();
            AnyObjectId previousStepCommitId = tags.get(previousStep.getTag()).get().getObjectId();

            AbstractTreeIterator newTreeIt = createTreeIterator(repository, focusStepCommitId);
            AbstractTreeIterator oldTreeIt = createTreeIterator(repository, previousStepCommitId);

            List<DiffEntry> diffEntries = List.ofAll(git.diff()
                    .setOldTree(oldTreeIt)
                    .setNewTree(newTreeIt)
                    .call());

            List<StepDiffEntry> entries = diffEntries.map(e -> {
                StepDiffEntry.FileChangeType changeType = Match(e.getChangeType()).of(
                        Case($(DiffEntry.ChangeType.ADD), StepDiffEntry.FileChangeType.ADD),
                        Case($(DiffEntry.ChangeType.DELETE), StepDiffEntry.FileChangeType.DELETE),
                        Case($(), StepDiffEntry.FileChangeType.MODIFY) // modify/rename/copy all map to modify
                );
                return new StepDiffEntry(changeType, new File(dir, e.getNewPath()));
            });

            return Try.success(entries);
        } catch (Throwable t) {
            return Try.failure(t);
        }

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
        GigDir gigDir = model.gigDirProperty().get();
        if (gigDir instanceof GigDir.ReadyGigDir) {
            try {
                Git git = Git.open(gigDir.getDir());
                model.getSteps().setAll(loadSteps(git).asJava());
            } catch (IOException e) {
                LOG.error("Loading existing steps failed", e);
            }
        } else {
            model.getSteps().clear();
        }
    }

    private void initModel() {
        chooseDirectory(new File(System.getProperty("user.home")));
        model.interactionModeProperty().setValue(InteractionMode.capture(Step.UPCOMING_STEP_TAG));
    }

    @Override
    public void chooseDirectory(File dir) {
        if (dir == null) {
            model.gigDirProperty().setValue(new GigDir.InvalidGigDir(new File("")));
        } else if (!dir.isDirectory()) {
            model.gigDirProperty().setValue(new GigDir.InvalidGigDir(dir));
        } else {
            File gitRepo = new File(dir, GIT_REPO);
            boolean isInitialized = RepositoryCache.FileKey.isGitRepository(gitRepo, FS.detect());
            if (isInitialized) {
                model.gigDirProperty().setValue(new GigDir.ReadyGigDir(dir));
                switchToCaptureMode();
            } else {
                model.gigDirProperty().setValue(new GigDir.UninitializedGigDir(dir));
            }
        }
    }

    @Override
    public void initGig() {
        if (model.gigDirProperty().get() instanceof GigDir.UninitializedGigDir) {
            File dir = model.gigDirProperty().get().getDir();
            Try<Git> tryInitGit = initGitRepo(dir);
            tryInitGit.onSuccess(git -> {
                model.gigDirProperty().setValue(new GigDir.ReadyGigDir(dir));
                commitInitialStatus(git);
            });
        }
    }

    @Override
    public Try<String> captureStep(String title, String description) {
        try {
            File dir = model.gigDirProperty().get().getDir();
            Git git = Git.open(dir);

            Files.write(new File(dir, README_FILE_NAME).toPath(), description.getBytes());

            git.add().addFilepattern(".").call();

            git.commit().setMessage(Labels.COMMIT_MSG).call();

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

            return Try.success(Labels.CAPTURE_SUCCESSFUL);
        } catch (GitAPIException | IOException e) {
            return Try.failure(e);
        }
    }

    @Override
    public Try<String> saveStep(String tag, String title, String description) {
        try {
            File dir = model.gigDirProperty().get().getDir();
            Git git = Git.open(dir);

            Files.write(new File(dir, README_FILE_NAME).toPath(), description.getBytes());

            git.add().setUpdate(true).addFilepattern(README_FILE_NAME).call();

            git.commit().setMessage(Labels.COMMIT_MSG).call();

            git.tag()
                    .setName(tag)
                    .setMessage(title)

                    // required for edit mode (moving rather than creating a tag)
                    .setForceUpdate(true)
                    .setAnnotated(true)

                    .call();

            refresh();

            switchToEditMode(tag);

            return Try.success(Labels.SAVE_SUCCESSFUL);
        } catch (GitAPIException | IOException e) {
            return Try.failure(e);
        }
    }

    @Override
    public Try<String> switchToCaptureMode() {
        try {
            File dir = model.gigDirProperty().get().getDir();
            Git git = Git.open(dir);

            switchToCaptureModeImpl(git);

            return Try.success(Labels.ENTERING_CAPTURE_MODE_SUCCESSFUL);
        } catch (Throwable t) {
            return Try.failure(t);
        }
    }

    private void switchToCaptureModeImpl(Git git) throws GitAPIException {
        refresh();

        Step headStep = model.getSteps().get(model.getSteps().size() - 1);
        String tag = headStep.getTag();

        git.checkout()
                .setName(tag)
                .setStartPoint(tag)
                .call();

        model.interactionModeProperty().setValue(InteractionMode.capture(Step.UPCOMING_STEP_TAG));

        refresh();
    }

    @Override
    public Try<String> switchToEditMode(String tag) {
        try {
            File dir = model.gigDirProperty().get().getDir();
            Git git = Git.open(dir);

            git.checkout()
                    .setName(tag)
                    .setStartPoint(tag)
                    .call();

            model.interactionModeProperty().set(InteractionMode.edit(tag));

            refresh();

            return Try.success(Labels.CHECKOUT_SUCCESSFUL);
        } catch (Throwable t) {
            return Try.failure(t);
        }
    }

    @Override
    public Try<String> deleteStep(String tag) {
        try {
            File dir = model.gigDirProperty().get().getDir();
            Git git = Git.open(dir);

            git.tagDelete().setTags(tag).call();

            switchToCaptureModeImpl(git);

            return Try.success(Labels.DELETE_STEP_SUCCESSFUL);
        } catch (Throwable t) {
            return Try.failure(t);
        }
    }
}