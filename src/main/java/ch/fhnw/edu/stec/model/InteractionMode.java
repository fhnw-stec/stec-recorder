package ch.fhnw.edu.stec.model;

public abstract class InteractionMode {

    private final Step step;

    private InteractionMode(Step step) {
        this.step = step;
    }

    public Step getStep() {
        return step;
    }

    public static Capture capture(Step step) {
        return new Capture(step);
    }

    public static Edit edit(Step step) {
        return new Edit(step);
    }

    public static final class Capture extends InteractionMode {
        private Capture(Step step) {
            super(step);
        }
    }

    public static final class Edit extends InteractionMode {
        private Edit(Step step) {
            super(step);
        }
    }

}
