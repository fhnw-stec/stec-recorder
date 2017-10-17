package ch.fhnw.edu.stec.model;

public final class Step {

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
