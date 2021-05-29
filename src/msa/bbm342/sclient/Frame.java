package msa.bbm342.sclient;

public class Frame {
    private int timeToDisplay;
    private String frame;

    public Frame(int timeToDisplay, String frame) {
        this.timeToDisplay = timeToDisplay;
        this.frame = frame;
    }

    public int getTimeToDisplay() {
        return timeToDisplay;
    }

    public String getFrame() {
        return frame;
    }
}
