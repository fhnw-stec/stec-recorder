package ch.fhnw.edu.stec.model;

public final class Step {

    private final String tag;
    private final String title;
    private final boolean head;

    public Step(String tag, String title, boolean head) {
        this.tag = tag;
        this.title = title;
        this.head = head;
    }

    public String getTag() {
        return tag;
    }

    public String getTitle() {
        return title;
    }

    public boolean isHead() {
        return head;
    }

}
