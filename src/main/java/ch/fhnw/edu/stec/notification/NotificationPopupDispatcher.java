package ch.fhnw.edu.stec.notification;

import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public final class NotificationPopupDispatcher implements ListChangeListener<Notification> {

    private final Stage popupOwner;

    public NotificationPopupDispatcher(Stage popupOwner) {
        this.popupOwner = popupOwner;
    }

    @Override
    public void onChanged(Change<? extends Notification> c) {
        if (c.next()) {
            c.getAddedSubList().forEach(notification -> {
                        Notifications popup = Notifications.create();
                        popup.text(notification.getMessage());
                        popup.position(Pos.TOP_RIGHT);
                        popup.owner(popupOwner);
                        switch (notification.getType()) {
                            case ERROR:
                                popup.hideAfter(Duration.INDEFINITE);
                                popup.showError();
                                break;
                            case WARN:
                                popup.hideAfter(Duration.INDEFINITE);
                                popup.showWarning();
                                break;
                            case INFO:
                                popup.hideAfter(Duration.seconds(2));
                                popup.showInformation();
                                break;
                            case SILENT:
                                // no popup
                                break;
                        }
                    }
            );
        }
    }

}