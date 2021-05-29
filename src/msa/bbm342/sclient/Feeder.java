package msa.bbm342.sclient;

import java.io.IOException;
import java.util.Queue;

public class Feeder implements Runnable {

    private static final int MIN_BUFFER_SIZE = 10;

    private final Queue<Frame> frameBuffer;

    private int currentFrameIdx;
    private AsciiVideoConnection videoConnection;
    private Runnable onEndOfFeed;

    private boolean isFeeding;

    public Feeder(AsciiVideoConnection videoConnection, Queue<Frame> frameBuffer, Runnable onEndOfFeed) {
        this.frameBuffer = frameBuffer;
        this.currentFrameIdx = 0;
        this.videoConnection = videoConnection;
        this.onEndOfFeed = onEndOfFeed;
    }

    @Override
    public void run() {
        start();

        try {
            VideoInfo info = videoConnection.getVideoInfo();

            Frame fetched = null;
            while (isFeeding() && currentFrameIdx < info.getNumberOfDistinctFrames()) {
                try {
                    if (fetched == null) {
                        fetched = videoConnection.getFrame(currentFrameIdx++);
                    }
                    synchronized (frameBuffer) {
                        if (frameBuffer.size() < MIN_BUFFER_SIZE) {
                            frameBuffer.add(fetched);
                            fetched = null;
                        }
                    }
                } catch (IOException e) {
                    System.err.printf("Error while getting frame: %d%n", currentFrameIdx - 1);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.printf("Error while getting video info.%n");
            e.printStackTrace();
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
    }
}
