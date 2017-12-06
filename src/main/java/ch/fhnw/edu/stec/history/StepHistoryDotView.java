package ch.fhnw.edu.stec.history;

import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.notification.NotificationController;
import ch.fhnw.edu.stec.util.Labels;
import ch.fhnw.ima.memento.MementoModel;
import ch.fhnw.ima.memento.MementoView;
import io.vavr.control.Try;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public final class StepHistoryDotView extends StackPane {

    public StepHistoryDotView(MementoModel<Step> model, StepHistoryController historyController, NotificationController notificationController) {
        MementoView<Step> mementoView = new MementoView<>(model, id -> Color.web("#039ED3"));
        mementoView.setPadding(new Insets(10));

//        mementoView.getSelectionModel().addListener((observable, oldValue, newValue) -> {
//            newValue.forEach(mementoRef -> {
//                model.getMemento(mementoRef.getMementoId()).forEach(memento -> {
//                    String tag = memento.getState().getTag();
//                    Try<String> result = historyController.switchToEditMode(tag);
//                    result.onSuccess(notificationController::notifyInfo);
//                    result.onFailure(error -> notificationController.notifyError(Labels.CHECKOUT_FAILED, error));
//                });
//            });
//        });

        getChildren().add(new ScrollPane(mementoView));
    }

}
