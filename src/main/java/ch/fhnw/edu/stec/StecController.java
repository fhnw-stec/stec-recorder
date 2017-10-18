package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.capture.StepCaptureController;
import ch.fhnw.edu.stec.gig.GigController;
import ch.fhnw.edu.stec.history.StepHistoryController;
import ch.fhnw.edu.stec.model.CaptureMode;
import ch.fhnw.edu.stec.model.GigDir;
import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.notification.Notification;
import ch.fhnw.edu.stec.notification.NotificationController;
import ch.fhnw.edu.stec.notification.NotificationPopupDispatcher;
import ch.fhnw.edu.stec.util.Labels;
import io.vavr.collection.*;
import io.vavr.control.Option;
import io.vavr.control.Try;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
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

final class StecController implements GigController, StepCaptureController, StepHistoryController, NotificationController {

    static final String GIT_REPO = ".git";
    static final String GIT_IGNORE_FILE_NAME = ".gitignore";
    static final String ADD_GIT_IGNORE_COMMIT_MSG = "Add " + GIT_IGNORE_FILE_NAME;
    static final String README_FILE_NAME = "README.adoc";

    private static final String STEP_PREFIX = "step-";
    private static final Logger LOG = LoggerFactory.getLogger(StecController.class);
    private static final String GIT_IGNORE_TEMPLATE_FILE_NAME = "/gitignore-template.txt";

    private final StecModel model;

    StecController(Stage popupOwner, StecModel model) {
        this.model = model;

        initModel();

        model.getNotifications().addListener(new NotificationPopupDispatcher(popupOwner));
        model.gigDirProperty().addListener((observable, oldValue, newValue) -> refresh());
        model.captureModeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue instanceof CaptureMode.Edit) {
                String tag = ((CaptureMode.Edit) newValue).getTag();
                Option<Step> stepOption = List.ofAll(model.getSteps()).find(s -> tag.equals(s.getTag()));
                stepOption.forEach(step -> {
                    model.titleProperty().setValue(step.getTitle());
                    model.descriptionProperty().setValue(step.getDescription());
                });
            } else if (newValue instanceof CaptureMode.Normal) {
                model.titleProperty().setValue("");
                model.descriptionProperty().setValue("");
            }
        });
    }

    private static Try<Git> initGitRepo(File dir) {
        try {
            return Try.success(Git.init().setDirectory(dir).call());
        } catch (GitAPIException e) {
            LOG.error("Git init failed.", e);
            return Try.failure(e);
        }
    }

    private static void commitGitIgnore(Git git) {
        try (InputStream gitIgnoreSource = StecController.class.getResourceAsStream(GIT_IGNORE_TEMPLATE_FILE_NAME)) {
            File workTree = git.getRepository().getWorkTree();

            File gitIgnoreTarget = new File(workTree, GIT_IGNORE_FILE_NAME);
            Files.copy(gitIgnoreSource, gitIgnoreTarget.toPath(), StandardCopyOption.REPLACE_EXISTING);
            git.add().addFilepattern(GIT_IGNORE_FILE_NAME).call();

            git.commit().setMessage(ADD_GIT_IGNORE_COMMIT_MSG).call();

        } catch (IOException | GitAPIException e) {
            LOG.error("Git commit .gitignore failed.", e);
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
        model.captureModeProperty().set(CaptureMode.normal());
        model.titleProperty().setValue("");
        model.descriptionProperty().setValue("");
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
                commitGitIgnore(git);
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
            git.add().setUpdate(true).addFilepattern(".").call();

            git.commit().setMessage(Labels.COMMIT_MSG).call();

            String tagName;
            if (model.captureModeProperty().get() instanceof CaptureMode.Edit) {
                tagName = ((CaptureMode.Edit) model.captureModeProperty().get()).getTag();
            } else {
                Set<String> existingTags = HashSet.ofAll(git.getRepository().getTags().keySet());
                tagName = nextTag(existingTags);
            }
            git.tag()
                    .setName(tagName)
                    .setMessage(title)

                    // required for edit mode (moving rather than creating a tag)
                    .setForceUpdate(true)
                    .setAnnotated(true)

                    .call();


            switchToNormalMode(git);

            return Try.success(Labels.CAPTURE_SUCCESSFUL);
        } catch (GitAPIException | IOException e) {
            return Try.failure(e);
        }
    }

    private void switchToNormalMode(Git git) throws GitAPIException {
        refresh();

        Step headStep = model.getSteps().get(model.getSteps().size() - 1);
        String tag = headStep.getTag();

        git.checkout()
                .setName(tag)
                .setStartPoint(tag)
                .call();

        model.captureModeProperty().setValue(CaptureMode.normal());

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

            model.captureModeProperty().set(CaptureMode.edit(tag));

            refresh();

            return Try.success(Labels.SWITCHING_TO_EDIT_MODE_SUCCESSFUL);
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

            refresh();

            return Try.success(Labels.DELETE_STEP_SUCCESSFUL);
        } catch (Throwable t) {
            return Try.failure(t);
        }
    }

    @Override
    public void notifyError(String message) {
        LOG.error(message);
        appendToModel(Notification.error(message));
    }

    @Override
    public void notifyError(String message, Throwable t) {
        LOG.error(message, t);
        appendToModel(Notification.error(message));
    }

    @Override
    public void notifyWarn(String message) {
        LOG.warn(message);
        appendToModel(Notification.warn(message));
    }

    @Override
    public void notifyInfo(String message) {
        LOG.info(message);
        appendToModel(Notification.info(message));
    }

    @Override
    public void notifySilent(String message) {
        LOG.debug(message);
        appendToModel(Notification.silent(message));
    }

    private void appendToModel(Notification notification) {
        Platform.runLater(() -> model.getNotifications().add(notification));
    }

}