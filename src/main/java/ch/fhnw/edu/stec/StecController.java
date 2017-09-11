package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.capture.StepCaptureController;
import ch.fhnw.edu.stec.chooser.GigChooserController;
import ch.fhnw.edu.stec.status.GigStatusController;
import io.vavr.control.Try;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
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
import java.util.HashMap;
import java.util.Map;

final class StecController implements GigChooserController, GigStatusController, StepCaptureController {

    private static final Logger LOG = LoggerFactory.getLogger(StecController.class);
    private static final String GIT_IGNORE_TEMPLATE_FILE_NAME = "/gitignore-template.txt";
    static final String GIT_REPO = ".git";
    static final String GIT_IGNORE_FILE_NAME = ".gitignore";
    static final String ADD_GIT_IGNORE_COMMIT_MSG = "Add " + GIT_IGNORE_FILE_NAME;

    private final StecModel model;

    StecController(StecModel model) {
        this.model = model;

        initModel();
    }

    private void initModel() {
        chooseDirectory(new File(System.getProperty("user.home")));
    }

    @Override
    public void chooseDirectory(File dir) {
        if (dir == null) {
            model.gigDirProperty().setValue(new StecModel.InvalidGigDir(new File("")));
        } else if (!dir.isDirectory()) {
            model.gigDirProperty().setValue(new StecModel.InvalidGigDir(dir));
        }else {
            File gitRepo = new File(dir, GIT_REPO);
            boolean isInitialized = RepositoryCache.FileKey.isGitRepository(gitRepo, FS.detect());
            if (isInitialized) {
                model.gigDirProperty().setValue(new StecModel.ReadyGigDir(dir));
                ObservableMap snapshots = FXCollections.observableMap(getSteps());
                model.snapshots().setValue(snapshots);
            } else {
                model.gigDirProperty().setValue(new StecModel.UninitializedGigDir(dir));
            }
        }
    }

    @Override
    public void initGig() {
        if (model.gigDirProperty().get() instanceof StecModel.UninitializedGigDir) {
            File dir = model.gigDirProperty().get().getDir();
            Try<Git> tryInitGit = initGitRepo(dir);
            tryInitGit.onSuccess(git -> {
                model.gigDirProperty().setValue(new StecModel.ReadyGigDir(dir));
                commitGitIgnore(git);
            });
        }
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

    @Override
    public void captureStep(String tagName, String description) {
        try {
            Git git = Git.open(model.gigDirProperty().get().getDir());

            git.add().addFilepattern(".").call();
            git.add().setUpdate(true).addFilepattern(".").call();

            git.commit().setMessage("Captured step").call();

            git.tag().setName(tagName).setMessage(description).call();

            getSteps();
        } catch (GitAPIException | IOException e) {
            LOG.error("Capturing a step failed.", e);
        }
    }

    private Map<String, String> getSteps() {
        try {
            Git git = Git.open(model.gigDirProperty().get().getDir());
            Map<String, Ref> tags = git.getRepository().getTags();
            LOG.info("Got all tags");
            for (String key : tags.keySet()) {
                LOG.debug(key);
            }

            return getDescriptions(git, tags);
        } catch (IOException e) {
            LOG.error("Fetching all tags failed.", e);
            return new HashMap<String, String>();
        }
    }

    private Map<String, String> getDescriptions(Git git, Map<String, Ref> tags) {

        Map<String, String> describedTags = new HashMap<>();
        try {
            RevWalk walk = new RevWalk(git.getRepository());
            for (String tagName : tags.keySet()) {
                Ref ref = tags.get(tagName);
                RevTag tag = walk.parseTag(ref.getObjectId());
                describedTags.put(tagName, tag.getFullMessage());
            }
            return describedTags;
        } catch (IOException e) {
            LOG.error("Fetching tag descriptions failed", e);
            return describedTags;
        }
    }

}