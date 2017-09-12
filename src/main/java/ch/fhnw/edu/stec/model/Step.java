package ch.fhnw.edu.stec.model;

public final class Step {

    private final String tag;
    private final String title;

    public Step(String tag, String title) {
        this.tag = tag;
        this.title = title;
    }

    public String getTag() {
        return tag;
    }

    public String getTitle() {
        return title;
    }

}
