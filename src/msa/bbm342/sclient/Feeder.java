package msa.bbm342.sclient;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public class Feeder implements Runnable {
    private static final int NOT_SET = -1;

    public static final int BUFFER_SIZE = Viewer.FPS * 10;

    private final Map<Integer, Frame> frameCache;

    private final BlockingQueue<Frame> frameBuffer;

    private int currentFrameIdx;
    private final AsciiVideoConnection videoConnection;
    private final VideoInfo videoInfo;
    private final Runnable onEndOfFeed;

    private boolean feeding;

    public Feeder(AsciiVideoConnection videoConnection, VideoInfo videoInfo, BlockingQueue<Frame> frameBuffer, Runnable onEndOfFeed) {
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

        while (feeding && currentFrameIdx < videoInfo.getFrameCount()) {
            try {
                Frame frame;
                int compressedFrameIdx = videoInfo.getCompressedFrameIdx(currentFrameIdx);
                if (frameCache.containsKey(compressedFrameIdx)) {
                    frame = frameCache.get(compressedFrameIdx);
                } else {
                    frame = videoConnection.getFrame(compressedFrameIdx);
                    frameCache.put(compressedFrameIdx, frame);
                }
                frameBuffer.put(frame);
                currentFrameIdx++;
            } catch (IOException | InterruptedException e) {
                System.err.printf("Error while getting frame: %d%n", currentFrameIdx);
                e.printStackTrace();
            }
        }

        stop();
        onEndOfFeed.run();
    }

    public boolean isFeeding() {
        return feeding;
    }

    public void stop() {
        feeding = false;
    }

    public void start() {
        feeding = true;
        frameCache.clear();
    }

    public int getCurrentFrameIdx() {
        return currentFrameIdx;
    }

    public void setCurrentFrameIdx(int currentFrameIdx) {
        this.currentFrameIdx = currentFrameIdx;
    }
}
