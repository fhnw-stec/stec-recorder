package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.chooser.GigChooserController;
import ch.fhnw.edu.stec.status.GigStatusController;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

final class StecController implements GigChooserController, GigStatusController {

    private static final Logger LOG = LoggerFactory.getLogger(StecController.class);

    private final StecModel model;

    StecController(StecModel model) {
        this.model = model;

        this.model.gigDirectoryProperty().addListener((observable, oldRootDir, newRootDir) -> {
            File gitRepo = new File(newRootDir, ".git");
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
        LOG.debug("TODO: initGig");
    }

}
