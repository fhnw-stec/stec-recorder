package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.dirchooser.DirectoryChooserController;

import java.io.File;

final class StecController implements DirectoryChooserController {

    private final StecModel model;

    StecController(StecModel model) {
        this.model = model;
    }

    @Override
    public void chooseDirectory(File dir) {
        if (dir != null && dir.isDirectory()) {
            model.rootDirectoryProperty().set(dir);
        }
    }

}
