package ch.fhnw.edu.stec.history;

import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.notification.NotificationController;
import ch.fhnw.edu.stec.util.Labels;
import ch.fhnw.ima.memento.MementoModel;
import ch.fhnw.ima.memento.MementoRef;
import ch.fhnw.ima.memento.MementoView;
import io.vavr.control.Option;
import io.vavr.control.Try;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public final class StepHistoryDotView extends StackPane {

    public StepHistoryDotView(StepHistoryModel stepHistoryModel, StepHistoryController historyController, NotificationController notificationController) {

        MementoModel<Step> mementoModel = stepHistoryModel.getMementoModel();

        ObjectProperty<Option<MementoRef>> selectionModel = stepHistoryModel.getMementoSelectionModel();
        MementoView<Step> mementoView = new MementoView<>(mementoModel, selectionModel, id -> Color.web("#039ED3"));
        mementoView.setPadding(new Insets(10));

        selectionModel.addListener((observable, oldValue, mementoRefOption) -> mementoRefOption.forEach(mementoRef -> {
            mementoModel.getMemento(mementoRef.getMementoId()).forEach(memento -> {
                String tag = memento.getState().getTag();
                Try<String> result = historyController.switchToEditMode(tag);
                result.onSuccess(notificationController::notifyInfo);
                result.onFailure(error -> notificationController.notifyError(Labels.CHECKOUT_FAILED, error));
            });
        }));

        getChildren().add(new ScrollPane(mementoView));
    }

}