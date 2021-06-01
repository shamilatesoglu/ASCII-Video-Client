import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public class Viewer implements Runnable {
    public static final int FPS = 20;

    private final Consumer<Frame> frameConsumer;
    private final BlockingQueue<Frame> frameBuffer;

    private boolean streaming;
    private boolean hasFeedingStopped;
    private final Runnable onEndOfViewing;

    private int currentFrameIdx;

    public Viewer(BlockingQueue<Frame> frameBuffer, Consumer<Frame> frameConsumer, Runnable onEndOfViewing) {
        this.frameBuffer = frameBuffer;
        this.frameConsumer = frameConsumer;
        this.onEndOfViewing = onEndOfViewing;
        this.currentFrameIdx = 0;
    }

    public void play() {
        streaming = true;
        hasFeedingStopped = false;
    }

    public void stop() {
        streaming = false;
        hasFeedingStopped = false;
    }

    public void notifyFeedingStopped() {
        this.hasFeedingStopped = true;
        this.streaming = false;
    }

    @Override
    public void run() {
        play();

        // Ensure constant frame rate.
        long now;
        long previous = System.nanoTime();
        long timePerFrame = 1000000000L / FPS;
        long delta = 0;

        while (streaming || (hasFeedingStopped && frameBuffer.size() > 0)) {
            try {
                Frame frame = frameBuffer.take();
                currentFrameIdx++;

                while ((delta / timePerFrame) < 1) {
                    now = System.nanoTime();
                    delta += (now - previous);
                    previous = now;
                }
                delta = 0;

                frameConsumer.accept(frame);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        onEndOfViewing.run();
    }

    public int getCurrentFrameIdx() {
        return currentFrameIdx;
    }

    public void setCurrentFrameIdx(int currentFrameIdx) {
        this.currentFrameIdx = currentFrameIdx;
    }
}
