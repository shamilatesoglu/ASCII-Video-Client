package msa.bbm342.sclient;

import java.util.Queue;
import java.util.function.Consumer;

public class Viewer implements Runnable {
    public static final int FPS = 20;

    private final Consumer<Frame> frameConsumer;
    private final Queue<Frame> frameBuffer;

    private boolean isStreaming;
    private boolean hasFeedingStopped;
    private final Runnable onEndOfStream;

    private int currentFrameIdx;

    public Viewer(Queue<Frame> frameBuffer, Consumer<Frame> frameConsumer, Runnable onEndOfStream) {
        this.frameBuffer = frameBuffer;
        this.frameConsumer = frameConsumer;
        this.onEndOfStream = onEndOfStream;
        this.currentFrameIdx = 0;
    }

    public void play() {
        isStreaming = true;
        hasFeedingStopped = false;
        currentFrameIdx = 0;
        synchronized (frameBuffer) {
            frameBuffer.clear();
        }
    }

    public void stop() {
        isStreaming = false;
        synchronized (frameBuffer) {
            frameBuffer.clear();
        }
    }

    public void notifyFeedingStopped() {
        this.hasFeedingStopped = true;
        this.isStreaming = false;
    }

    @Override
    public void run() {
        play();

        // Ensure constant frame rate.
        long now;
        long previous = System.nanoTime();
        long timePerFrame = 1000000000L / FPS;
        long delta = 0;

        Frame frame = null;
        while (isStreaming || (hasFeedingStopped && frameBuffer.size() > 0)) {
            synchronized (frameBuffer) {
                if (frameBuffer.size() > 0) {
                    frame = frameBuffer.poll();
                }
            }
            if (frame != null) {
                while ((delta / timePerFrame) < 1) {
                    now = System.nanoTime();
                    delta += (now - previous);
                    previous = now;
                }

                frameConsumer.accept(frame);
                delta = 0;
                currentFrameIdx++;
                frame = null;
            }
        }

        stop();
        System.out.printf("Viewing stopped.%n");
        onEndOfStream.run();
    }

    public int getCurrentFrameIdx() {
        return currentFrameIdx;
    }

    public void setCurrentFrameIdx(int currentFrameIdx) {
        this.currentFrameIdx = currentFrameIdx;
    }
}
