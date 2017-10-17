package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.model.CaptureMode;
import ch.fhnw.edu.stec.model.GigDir;
import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.notification.Notification;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class StecModel {

    private final ObjectProperty<GigDir> gigDir = new SimpleObjectProperty<>();
    private final ObservableList<Step> steps = FXCollections.observableArrayList();
    private final ObservableList<Notification> notifications = FXCollections.observableArrayList();
    private final ObjectProperty<CaptureMode> captureMode = new SimpleObjectProperty<>();

    ObjectProperty<GigDir> gigDirProperty() {
        return gigDir;
    }

    ObservableList<Step> getSteps() {
        return steps;
    }

    ObservableList<Notification> getNotifications() {
        return notifications;
    }

    ObjectProperty<CaptureMode> captureModeProperty() {
        return captureMode;
    }

}