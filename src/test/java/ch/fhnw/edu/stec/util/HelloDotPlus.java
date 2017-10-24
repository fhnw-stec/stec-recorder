package ch.fhnw.edu.stec.util;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HelloDotPlus extends Application {

    @Override
    public void start(Stage stage) {

        Scene scene = new Scene(new StackPane(new DotPlus(15)));

        stage.setScene(scene);
        stage.setWidth(100);
        stage.setHeight(100);

        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        stage.show();

    }

}