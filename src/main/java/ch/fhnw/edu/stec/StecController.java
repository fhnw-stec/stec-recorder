package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.capture.StepCaptureController;
import ch.fhnw.edu.stec.chooser.GigChooserController;
import ch.fhnw.edu.stec.model.GigDir;
import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.notification.Notification;
import ch.fhnw.edu.stec.notification.NotificationController;
import ch.fhnw.edu.stec.notification.NotificationPopupDispatcher;
import ch.fhnw.edu.stec.status.GigStatusController;
import ch.fhnw.edu.stec.util.Labels;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Try;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

final class StecController implements GigChooserController, GigStatusController, StepCaptureController, NotificationController {

    static final String GIT_REPO = ".git";
    static final String GIT_IGNORE_FILE_NAME = ".gitignore";
    static final String ADD_GIT_IGNORE_COMMIT_MSG = "Add " + GIT_IGNORE_FILE_NAME;
    private static final Logger LOG = LoggerFactory.getLogger(StecController.class);
    private static final String GIT_IGNORE_TEMPLATE_FILE_NAME = "/gitignore-template.txt";
    private static final String README_FILE_NAME = "README.adoc";
    private final StecModel model;

    StecController(Stage popupOwner, StecModel model) {
        this.model = model;

        initModel();

        model.getNotifications().addListener(new NotificationPopupDispatcher(popupOwner));
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

    private void initModel() {
        chooseDirectory(new File(System.getProperty("user.home")));
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
                try {
                    Git git = Git.open(model.gigDirProperty().get().getDir());
                    model.getSteps().setAll(loadSteps(git).asJava());
                } catch (IOException e) {
                    LOG.error("Loading existing getSteps failed", e);
                }
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

            // TODO: Auto-generation of next available tag
            String tagName = "step-42";
            git.tag().setName(tagName).setMessage(title).call();

            model.getSteps().setAll(loadSteps(git).asJava());

            return Try.success("Capturing step successful");
        } catch (GitAPIException | IOException e) {
            LOG.error("Capturing step failed", e);
            return Try.failure(e);
        }
    }

    private static Seq<Step> loadSteps(Git git) {
        Map<String, Ref> tags = HashMap.ofAll(git.getRepository().getTags());
        RevWalk walk = new RevWalk(git.getRepository());
        return tags.flatMap(tag -> loadStep(walk, tag._1, tag._2));
    }

    private static Try<Step> loadStep(RevWalk walk, String tagName, Ref tagRef) {
        try {
            RevTag revTag = walk.parseTag(tagRef.getObjectId());
            Step step = new Step(tagName, revTag.getFullMessage());
            return Try.success(step);
        } catch (Throwable t) {
            LOG.error("Loading step details failed", t);
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