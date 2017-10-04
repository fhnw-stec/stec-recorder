package ch.fhnw.edu.stec.history;

import ch.fhnw.edu.stec.model.Step;
import ch.fhnw.edu.stec.notification.NotificationController;
import ch.fhnw.edu.stec.util.Labels;
import io.vavr.control.Try;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import java.util.function.Supplier;

public final class StepHistoryContextMenu extends ContextMenu {

    public StepHistoryContextMenu(Supplier<Step> stepSupplier, StepHistoryController historyController, NotificationController notificationController) {
        MenuItem checkoutItem = new MenuItem(Labels.CHECKOUT_CONTEXT_MENU_ITEM);
        checkoutItem.setOnAction(e -> {
            Try<String> result = historyController.checkoutStep(stepSupplier.get().getTag());
            result.onSuccess(notificationController::notifyInfo);
            result.onFailure(error -> notificationController.notifyError(Labels.CHECKOUT_FAILED, error));
        });
        getItems().add(checkoutItem);
    }

}