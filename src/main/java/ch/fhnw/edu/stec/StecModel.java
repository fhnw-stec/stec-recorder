package ch.fhnw.edu.stec;

import ch.fhnw.edu.stec.form.StepFormModel;
import ch.fhnw.edu.stec.model.ProjectDir;
import ch.fhnw.edu.stec.model.InteractionMode;
import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.model.StepDiffEntry;
import ch.fhnw.edu.stec.notification.Notification;
import ch.fhnw.edu.stec.status.StatusBarModel;
import ch.fhnw.ima.memento.Memento;
import ch.fhnw.ima.memento.MementoModel;
import io.vavr.collection.List;
import io.vavr.control.Option;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

public final class StecModel implements StepFormModel, StatusBarModel {

    private final ObservableList<Notification> notifications = FXCollections.observableArrayList();
    private final ObjectProperty<ProjectDir> projectDir = new SimpleObjectProperty<>();
    private final MementoModel<Step> mementoModel = new MementoModel<>();
    private final ObjectProperty<InteractionMode> interactionMode = new SimpleObjectProperty<>();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final ObservableList<Node> statusBarLeftItems = FXCollections.observableArrayList();
    private final ObservableList<Node> statusBarRightItems = FXCollections.observableArrayList();
    private final ObservableList<StepDiffEntry> stepDiffEntries = FXCollections.observableArrayList();

    @Override
    public ObjectProperty<ProjectDir> projectDirProperty() {
        return projectDir;
    }

    public MementoModel<Step> getMementoModel() {
        return mementoModel;
    }

    @Override
    public Option<Step> getStepByTag(String tag) {
        if (Step.UPCOMING_STEP_TAG.equals(tag)) {
            return Option.of(Step.UPCOMING_STEP);
        } else {
            return List.ofAll(getSteps()).find(step -> step.getTag().equals(tag));
        }
    }

    public List<Step> getSteps() {
        return mementoModel.getAllMementosFlattened()
                .flatMap(mementoModel::getMemento)
                .map(Memento::getState);
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