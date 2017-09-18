package ch.fhnw.edu.stec.notification;

import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.time.format.DateTimeFormatter;

public class NotificationListView extends ListView<Notification> {

    private static final String CSS_RESOURCE = "notification-list-view.css";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public NotificationListView(ObservableList<Notification> items) {
        super(items);
        getStylesheets().add(getClass().getResource(CSS_RESOURCE).toExternalForm());
        setCellFactory(lv -> new NotificationListCell());
    }

    private static final class NotificationListCell extends ListCell<Notification> {

        @Override
        protected void updateItem(Notification notification, boolean empty) {
            super.updateItem(notification, empty);

            // necessary because list cells are recycled by JavaFX
            reset();

            if (notification != null) {
                String timestamp = DATE_TIME_FORMATTER.format(notification.getTimestamp());
                setText(timestamp + " " + notification.getMessage());
                getStyleClass().add(notification.getType().getStyleClassName());
            }
        }

        private void reset() {
            setText("");
            for (Notification.Type type : Notification.Type.values()) {
                getStyleClass().remove(type.getStyleClassName());
            }
        }

    }

}
