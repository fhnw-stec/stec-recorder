package ch.fhnw.edu.stec.model;

public abstract class CaptureMode {

    public static CaptureMode normal() {
        return new Normal();
    }

    public static Edit edit(String tag) {
        return new Edit(tag);
    }

    public static final class Normal extends CaptureMode {
        private Normal() {
        }
    }

    public static final class Edit extends CaptureMode {

        private final String tag;

        private Edit(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }

    }

}
