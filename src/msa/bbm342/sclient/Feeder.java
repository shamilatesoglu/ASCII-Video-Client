package msa.bbm342.sclient;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;

public class Feeder implements Runnable {
    private static final int NOT_SET = -1;

    public static final int BUFFER_SIZE = 1000;

    private final Map<Integer, Frame> frameCache;

    private final Queue<Frame> frameBuffer;

    private int currentFrameIdx;
    private final AsciiVideoConnection videoConnection;
    private final VideoInfo videoInfo;
    private final Runnable onEndOfFeed;

    private boolean isFeeding;

    public Feeder(AsciiVideoConnection videoConnection, VideoInfo videoInfo, Queue<Frame> frameBuffer, Runnable onEndOfFeed) {
        this.frameBuffer = frameBuffer;
        this.currentFrameIdx = 0;
        this.videoConnection = videoConnection;
        this.onEndOfFeed = onEndOfFeed;
        this.videoInfo = videoInfo;
        this.frameCache = new LinkedHashMap<>();
    }

    @Override
    public void run() {
        start();

        Frame fetched = null;
        while (isFeeding() && currentFrameIdx < videoInfo.getFrameCount()) {
            try {
                if (fetched == null) {
                    int compressedFrameIdx = videoInfo.getCompressedFrameIdx(currentFrameIdx);
                    if (frameCache.containsKey(compressedFrameIdx)) {
                        fetched = frameCache.get(compressedFrameIdx);
                    } else {
                        fetched = videoConnection.getFrame(compressedFrameIdx);
                        frameCache.put(compressedFrameIdx, fetched);
                    }
                }
                synchronized (frameBuffer) {
                    if (frameBuffer.size() < BUFFER_SIZE) {
                        frameBuffer.add(fetched);
                        fetched = null;
                        currentFrameIdx++;
                    }
                }
            } catch (IOException e) {
                System.err.printf("Error while getting frame: %d%n", currentFrameIdx - 1);
                e.printStackTrace();
            }
        }

        stop();
        System.out.println("End of stream.");
        onEndOfFeed.run();
    }

    public boolean isFeeding() {
        return isFeeding;
    }

    public void stop() {
        isFeeding = false;
    }

    public void start() {
        isFeeding = true;
        currentFrameIdx = 0;
        frameCache.clear();
    }

    public int getCurrentFrameIdx() {
        return currentFrameIdx;
    }

    public void setCurrentFrameIdx(int currentFrameIdx) {
        this.currentFrameIdx = currentFrameIdx;
    }
}
