package ch.fhnw.edu.stec.model;

public abstract class InteractionMode {

    private final String tag;

    private InteractionMode(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public static Capture capture(String tag) {
        return new Capture(tag);
    }

    public static Edit edit(String tag) {
        return new Edit(tag);
    }

    public static final class Capture extends InteractionMode {
        private Capture(String tag) {
            super(tag);
        }
    }

    public static final class Edit extends InteractionMode {
        private Edit(String tag) {
            super(tag);
        }
    }

}
