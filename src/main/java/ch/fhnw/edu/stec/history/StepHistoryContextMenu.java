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

        MenuItem editItem = new MenuItem(Labels.EDIT_CONTEXT_MENU_ITEM);
        editItem.setOnAction(e -> {
            Try<String> result = historyController.switchToEditMode(stepSupplier.get().getTag());
            onResult(notificationController, result, Labels.SWITCHING_TO_EDIT_MODE_FAILED);
        });

        MenuItem deleteItem = new MenuItem(Labels.DELETE_CONTEXT_MENU_ITEM);
        deleteItem.setOnAction(e -> {
            Try<String> result = historyController.deleteStep(stepSupplier.get().getTag());
            onResult(notificationController, result, Labels.DELETE_STEP_FAILED);
        });

        getItems().addAll(editItem, deleteItem);
    }

    private void onResult(NotificationController notificationController, Try<String> result, String msg) {
        result.onSuccess(notificationController::notifyInfo);
        result.onFailure(error -> notificationController.notifyError(msg, error));
    }

}