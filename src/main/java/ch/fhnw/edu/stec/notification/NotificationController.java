package ch.fhnw.edu.stec.notification;

import io.vavr.collection.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface NotificationController {

    // TODO: Make this private as soon as we're on Java 9
    Logger LOG = LoggerFactory.getLogger(NotificationController.class);

    List<Notification> appendNotification(Notification notification);

    default void notifyError(String message) {
        LOG.error(message);
        appendNotification(Notification.error(message));
    }

    default void notifyError(String message, Throwable t) {
        LOG.error(message, t);
        appendNotification(Notification.error(message));
    }

    default void notifyWarn(String message) {
        LOG.warn(message);
        appendNotification(Notification.warn(message));
    }

    default void notifyInfo(String message) {
        LOG.info(message);
        appendNotification(Notification.info(message));
    }

    default void notifySilent(String message) {
        LOG.debug(message);
        appendNotification(Notification.silent(message));
    }

}
