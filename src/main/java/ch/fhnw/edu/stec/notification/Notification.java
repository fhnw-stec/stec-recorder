package ch.fhnw.edu.stec.notification;

import java.time.LocalDateTime;

public final class Notification {

    public enum Type {

        ERROR("error"),
        WARN("warn"),
        INFO("info"),
        SILENT("silent"); // does not create a popup (just appears in the list)

        private final String styleClassName;

        Type(String styleClassName) {
            this.styleClassName = styleClassName;
        }

        public String getStyleClassName() {
            return styleClassName;
        }

    }

    private final LocalDateTime timestamp;
    private final Type type;
    private final String message;

    private Notification(Type type, String message) {
        this.timestamp = LocalDateTime.now();
        this.type = type;
        // poor man's padding...
        this.message = " " + message;
    }

    public static Notification error(String message) {
        return new Notification(Type.ERROR, message);
    }

    public static Notification warn(String message) {
        return new Notification(Type.WARN, message);
    }

    public static Notification info(String message) {
        return new Notification(Type.INFO, message);
    }

    public static Notification silent(String message) {
        return new Notification(Type.SILENT, message);
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

}
