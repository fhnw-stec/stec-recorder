package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.dirchooser.DirectoryChooserView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Window;

final class StecView extends BorderPane {

    StecView(StecModel model, Window owner, StecController controller) {

        DirectoryChooserView directoryChooserView = new DirectoryChooserView(model.rootDirectoryProperty(), owner, controller);
        setTop(directoryChooserView);

    }

}