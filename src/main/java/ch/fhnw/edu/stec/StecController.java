package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.chooser.GigChooserController;
import ch.fhnw.edu.stec.status.GigStatusController;
import io.vavr.control.Try;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

final class StecController implements GigChooserController, GigStatusController {

    private static final Logger LOG = LoggerFactory.getLogger(StecController.class);
    private static final String GIT_IGNORE_TEMPLATE_FILE_NAME = "/gitignore-template.txt";
    static final String GIT_REPO = ".git";
    static final String GIT_IGNORE_FILE_NAME = ".gitignore";
    static final String ADD_GIT_IGNORE_COMMIT_MSG = "Add " + GIT_IGNORE_FILE_NAME;

    private final StecModel model;

    StecController(StecModel model) {
        this.model = model;

        this.model.gigDirectoryProperty().addListener((observable, oldRootDir, newRootDir) -> {
            File gitRepo = new File(newRootDir, GIT_REPO);
            boolean isInitialized = RepositoryCache.FileKey.isGitRepository(gitRepo, FS.detect());
            this.model.gigReady().setValue(isInitialized);
        });

        initModel();
    }

    private void initModel() {
        model.gigDirectoryProperty().set(new File(System.getProperty("user.home")));
    }

    @Override
    public void chooseDirectory(File dir) {
        if (dir != null && dir.isDirectory()) {
            model.gigDirectoryProperty().set(dir);
        }
    }

    @Override
    public void initGig() {
        if (!model.gigReady().get()) {
            Try<Git> tryInitGit = initGitRepo(model.gigDirectoryProperty().get());
            tryInitGit.onSuccess(git -> {
                model.gigReady().setValue(true);
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

}