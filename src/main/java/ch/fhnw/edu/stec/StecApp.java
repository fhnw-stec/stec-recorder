package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.notification.NotificationPopupDispatcher;
import ch.fhnw.edu.stec.status.StatusBarController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Scanner;

public final class StecApp extends Application {

    private static final double WIDTH = 1000;
    private static final double HEIGHT = 1000;
    private static final String TITLE = "STEC";

    private static final Logger LOG = LoggerFactory.getLogger(StecApp.class);

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

    private static String getVersionSuffix() {
        try {
            InputStream versionAsStream = StecApp.class.getResourceAsStream("/version.txt");
            String version = new Scanner(versionAsStream).useDelimiter("\\A").next();
            return " – " + version;
        } catch (Throwable t) {
            // Expected to fail if not launched from JAR (e.g. inside IDE)
        }
        return "";
    }

    @Override
    public void start(Stage stage) {

        StecModel model = new StecModel();
        StecController controller = new StecController(model);
        StecView view = new StecView(model, stage.getOwner(), controller);
        Scene scene = new Scene(view);

        model.getNotifications().addListener(new NotificationPopupDispatcher(stage));
        model.interactionModeProperty().addListener(new StatusBarController(model));

        stage.setTitle(TITLE + getVersionSuffix());
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

}