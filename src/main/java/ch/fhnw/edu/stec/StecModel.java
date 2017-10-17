package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.history.StepHistoryModel;
import ch.fhnw.edu.stec.model.CaptureMode;
import ch.fhnw.edu.stec.model.GigDir;
import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.notification.Notification;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class StecModel implements StepHistoryModel {

    private final ObjectProperty<GigDir> gigDir = new SimpleObjectProperty<>();
    private final ObservableList<Step> steps = FXCollections.observableArrayList();
    private final ObservableList<Notification> notifications = FXCollections.observableArrayList();
    private final ObjectProperty<CaptureMode> captureMode = new SimpleObjectProperty<>();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();

    ObjectProperty<GigDir> gigDirProperty() {
        return gigDir;
    }

    @Override
    public ObservableList<Step> getSteps() {
        return steps;
    }

    ObservableList<Notification> getNotifications() {
        return notifications;
    }

    @Override
    public ObjectProperty<CaptureMode> captureModeProperty() {
        return captureMode;
    }

    StringProperty titleProperty() {
        return title;
    }

    StringProperty descriptionProperty() {
        return description;
    }

}