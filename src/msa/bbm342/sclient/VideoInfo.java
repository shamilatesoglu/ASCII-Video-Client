package msa.bbm342.sclient;

import java.util.Map;

public class VideoInfo {
    private final int resolutionX;
    private final int resolutionY;
    private final Map<Integer, Integer> timeMap;
    private final Map<Integer, Integer> compressedFrames;

    public VideoInfo(int resolutionX, int resolutionY,
                     Map<Integer, Integer> compressedFrames,
                     Map<Integer, Integer> timeMap) {
        this.resolutionX = resolutionX;
        this.resolutionY = resolutionY;
        this.compressedFrames = compressedFrames;
        this.timeMap = timeMap;
    }

    public int getResolutionX() {
        return resolutionX;
    }

    public int getResolutionY() {
        return resolutionY;
    }

    public int getNumberOfDistinctFrames() {
        return timeMap.keySet().size();
    }

    public int getCompressedFrameIdx(int frameIdx) {
        return compressedFrames.get(frameIdx);
    }

    public int getFrameCount() {
        return compressedFrames.keySet().size();
    }

    public int getTimeToDisplay(int compressedFrameIdx) {
        return timeMap.get(compressedFrameIdx);
    }
}
