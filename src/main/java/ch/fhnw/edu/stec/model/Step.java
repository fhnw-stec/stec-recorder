package ch.fhnw.edu.stec.model;

public final class Step {

    /** Used to identify the next step about to be captured. */
    public static final String UPCOMING_STEP_TAG = "upcoming-step-tag";

    /** Represents the next step about to be captured. */
    public static final Step UPCOMING_STEP = new Step(UPCOMING_STEP_TAG, "", "");

    private final String tag;
    private final String title;
    private final String description;

    public Step(String tag, String title, String description) {
        this.tag = tag;
        this.title = title;
        this.description = description;
    }

    public String getTag() {
        return tag;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

}
