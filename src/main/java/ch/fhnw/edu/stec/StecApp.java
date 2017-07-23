package ch.fhnw.edu.stec;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StecApp extends Application {

    private static final double WIDTH = 800;
    private static final double HEIGHT = 600;
    private static final String TITLE = "STEC";

    private static final Logger LOG = LoggerFactory.getLogger(StecApp.class);

    @Override
    public void start(Stage stage) {

        StecModel model = new StecModel();
        StecController controller = new StecController(model);
        StecView view = new StecView(model, stage.getOwner(), controller);
        Scene scene = new Scene(view);

        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);

        stage.setOnCloseRequest(e -> {
            LOG.trace("Main window closed, exiting application");
            Platform.exit();
            System.exit(0);
        });

        stage.show();
    }

    public static void main(String... args) {
        Platform.setImplicitExit(true); // Exit VM if main frame is closed
        try {
            LOG.trace("Launching application");
            launch(args);
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
            System.exit(-1);
        }
    }

}