package ch.fhnw.edu.stec.history;

import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.notification.NotificationController;
import ch.fhnw.edu.stec.util.Labels;
import io.vavr.control.Try;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import java.util.function.Supplier;

final class StepHistoryContextMenu extends ContextMenu {

    StepHistoryContextMenu(Supplier<Step> stepSupplier, StepHistoryController historyController, NotificationController notificationController) {

        MenuItem deleteItem = new MenuItem(Labels.DELETE_CONTEXT_MENU_ITEM);
        deleteItem.setOnAction(e -> {
            Try<String> result = historyController.deleteStep(stepSupplier.get());
            result.onSuccess(notificationController::notifyInfo);
            result.onFailure(error -> notificationController.notifyError(Labels.DELETE_STEP_FAILED, error));
        });

        getItems().addAll(deleteItem);
    }

}