package msa.bbm342.sclient;

public class VideoInfo {
    private final int resolutionX;
    private final int resolutionY;
    private final int numberOfDistinctFrames;

    public VideoInfo(int resolutionX, int resolutionY, int numberOfDistinctFrames) {
        this.resolutionX = resolutionX;
        this.resolutionY = resolutionY;
        this.numberOfDistinctFrames = numberOfDistinctFrames;
    }

    public int getResolutionX() {
        return resolutionX;
    }

    public int getResolutionY() {
        return resolutionY;
    }

    public int getNumberOfDistinctFrames() {
        return numberOfDistinctFrames;
    }
}
