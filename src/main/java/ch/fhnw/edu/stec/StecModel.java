package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.form.StepFormModel;
import ch.fhnw.edu.stec.history.StepHistoryModel;
import ch.fhnw.edu.stec.model.GigDir;
import ch.fhnw.edu.stec.model.InteractionMode;
import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.model.StepDiffEntry;
import ch.fhnw.edu.stec.notification.Notification;
import ch.fhnw.edu.stec.status.StatusBarModel;
import io.vavr.collection.List;
import io.vavr.control.Option;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

public final class StecModel implements StepFormModel, StepHistoryModel, StatusBarModel {

    private final ObservableList<Notification> notifications = FXCollections.observableArrayList();
    private final ObjectProperty<GigDir> gigDir = new SimpleObjectProperty<>();
    private final ObservableList<Step> steps = FXCollections.observableArrayList();
    private final ObjectProperty<InteractionMode> interactionMode = new SimpleObjectProperty<>();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final ObservableList<Node> statusBarLeftItems = FXCollections.observableArrayList();
    private final ObservableList<Node> statusBarRightItems = FXCollections.observableArrayList();
    private final ObservableList<StepDiffEntry> stepDiffEntries = FXCollections.observableArrayList();

    @Override
    public ObjectProperty<GigDir> gigDirProperty() {
        return gigDir;
    }

    @Override
    public ObservableList<Step> getSteps() {
        return steps;
    }

    @Override
    public Option<Step> getStepByTag(String tag) {
        if (Step.UPCOMING_STEP_TAG.equals(tag)) {
            return Option.of(Step.UPCOMING_STEP);
        } else {
            return List.ofAll(steps).find(step -> step.getTag().equals(tag));
        }
    }

    ObservableList<Notification> getNotifications() {
        return notifications;
    }

    @Override
    public ObjectProperty<InteractionMode> interactionModeProperty() {
        return interactionMode;
    }

    @Override
    public StringProperty titleProperty() {
        return title;
    }

    @Override
    public StringProperty descriptionProperty() {
        return description;
    }

    @Override
    public ObservableList<Node> getStatusBarLeftItems() {
        return statusBarLeftItems;
    }

    @Override
    public ObservableList<Node> getStatusBarRightItems() {
        return statusBarRightItems;
    }

    public ObservableList<StepDiffEntry> getStepDiffEntries() {
        return stepDiffEntries;
    }

}